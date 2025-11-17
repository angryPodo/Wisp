package com.angrypodo.wisp

import com.angrypodo.wisp.annotations.Wisp
import com.angrypodo.wisp.generator.RouteFactoryGenerator
import com.angrypodo.wisp.mapper.toRouteInfo
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * @Wisp 어노테이션이 붙은 클래스를 찾아 유효성을 검증하고 코드를 생성하는 메인 프로세서 클래스입니다.
 */
internal class WispProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val factoryGenerator = RouteFactoryGenerator()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(WISP_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) return emptyList()

        val (processableSymbols, deferredSymbols) = symbols.partition { it.validate() }

        processableSymbols.forEach { processSymbol(it) }

        return deferredSymbols
    }

    private fun processSymbol(routeClass: KSClassDeclaration) {
        if (!routeClass.hasSerializableAnnotation()) {
            logger.error(
                "Wisp Error: Route '${routeClass.qualifiedName?.asString()}' must be annotated with @Serializable.",
                routeClass
            )
            return
        }

        val routeInfo = routeClass.toRouteInfo() ?: run {
            logger.error(
                "Wisp Error: Route '${routeClass.simpleName.asString()}' is missing @Wisp path or has invalid parameters.",
                routeClass
            )
            return
        }

        val fileSpec = factoryGenerator.generate(routeInfo)
        fileSpec.writeTo(codeGenerator, Dependencies(true, routeClass.containingFile!!))
    }

    private fun KSClassDeclaration.hasSerializableAnnotation(): Boolean = annotations.any { annotation ->
        val shortName = annotation.shortName.asString()
        val qualifiedName = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
        shortName == SERIALIZABLE_SHORT_NAME && qualifiedName == SERIALIZABLE_ANNOTATION
    }

    companion object {
        private val WISP_ANNOTATION = requireNotNull(Wisp::class.qualifiedName)
        private const val SERIALIZABLE_SHORT_NAME = "Serializable"
        private const val SERIALIZABLE_ANNOTATION = "kotlinx.serialization.Serializable"
    }
}
