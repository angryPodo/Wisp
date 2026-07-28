package com.angrypodo.wisp.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Factory that registers WispProcessor with KSP.
 */
class WispProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = WispProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger
    )
}
