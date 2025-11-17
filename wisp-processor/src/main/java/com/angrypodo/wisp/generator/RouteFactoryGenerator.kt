package com.angrypodo.wisp.generator

import com.angrypodo.wisp.model.ParameterInfo
import com.angrypodo.wisp.model.RouteInfo
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal class RouteFactoryGenerator {

    private val routeFactoryInterface = ClassName("com.angrypodo.wisp.runtime", "RouteFactory")
    private val missingParameterError = ClassName("com.angrypodo.wisp.runtime", "WispError", "MissingParameter")
    private val invalidParameterError = ClassName("com.angrypodo.wisp.runtime", "WispError", "InvalidParameter")

    fun generate(routeInfo: RouteInfo): FileSpec {
        val createFun = FunSpec.builder("create")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("params", MAP.parameterizedBy(STRING, STRING))
            .returns(ANY)
            .addCode(buildCreateFunctionBody(routeInfo))
            .build()

        val factoryObject = TypeSpec.objectBuilder(routeInfo.factoryClassName)
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(routeFactoryInterface)
            .addFunction(createFun)
            .build()

        return FileSpec.builder(routeInfo.factoryClassName.packageName, routeInfo.factoryClassName.simpleName)
            .addType(factoryObject)
            .build()
    }

    private fun buildCreateFunctionBody(routeInfo: RouteInfo): CodeBlock {
        val block = CodeBlock.builder()
        routeInfo.parameters.forEach { parameter ->
            val conversion = buildConversionCode(parameter, routeInfo.wispPath)
            block.addStatement("val %L = %L", parameter.name, conversion)
        }
        val constructorArgs = routeInfo.parameters.joinToString(", ") { "${it.name} = ${it.name}" }
        block.addStatement("return %T(%L)", routeInfo.routeClassName, constructorArgs)
        return block.build()
    }

    private fun buildConversionCode(param: ParameterInfo, wispPath: String): CodeBlock {
        val rawAccess = CodeBlock.of("params[%S]", param.name)
        val conversionLogic = getConversionLogic(param, rawAccess)

        if (param.isNullable) {
            return conversionLogic
        }

        val nonNullableType = param.typeName.copy(nullable = false)
        val errorType = if (nonNullableType == STRING) missingParameterError else invalidParameterError

        return CodeBlock.of(
            "(%L ?: throw %T(%S, %S))",
            conversionLogic,
            errorType,
            wispPath,
            param.name
        )
    }

    private fun getConversionLogic(param: ParameterInfo, rawAccess: CodeBlock): CodeBlock {
        val nonNullableType = param.typeName.copy(nullable = false)
        return when {
            param.isEnum -> CodeBlock.of(
                "runCatching { %T.valueOf(%L!!.uppercase()) }.getOrNull()",
                nonNullableType,
                rawAccess
            )
            nonNullableType == STRING -> rawAccess
            nonNullableType == INT -> CodeBlock.of("%L?.toIntOrNull()", rawAccess)
            nonNullableType == LONG -> CodeBlock.of("%L?.toLongOrNull()", rawAccess)
            nonNullableType == BOOLEAN -> CodeBlock.of("%L?.toBooleanStrictOrNull()", rawAccess)
            nonNullableType == FLOAT -> CodeBlock.of("%L?.toFloatOrNull()", rawAccess)
            else -> throw IllegalArgumentException("Unsupported type: ${param.typeName}")
        }
    }
}
