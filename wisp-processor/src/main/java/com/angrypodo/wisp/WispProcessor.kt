package com.angrypodo.wisp

import com.angrypodo.wisp.annotations.Wisp
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

/**
 * @Wisp 어노테이션이 붙은 클래스를 찾아 유효성을 검증하고 코드를 생성하는 메인 프로세서 클래스입니다.
 */
internal class WispProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val logger = environment.logger

    /**
     * KSP가 코드를 분석할 때 호출되는 함수입니다.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(WISP_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) return emptyList()

        logger.info("Wisp: found ${symbols.count()} @Wisp Route Symbols.")

        val (processableSymbols, deferredSymbols) = symbols.partition { it.validate() }


        processableSymbols.forEach { routeClass ->
            val routeInfo = routeClass.toRouteClassInfo()
            val result = WispValidator.validate(routeInfo)

            when (result) {
                is WispValidator.ValidationResult.Success ->
                    logger.info("Wisp: Route '${routeClass.simpleName.asString()}' validation successful.")

                is WispValidator.ValidationResult.Failure ->
                    logger.error(result.message, routeClass)
            }
        }

        return deferredSymbols
    }

    /**
     * KSP의 KSClassDeclaration을 RouteClassInfo로 변환하는 매퍼 함수입니다.
     */
    private fun KSClassDeclaration.toRouteClassInfo(): RouteClassInfo = RouteClassInfo(
        qualifiedName = qualifiedName?.asString(),
        simpleName = simpleName.asString(),
        annotations = annotations.map { it.toAnnotationInfo() }.toList(),
    )

    /**
     * KSP의 KSAnnotation을 AnnotationInfo로 변환하는 매퍼 함수입니다.
     */
    private fun KSAnnotation.toAnnotationInfo(): AnnotationInfo = AnnotationInfo(
        qualifiedName = annotationType.resolve().declaration.qualifiedName?.asString(),
        shortName = shortName.asString(),
    )

    companion object {
        private val WISP_ANNOTATION = requireNotNull(Wisp::class.qualifiedName)
    }
}
