package com.angrypodo.wisp

import com.angrypodo.wisp.annotations.Wisp
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

/**
 * @Wisp 어노테이션이 붙은 클래스를 찾아 유효성을 검증하고 코드를 생성하는 메인 프로세서 클래스입니다.
 */
class WispProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val logger = environment.logger
    private val wispAnnotation = requireNotNull(Wisp::class.qualifiedName)
    private val serializableShortName = "Serializable"
    private val serializableAnnotation = "kotlinx.serialization.Serializable"

    /**
     * KSP가 코드를 분석할 때 호출되는 함수입니다.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(wispAnnotation)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) return emptyList()

        logger.info("Wisp: found ${symbols.count()} @Wisp Route Symbols.")

        symbols.forEach { routeClass ->
            validateRoute(routeClass)
        }

        return symbols.filterNot { it.validate() }.toList()
    }

    /**
     * 라우트 심볼이 @Serializable 어노테이션을 가지고 있는지 검증하는 함수입니다.
     */
    private fun validateRoute(classDeclaration: KSClassDeclaration) {
        val isSerializable = classDeclaration.annotations.any { annotation ->
            annotation.shortName.asString() == serializableShortName && annotation.annotationType.resolve().declaration.qualifiedName?.asString() == serializableAnnotation
        }

        if (!isSerializable) {
            val className = classDeclaration.qualifiedName?.asString()

            logger.error(
                "Wisp Error: Route Class '$className' needs a @Wisp annotation with @Serializable annotation.",
                classDeclaration
            )

            throw IllegalStateException("Validation failed for $className")
        }

        logger.info("Wisp: Route '${classDeclaration.simpleName.asString()}' validation successful.")
    }
}
