package com.angrypodo.wisp

import com.angrypodo.wisp.WispValidator.validateDuplicatePaths
import com.angrypodo.wisp.annotations.Wisp
import com.angrypodo.wisp.generator.RouteFactoryGenerator
import com.angrypodo.wisp.generator.WispRegistryGenerator
import com.angrypodo.wisp.mapper.toRouteInfo
import com.angrypodo.wisp.model.RouteInfo
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * @Wisp 어노테이션이 붙은 클래스를 찾아 유효성을 검증하고 코드를 생성하는 메인 프로세서 클래스입니다.
 */
internal class WispProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val factoryGenerator = RouteFactoryGenerator(logger)
    private val registryGenerator = WispRegistryGenerator()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(WISP_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) return emptyList()

        val (processableSymbols, deferredSymbols) = symbols.partition { it.validate() }

        val routesWithSymbols = processableSymbols.mapNotNull { routeClass ->
            val routeInfo = processSymbol(routeClass) ?: return@mapNotNull null
            routeInfo to routeClass
        }

        val routeInfos = routesWithSymbols.map { it.first }

        val duplicateValidationResult = validateDuplicatePaths(routeInfos)

        if (duplicateValidationResult is WispValidator.ValidationResult.Failure) {
            duplicateValidationResult.errors.forEach { logger.error(it) }
            return deferredSymbols
        }

        if (routesWithSymbols.isNotEmpty()) {
            val sourceFiles = routesWithSymbols.mapNotNull { it.second.containingFile }.distinct()
            generateRouteRegistry(routeInfos, sourceFiles)
        }

        return deferredSymbols
    }

    private fun processSymbol(routeClass: KSClassDeclaration): RouteInfo? {
        if (!validateSerializable(routeClass)) return null

        val routeInfo = routeClass.toRouteInfo() ?: run {
            logInvalidRouteError(routeClass)
            return null
        }

        generateRouteFactory(routeClass, routeInfo)

        return routeInfo
    }

    private fun validateSerializable(routeClass: KSClassDeclaration): Boolean {
        if (routeClass.hasSerializableAnnotation()) return true
        val routeName = routeClass.qualifiedName?.asString()
        logger.error(
            "Wisp Error: Route '$routeName' must be annotated with @Serializable.",
            routeClass
        )
        return false
    }

    private fun logInvalidRouteError(routeClass: KSClassDeclaration) {
        val routeName = routeClass.simpleName.asString()
        logger.error(
            "Wisp Error: Route '$routeName' is missing @Wisp path or has invalid parameters.",
            routeClass
        )
    }

    private fun generateRouteFactory(
        routeClass: KSClassDeclaration,
        routeInfo: RouteInfo
    ) {
        val fileSpec = factoryGenerator.generate(routeInfo)
        val dependencies = Dependencies(false, routeClass.containingFile!!)
        fileSpec.writeTo(codeGenerator, dependencies)
    }

    private fun generateRouteRegistry(
        routeInfos: List<RouteInfo>,
        sourceFiles: List<KSFile>
    ) {
        val fileSpec = registryGenerator.generate(routeInfos)
        val dependencies = Dependencies(true, *sourceFiles.toTypedArray())
        fileSpec.writeTo(codeGenerator, dependencies)
    }

    private fun KSClassDeclaration.hasSerializableAnnotation(): Boolean {
        return annotations.any { annotation ->
            val shortName = annotation.shortName.asString()
            val qualifiedName = annotation.annotationType.resolve()
                .declaration.qualifiedName?.asString()
            val isSerializable = shortName == SERIALIZABLE_SHORT_NAME &&
                qualifiedName == SERIALIZABLE_ANNOTATION
            isSerializable
        }
    }

    companion object {
        private val WISP_ANNOTATION = requireNotNull(Wisp::class.qualifiedName)
        private const val SERIALIZABLE_SHORT_NAME = "Serializable"
        private const val SERIALIZABLE_ANNOTATION = "kotlinx.serialization.Serializable"
    }
}
