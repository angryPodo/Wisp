package com.angrypodo.wisp.generator

import com.angrypodo.wisp.WispClassName
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
}
