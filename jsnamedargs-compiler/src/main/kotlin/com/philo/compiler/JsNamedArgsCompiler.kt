package com.philo.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate

class JsNamedArgsCompiler(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("com.philo.JsNamedArgs")

        val functionSymbols = symbols.filterIsInstance<KSFunctionDeclaration>()
            .filter { it.validate() }

        val classSymbols = symbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }

        val processedFunctionSymbols = functionSymbols.toList()
        val processedClassSymbols = classSymbols.toList()
        val visitor = JsNamedArgsVisitor(codeGenerator)

        processedFunctionSymbols.forEach { it.accept(visitor, Unit) }
        processedClassSymbols.forEach { it.accept(visitor, Unit) }

        return symbols.toList() - processedFunctionSymbols.toSet() - processedClassSymbols.toSet()
    }
}

class JsNamedArgsCompilerProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return JsNamedArgsCompiler(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
}
