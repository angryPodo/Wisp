package com.angrypodo.wisp

import com.angrypodo.wisp.annotations.Wisp
import com.angrypodo.wisp.generator.RouteFactoryGenerator
import com.angrypodo.wisp.model.ParameterInfo
import com.angrypodo.wisp.model.RouteInfo
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * @Wisp 어노테이션이 붙은 클래스를 찾아 유효성을 검증하고 코드를 생성하는 메인 프로세서 클래스입니다.
 */
internal class WispProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val factoryGenerator = RouteFactoryGenerator()

    /**
     * KSP가 코드를 분석할 때 호출되는 함수입니다.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(WISP_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) return emptyList()

        val (processableSymbols, deferredSymbols) = symbols.partition { it.validate() }

        processableSymbols.forEach { routeClass ->
            if (!routeClass.hasSerializableAnnotation()) {
                logger.error(
                    "Wisp Error: Route '${routeClass.qualifiedName?.asString()}' must be annotated with @Serializable.",
                    routeClass
                )
                return@forEach
            }

            val routeInfo = routeClass.toRouteInfo()
            if (routeInfo == null) {
                logger.error(
                    "Wisp Error: Route '${routeClass.simpleName.asString()}' is missing @Wisp path.",
                    routeClass
                )
                return@forEach
            }

            val fileSpec = factoryGenerator.generate(routeInfo)
            val containingFile = routeClass.containingFile
            val dependencies = containingFile?.let { Dependencies(true, it) } ?: Dependencies(true)
            fileSpec.writeTo(codeGenerator, dependencies)
        }

        return deferredSymbols
    }

    private fun KSClassDeclaration.toRouteInfo(): RouteInfo? {
        val wispAnnotation = annotations.find { it.shortName.asString() == WISP_SIMPLE_NAME } ?: return null
        val pathArgument = wispAnnotation.arguments
            .firstOrNull { it.name?.asString() == WISP_PATH_ARGUMENT }
            ?: wispAnnotation.arguments.firstOrNull()

        val wispPath = pathArgument?.value as? String ?: return null

        val routeClassName = toClassName()
        val factoryClassName = ClassName(routeClassName.packageName, "${routeClassName.simpleName}RouteFactory")
        val parameters = primaryConstructor?.parameters?.mapNotNull { parameter ->
            val parameterName = parameter.name?.asString() ?: return@mapNotNull null
            val resolvedType = parameter.type.resolve()
            val declaration = resolvedType.declaration
            val isEnum = declaration is KSClassDeclaration && declaration.classKind == ClassKind.ENUM_CLASS

            ParameterInfo(
                name = parameterName,
                typeName = resolvedType.toTypeName(),
                isNullable = resolvedType.isMarkedNullable,
                isEnum = isEnum
            )
        } ?: emptyList()

        return RouteInfo(
            routeClassName = routeClassName,
            factoryClassName = factoryClassName,
            parameters = parameters,
            wispPath = wispPath
        )
    }

    companion object {
        private val WISP_ANNOTATION = requireNotNull(Wisp::class.qualifiedName)
        private const val WISP_SIMPLE_NAME = "Wisp"
        private const val WISP_PATH_ARGUMENT = "path"
        private const val SERIALIZABLE_SHORT_NAME = "Serializable"
        private const val SERIALIZABLE_ANNOTATION = "kotlinx.serialization.Serializable"
    }

    private fun KSClassDeclaration.hasSerializableAnnotation(): Boolean = annotations.any { annotation ->
        val shortName = annotation.shortName.asString()
        val qualifiedName = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
        shortName == SERIALIZABLE_SHORT_NAME && qualifiedName == SERIALIZABLE_ANNOTATION
    }
}