package com.angrypodo.wisp.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * WispProcessor를 KSP에 등록하는 팩토리 클래스입니다.
 */
class WispProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = WispProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger
    )
}
