package io.telereso.kmp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import io.telereso.kmp.annotations.FlutterExport
import io.telereso.kmp.annotations.ReactNativeExport
import io.telereso.kmp.processor.flutter.FlutterModelSymbolValidator
import io.telereso.kmp.processor.flutter.FlutterModelVisitor
import io.telereso.kmp.processor.model.ModelSymbolValidator
import io.telereso.kmp.processor.model.ModelVisitor
import io.telereso.kmp.processor.reactnative.ReactNativeMangerVisitor
import io.telereso.kmp.processor.reactnative.ReactNativeSymbolValidator

class KMProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KMPModelProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
            packageName = environment.options["packageName"]
        )
    }
}

class KMPModelProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val packageName: String? = null,
) : SymbolProcessor {
    private val flutterValidator = FlutterModelSymbolValidator(logger)
    private val modelValidator = ModelSymbolValidator(logger)
    private val reactNativeValidator = ReactNativeSymbolValidator(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("KMPModelProcessor was invoked.")
        return processModel(resolver, packageName) +
                processFlutter(resolver, packageName) +
                processReactNative(resolver, packageName)
    }

    private fun processModel(resolver: Resolver, packageName: String?): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = kotlinx.serialization.Serializable::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter { it.validate() }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor = ModelVisitor(codeGenerator, dependencies, packageName)
            var isUtilFileGenerated = false

            validatedSymbols.filter {
                modelValidator.isValid(it)
                true
            }.forEach {
                it.accept(visitor, Unit)
                if(!isUtilFileGenerated){
                    isUtilFileGenerated = true
                    visitor.shouldGenerateUtilFile = false
                }
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }

    private fun processFlutter(resolver: Resolver, packageName: String?): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = FlutterExport::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter { it.validate() }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor = FlutterModelVisitor(codeGenerator, dependencies, packageName)

            validatedSymbols.filter {
                flutterValidator.isValid(it)
                true
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }

    private fun processReactNative(resolver: Resolver, packageName: String?): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = ReactNativeExport::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.getSymbolsWithAnnotation(annotationName).toList()     // 1
            val validatedSymbols = resolved.filter { it.validate() }.toList()     // 2
            val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
            val visitor = ReactNativeMangerVisitor(logger,codeGenerator, dependencies, packageName)

            validatedSymbols.filter {
                reactNativeValidator.isValid(it)
                true
            }.forEach {
                it.accept(visitor, Unit)
            }
            unresolvedSymbols = resolved - validatedSymbols.toSet()     //4
        }
        return unresolvedSymbols
    }
}