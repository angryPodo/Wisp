package com.angrypodo.wisp.generator

import com.angrypodo.wisp.WispClassName
import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.angrypodo.wisp.model.RouteInfo
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal class WispRegistryGenerator {

    private val registryName = "WispRegistry"
    private val factoriesPropertyName = "factories"

    fun generate(routes: List<RouteInfo>): FileSpec {
        val factoriesProperty = buildFactoriesProperty(routes)

        val registryObject = TypeSpec.objectBuilder(registryName)
            .addSuperinterface(WispClassName.WISP_REGISTRY_SPEC)
            .addModifiers(KModifier.PUBLIC)
            .addProperty(factoriesProperty)
            .addFunction(buildCreateRouteFun(factoriesProperty))
            .addFunction(buildGetRoutePatternFun(routes))
            .build()

        return FileSpec.builder(WispClassName.GENERATED_PACKAGE, registryName)
            .addType(registryObject)
            .build()
    }

    private fun buildFactoriesProperty(routes: List<RouteInfo>): PropertySpec {
        val mapType = MAP.parameterizedBy(STRING, WispClassName.ROUTE_FACTORY)
        val initializerBlock = CodeBlock.builder()
            .add("mapOf(\n")
            .indent()
        routes.forEach { route ->
            initializerBlock.add("%S to %T,\n", route.wispPath, route.factoryClassName)
        }
        initializerBlock.unindent().add(")")

        return PropertySpec.builder(factoriesPropertyName, mapType)
            .addModifiers(KModifier.PRIVATE)
            .initializer(initializerBlock.build())
            .build()
    }

    private fun buildCreateRouteFun(factoriesProperty: PropertySpec): FunSpec {
        return FunSpec.builder("createRoute")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("path", STRING)
            .returns(ANY.copy(nullable = true))
            .addCode(
                CodeBlock.builder()
                    .beginControlFlow("for (pattern in %N.keys)", factoriesProperty)
                    .addStatement(
                        "val params = %T.match(path, pattern)",
                        WispClassName.WISP_URI_MATCHER
                    )
                    .beginControlFlow("if (params != null)")
                    .addStatement("val factory = %N[pattern]", factoriesProperty)
                    .addStatement("return factory?.create(params)")
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("return null")
                    .build()
            )
            .build()
    }

    private fun buildGetRoutePatternFun(routes: List<RouteInfo>): FunSpec {
        return FunSpec.builder("getRoutePattern")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("route", ANY)
            .returns(STRING.copy(nullable = true))
            .apply {
                val whenBlock = CodeBlock.builder()
                    .beginControlFlow("return when (route)")
                routes.forEach { routeInfo ->
                    val routePattern = buildRoutePatternString(routeInfo)
                    whenBlock.addStatement("is %T -> %S", routeInfo.routeClassName, routePattern)
                }
                whenBlock.addStatement("else -> null")
                    .endControlFlow()
                addCode(whenBlock.build())
            }
            .build()
    }

    private fun buildRoutePatternString(routeInfo: RouteInfo): String {
        return when (routeInfo) {
            is ClassRouteInfo -> {
                val pathParams = routeInfo.parameters
                    .filter { param ->
                        !param.isNullable && routeInfo.wispPath.contains(
                            "{${param.name}}"
                        )
                    }
                    .joinToString("") { "/{${it.name}}" }

                val queryParams = routeInfo.parameters
                    .filterNot { param ->
                        !param.isNullable && routeInfo.wispPath.contains(
                            "{${param.name}}"
                        )
                    }
                    .joinToString("&") { "${it.name}={${it.name}}" }

                val canonical = routeInfo.routeClassName.canonicalName
                val query = if (queryParams.isNotEmpty()) "?$queryParams" else ""

                return "$canonical$pathParams$query"
            }
            is ObjectRouteInfo -> routeInfo.routeClassName.canonicalName
        }
    }
}
