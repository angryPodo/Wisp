package com.angrypodo.wisp.generator

import com.angrypodo.wisp.WispClassName.GENERATED_PACKAGE
import com.angrypodo.wisp.WispClassName.ROUTE_FACTORY
import com.angrypodo.wisp.model.RouteInfo
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal object WispRegistryGenerator {
    private const val REGISTRY_NAME = "WispRegistry"
    private const val FACTORIES_PROPERTY_NAME = "factories"
    private const val GET_FACTORY_FUN_NAME = "getRouteFactory"

    fun generate(routes: List<RouteInfo>): FileSpec {
        val mapType = MAP.parameterizedBy(STRING, ROUTE_FACTORY)

        val initializerBlock = CodeBlock.builder()
            .add("mapOf(\n")
            .indent()

        routes.forEach { route ->
            initializerBlock.add("%S to %T,\n", route.wispPath, route.factoryClassName)
        }

        initializerBlock.unindent().add(")")

        val factoriesProperty = PropertySpec.builder(FACTORIES_PROPERTY_NAME, mapType)
            .addModifiers(KModifier.PRIVATE)
            .initializer(initializerBlock.build())
            .build()

        val getFactoryFun = FunSpec.builder(GET_FACTORY_FUN_NAME)
            .addModifiers(KModifier.INTERNAL)
            .addParameter("path", STRING)
            .returns(ROUTE_FACTORY.copy(nullable = true))
            .addStatement("return %N[path]", factoriesProperty)
            .build()

        val registryObject = TypeSpec.objectBuilder(REGISTRY_NAME)
            .addModifiers(KModifier.INTERNAL)
            .addProperty(factoriesProperty)
            .addFunction(getFactoryFun)
            .build()

        return FileSpec.builder(GENERATED_PACKAGE, REGISTRY_NAME)
            .addType(registryObject)
            .build()
    }
}
