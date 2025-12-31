package com.angrypodo.wisp.generator

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.angrypodo.wisp.model.ParameterInfo
import com.angrypodo.wisp.model.RouteInfo
import com.angrypodo.wisp.util.WispClassName
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal class RouteFactoryGenerator(
    private val logger: KSPLogger
) {
    private val jsonClass = ClassName("kotlinx.serialization.json", "Json")
    private val jsonObjectClass = ClassName("kotlinx.serialization.json", "JsonObject")
    private val jsonPrimitiveClass = ClassName("kotlinx.serialization.json", "JsonPrimitive")
    private val jsonElementClass = ClassName("kotlinx.serialization.json", "JsonElement")
    private val decodeFromJsonElement = MemberName(
        "kotlinx.serialization.json",
        "decodeFromJsonElement"
    )

    fun generate(routeInfo: RouteInfo): FileSpec {
        val createFun = FunSpec.builder("create")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("params", MAP.parameterizedBy(STRING, STRING))
            .returns(ANY)
            .addCode(buildCreateFunctionBody(routeInfo))
            .build()

        val factoryObject = TypeSpec.objectBuilder(routeInfo.factoryClassName)
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(WispClassName.ROUTE_FACTORY)
            .addFunction(createFun)
            .build()

        return FileSpec.builder(
            routeInfo.factoryClassName.packageName,
            routeInfo.factoryClassName.simpleName
        )
            .addType(factoryObject)
            .build()
    }

    private fun buildCreateFunctionBody(routeInfo: RouteInfo): CodeBlock {
        if (routeInfo is ObjectRouteInfo) {
            return CodeBlock.of("return %T", routeInfo.routeClassName)
        }

        val info = routeInfo as ClassRouteInfo
        val block = CodeBlock.builder()

        // 1. Prepare JSON fields map
        block.addStatement(
            "val jsonFields = mutableMapOf<%T, %T>()",
            STRING,
            jsonElementClass
        )

        // 2. Iterate parameters and populate map
        info.parameters.forEach { param ->
            val conversion = getJsonConversion(param)
            block.beginControlFlow("params[%S]?.let", param.name)
            block.addStatement("jsonFields[%S] = %L", param.name, conversion)
            block.endControlFlow()
        }

        // 3. Decode
        block.addStatement("val jsonObject = %T(jsonFields)", jsonObjectClass)

        // Use default Json instance and the extension function
        block.addStatement(
            "return %T.Default.%M<%T>(jsonObject)",
            jsonClass,
            decodeFromJsonElement,
            routeInfo.routeClassName
        )

        return block.build()
    }

    private fun getJsonConversion(param: ParameterInfo): CodeBlock {
        val type = param.typeName.copy(nullable = false)
        return when (type) {
            STRING -> CodeBlock.of("%T(it)", jsonPrimitiveClass)
            INT -> CodeBlock.of("%T(it.toInt())", jsonPrimitiveClass)
            LONG -> CodeBlock.of("%T(it.toLong())", jsonPrimitiveClass)
            BOOLEAN -> CodeBlock.of("%T(it.toBoolean())", jsonPrimitiveClass)
            FLOAT -> CodeBlock.of("%T(it.toFloat())", jsonPrimitiveClass)
            DOUBLE -> CodeBlock.of("%T(it.toDouble())", jsonPrimitiveClass)
            else -> {
                // Fallback for Enum or others
                CodeBlock.of("%T(it)", jsonPrimitiveClass)
            }
        }
    }
}
