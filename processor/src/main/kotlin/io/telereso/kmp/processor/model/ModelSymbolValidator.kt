package io.telereso.kmp.processor.model

import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

class ModelSymbolValidator(private val logger: KSPLogger) {
    fun isValid(symbol: KSAnnotated): Boolean {
        return symbol is KSClassDeclaration
                && symbol.validate() && !symbol.isInternal()
    }
}