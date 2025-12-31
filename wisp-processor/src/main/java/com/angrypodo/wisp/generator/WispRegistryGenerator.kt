package com.angrypodo.wisp.generator

import com.angrypodo.wisp.model.RouteInfo
import com.angrypodo.wisp.util.WispClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal data class GeneratedRegistry(
    val fileSpec: FileSpec,
    val className: String
)

internal class WispRegistryGenerator {

    fun generate(routes: List<RouteInfo>): GeneratedRegistry {
        val hash = computeRoutesHash(routes)
        val registryClassName = "WispModuleRegistry_$hash"

        val factoriesProperty = buildFactoriesProperty(routes)

        val registryObject = TypeSpec.classBuilder(registryClassName)
            .addSuperinterface(WispClassName.WISP_MODULE_REGISTRY) // Changed interface
            .addModifiers(KModifier.PUBLIC)
            .addProperty(factoriesProperty)
            .addFunction(buildGetRoutesFun(factoriesProperty))
            .build()

        val fileSpec = FileSpec.builder(WispClassName.GENERATED_PACKAGE, registryClassName)
            .addType(registryObject)
            .build()

        return GeneratedRegistry(
            fileSpec = fileSpec,
            className = "${WispClassName.GENERATED_PACKAGE}.$registryClassName"
        )
    }

    private fun computeRoutesHash(routes: List<RouteInfo>): String {
        // Sort routes to ensure deterministic hash
        val sortedPaths = routes.map { it.wispPath }.sorted()
        return sortedPaths.joinToString("|").hashCode().toString().replace("-", "N")
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

        return PropertySpec.builder("factories", mapType)
            .addModifiers(KModifier.PRIVATE)
            .initializer(initializerBlock.build())
            .build()
    }

    private fun buildGetRoutesFun(factoriesProperty: PropertySpec): FunSpec {
        val returnType = MAP.parameterizedBy(STRING, WispClassName.ROUTE_FACTORY)
        return FunSpec.builder("getRoutes")
            .addModifiers(KModifier.OVERRIDE)
            .returns(returnType)
            .addStatement("return %N", factoriesProperty)
            .build()
    }
}
