package ru.impression.ui_generator_processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class UIGeneratorProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        UIGenerator(environment.codeGenerator, environment.logger, environment.options["packageName"]!!)
}