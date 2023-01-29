package io.telereso.kmp.processor.reactnative

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.*
import java.io.OutputStream

class ReactNativeMangerVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() { //1
    val skipFunctions = listOf("equals", "hashCode", "toString")

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {

        createAndroidManager(classDeclaration)
        createIosManager(classDeclaration)
        createReactNativeIndex(classDeclaration)
    }

    private fun createAndroidManager(classDeclaration: KSClassDeclaration) {

        val packageString = packageName ?: classDeclaration.packageName.asString()
        val modelsPackageString = packageString.removeSuffix(".client").plus(".models")
        val originalClassName = classDeclaration.simpleName.getShortName()

        logger.logging("Create Android Manager for $originalClassName")


        val className = originalClassName.removeSuffix("Manager")
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()
        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "${packageString}.rn",
            fileName = "${className}Module",
            extensionName = "kt"
        )

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyAndroid().second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                """  
                |  @ReactMethod
                |  fun ${it.simpleName.asString()}(${
                    it.getTypedParametersAndroid()
                        .let { params -> if (params.isNotBlank()) "$params, " else params }
                }promise: Promise) {
                |${it.getMethodBodyAndroid().first}
                |  }
                """.trimMargin()
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${modelsPackageString}.models.$it" }

        outputStream.write(
            """
            |package $packageString.rn
            |
            |import $packageString.$originalClassName
            |import $modelsPackageString.*
            |import com.facebook.react.bridge.*
            |import kotlin.js.ExperimentalJsExport
            |
            |
            |@OptIn(ExperimentalJsExport::class)
            |class ${className}Module(reactContext: ReactApplicationContext) :
            |  ReactContextBaseJavaModule(reactContext) {
            |
            |  private val manager = $originalClassName.getInstance()
            |
            |  override fun getName(): String {
            |    return NAME
            |  }
            |
            |${methods.joinToString("\n\n")}
            |
            |  companion object {
            |    const val NAME = "$className"
            |  }
            |}
            """.trimMargin().toByteArray()
        )
    }

    private fun createIosManager(classDeclaration: KSClassDeclaration) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val originalClassName = classDeclaration.simpleName.getShortName()
        val className = originalClassName.removeSuffix("Manager")
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        logger.logging("Create iOS Manager for $originalClassName")

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "ios",
            fileName = "$className",
            extensionName = "swift"
        )

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyIos(className).second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                """
                |    @objc(${
                    it.getTypedHeaderParametersIos()
                })
                |    func ${it.simpleName.asString()}(${
                    it.getTypedParametersIos()
                        .let { params -> if (params.isNotBlank()) "$params, " else "_ " }
                }resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
                |        ${it.getMethodBodyIos(className).first}
                |    }
                """.trimMargin()
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }

        outputStream.write(
            """
            |import $className
            |
            |extension String: Error {
            |}
            |
            |@objc($className)
            |class $className: NSObject {
            |    private class func getManger() -> ${className}Manager? {
            |        do {
            |            return try ${className}Manager.Companion().getInstance()
            |        } catch {
            |            return nil
            |        }
            |    }
            |    // uncomment for testing, handle builder constructor if changed 
            |    //var a = ${className}Manager.Builder(databaseDriverFactory: DatabaseDriverFactory()).build()
            |    var manager = getManger()
            |
            |${methods.joinToString("\n\n")}
            |
            |}
            """.trimMargin().toByteArray()
        )

        createIosManagerHeader(classDeclaration)
    }

    private fun createIosManagerHeader(classDeclaration: KSClassDeclaration) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val originalClassName = classDeclaration.simpleName.getShortName()
        val className = originalClassName.removeSuffix("Manager")
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        logger.logging("Create iOS Manager Header for $originalClassName")

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "ios",
            fileName = className,
            extensionName = "m"
        )

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyIos(className).second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                """
                |RCT_EXTERN_METHOD(${it.getTypedHeaderParametersIosOC()})
                """.trimMargin()
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }

        outputStream.write(
            """
            |#import <React/RCTBridgeModule.h>
            |
            |@interface RCT_EXTERN_MODULE($className, NSObject)
            |
            |${methods.joinToString("\n\n")}
            |
            |+ (BOOL)requiresMainQueueSetup
            |{
            |  return NO;
            |}
            |
            |@end
            |
            """.trimMargin().toByteArray()
        )
    }

    private fun createReactNativeIndex(classDeclaration: KSClassDeclaration) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val originalClassName = classDeclaration.simpleName.getShortName()
        val className = originalClassName.removeSuffix("Manager")
        val projectName = className.removeSuffix("Client").lowercase()
        val modelPackage = packageString.removeSuffix(".client").plus(".models")
        val modelClassName = projectName.snakeToUpperCamelCase().plus("Models")
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        logger.logging("Create JS Manager for $originalClassName")

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "js",
            fileName = "index",
            extensionName = "tsx"
        )

        val modelImports = hashSetOf<String>()
        val modelFromJsonImports = hashSetOf<String>()
        val modelToJsonImports = hashSetOf<String>()

        classDeclaration.getAllFunctions().forEach {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                val typedParams = it.getTypedParametersJs().second
                it.getMethodBodyJs(className).let { trip ->
                    trip.second?.let { klass ->
                        typedParams.add(klass)
                        modelFromJsonImports.add(klass)
                    }
                    modelToJsonImports.addAll(trip.third)
                }
                modelImports.addAll(typedParams)
            }
        }

        val modelImportString =
            modelImports.joinToString("\n") {
                """
                const $it = ${modelClassName}.${it};
                """.trimIndent()
            }

        val modelJsonImportString =
            modelFromJsonImports.joinToString("\n") {
                """
                // eslint-disable-next-line @typescript-eslint/no-unused-vars    
                // @ts-ignore
                const ${it}FromJson = ${modelClassName}.${it}FromJson;
                // eslint-disable-next-line @typescript-eslint/no-unused-vars
                // @ts-ignore
                const ${it}FromJsonArray = ${modelClassName}.${it}FromJsonArray;
                """.trimIndent()
            }

        val modelToJsonImportsString =
            modelToJsonImports.joinToString("\n") {
                """
                const ${it}ToJson = ${modelClassName}.${it}ToJson;
                """.trimIndent()
            }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                val res = it.getMethodBodyJs(className)
                val typedParams = it.getTypedParametersJs()
                res.second?.let { klass: String ->
                    modelImports.add(klass)
                }
                modelImports.addAll(typedParams.second)

                """
                |export function ${it.simpleName.asString()}(${typedParams.first}): Promise<${res.second.jsType()}> {
                |  return new Promise<${res.second.jsType()}>((resolve, reject) => {
                |      ${res.first}
                |  });
                |}
                """.trimMargin()
            } else
                null
        }

        outputStream.write(
            """
            |import { NativeModules, Platform } from 'react-native';
            |
            |const LINKING_ERROR =
            |  `The package 'react-native-${projectName}-client' doesn't seem to be linked. Make sure: \n\n` +
            |  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
            |  '- You rebuilt the app after installing the package\n' +
            |  '- You are not using Expo Go\n';
            |
            |const ${projectName.snakeToUpperCamelCase()}Models = require('$projectName-models').${modelPackage};
            |
            |
            |const $className = NativeModules.${className}
            |  ? NativeModules.${className}
            |  : new Proxy(
            |      {},
            |      {
            |        get() {
            |          throw new Error(LINKING_ERROR);
            |        },
            |      }
            |    );
            |    
            |$modelImportString
            |$modelJsonImportString
            |$modelToJsonImportsString
            |
            |${methods.joinToString("\n\n")}
            |
            """.trimMargin().toByteArray()
        )
    }

}

const val PREFIX_TASK = "Task<"
const val PREFIX_TASK_ARRAY = "Task<Array<"
val REGEX_TASK = Regex("(?<=Task<)(.*?)(?=>)")
val REGEX_TASK_ARRAY = Regex("(?<=Task<Array<)(.*?)(?=>)")

private fun KSFunctionDeclaration.getResultAndroid(): Pair<String, String?> {


    val type = returnType?.resolve()
    val typeString = type?.toString()
    return when {
        typeString == null -> "it" to null
        typeString.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(typeString)?.value

            "${klass}.toJson(it)" to klass
        }
        typeString.startsWith(PREFIX_TASK) -> {
            val klass = REGEX_TASK.find(typeString)?.value
            val nullabilityString = if (klass?.endsWith("?") == true) "?" else ""
//            val nullabilityString = when (type.nullability) {
//                Nullability.NULLABLE -> "?"
//                else -> ""
//            }
            "it${nullabilityString}.toJson()" to null
        }
        else -> {
            "it" to null
        }
    }
}

private fun KSFunctionDeclaration.getResultIos(): Pair<String, String?> {

    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "res" to null
        type.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(type)?.value
            "${klass}.Companion().toJson(array: res)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            "res.toJson()" to null
        }
        else -> {
            "res" to null
        }
    }
}

private fun KSFunctionDeclaration.getResultJs(): Pair<String, String?> {

    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "data" to null
        type.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(type)?.value?.jsType()?.removePrefix("typeof ")
            "${klass}FromJsonArray(${klass}.Companion,data)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            val klass = REGEX_TASK.find(type)?.value?.jsType()?.removePrefix("typeof ")
            "${klass}FromJson(${klass}.Companion,data)" to klass
        }
        else -> {
            "data" to null
        }
    }
}

private fun KSFunctionDeclaration.getMethodBodyAndroid(): Pair<String, String?> {
    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "" to null
        type.startsWith(PREFIX_TASK) -> {
            val res = getResultAndroid()
            """       
            |    manager.${simpleName.asString()}(${getParametersAndroid()}).onSuccess {
            |      promise.resolve(${res.first})
            |    }.onFailure {
            |      promise.reject(it)
            |    }
           """.trimMargin() to res.second
        }
        else -> {
            "       try {\n" +
                    "          promise.resolve(manager.${simpleName.asString()}(${getParametersAndroid()})) \n" +
                    "       } catch (e:Exception){\n" +
                    "          promise.reject(e)\n" +
                    "       }\n" to null
        }
    }

}

private fun KSFunctionDeclaration.getMethodBodyIos(className: String): Pair<String, String?> {
    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "" to null
        type.startsWith(PREFIX_TASK) -> {
            val res = getResultIos()
            """
            |if (manager == nil) {
            |            reject("${simpleName.asString()} error", "${className}Manager was not initialized", "${className}Manager was not initialized")
            |        } else {
            |            manager!.${simpleName.asString()}(${getParametersIos()})
            |                    .onSuccess { result in
            |                        guard let res = result else {
            |                            return
            |                        }
            |                        resolve(${res.first})
            |                    }
            |                    .onFailure { KotlinThrowable in
            |                        reject("${simpleName.asString()} error", "${simpleName.asString()} error", KotlinThrowable.asError())
            |                    }
            |        }
            """.trimMargin() to res.second
        }
        else -> {
            """
            |if (manager == nil) {
            |            reject("${simpleName.asString()} error", "${className}Manager was not initialized", "${className}Manager was not initialized")
            |        } else {
            |            resolve(manager!.${simpleName.asString()}(${getParametersIos()}))
            |        }
            """.trimMargin() to null
        }
    }

}

private fun KSFunctionDeclaration.getMethodBodyJs(className: String): Triple<String, String?, Set<String>> {
    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> Triple("void", null, emptySet())
//        type.startsWith(PREFIX_TASK) -> {
        else -> {
            val res = getResultJs()
            val dataString = if (type == "Unit") "" else "data: string"
            val resolveString = if (type == "Unit") "" else res.first


            val params = getParametersJs()
            Triple(
                """
            |    $className.${simpleName.asString()}(${params.first})
            |      .then(($dataString) => {
            |        resolve(${resolveString});
            |      })
            |      .catch((e: any) => {
            |        reject(e);
            |      });
            """.trimMargin(), res.second, params.second
            )
        }
    }

}

private fun KSTypeReference.getDefault(nullability: Nullability): String {
    return when {
        nullability == Nullability.NULLABLE -> "null"
        "$this" == "Boolean" -> "false"
        "$this" == "Int" -> "0"
        "$this" == "Long" -> "0"
        "$this" == "Float" -> "0"
        else -> ""
    }
}

private fun KSFunctionDeclaration.getTypedParametersAndroid(): String {
    return parameters.mapNotNull { p ->
        val t = p.type.resolve()
        val nullabilityString = when (t.nullability) {
            Nullability.NULLABLE -> "?"
            else -> ""
        }
        val defaultString = if (p.hasDefault) " = ${p.type.getDefault(t.nullability)}" else ""
        p.name?.let { name -> "${name.asString()}: ${p.type.kotlinType().first}$nullabilityString$defaultString" }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getTypedParametersIos(): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name -> "${name.asString()}: ${p.type.swiftType().first}" }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getTypedParametersJs(): Pair<String, HashSet<String>> {
    val paramClasses = hashSetOf<String>()
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val type = p.type.jsType(p.hasDefault)
            if (type.startsWith("typeof ")) {
                paramClasses.add(type.removePrefix("typeof "))
            }
            "${name.asString()}: $type"
        }
    }.joinToString(",") to paramClasses
}

private fun KSFunctionDeclaration.getTypedHeaderParametersIos(): String {
    val params = parameters.mapIndexedNotNull { index, p ->
        if (index == 0) simpleName.asString()
        else
            p.name?.asString()
    }.joinToString(":")
    return if (params.isBlank())
        "${simpleName.asString()}:withRejecter:"
    else
        params.plus(":withResolver:withRejecter:")
}

private fun KSFunctionDeclaration.getTypedHeaderParametersIosOC(): String {
    val params = parameters.mapIndexedNotNull { index, p ->
        val param = "(${p.type.objectiveCType()})${p.name?.asString()}"
        if (index == 0)
            "${simpleName.asString()}:$param"
        else
            """
            |
            |                 ${p.name?.asString()}:$param
            """.trimMargin()
    }.joinToString("")
    return if (params.isBlank()) {
        "${simpleName.asString()}:"
            .plus(
                """
                |
                |                 (RCTPromiseResolveBlock)resolve
                |                 withRejecter:(RCTPromiseRejectBlock)reject
                """.trimMargin()
            )
    } else {
        params.plus(
            """
            |
            |                 withResolver:(RCTPromiseResolveBlock)resolve
            |                 withRejecter:(RCTPromiseRejectBlock)reject
            """.trimMargin()
        )
    }

}

private fun KSFunctionDeclaration.getParametersAndroid(): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val kotlinType = p.type.kotlinType()
            val paramsAndroid =
                if (kotlinType.second) "${p.type}.fromJson(${name.asString()})" else p.name?.asString()
            paramsAndroid
        }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getParametersIos(): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val iosType = p.type.swiftType()
            val paramIos = when (iosType.first) {
                "Bool" -> "${name.asString()} ? true : false"
                else -> if (iosType.second) "${p.type}.companion.fromJson(json: ${name.asString()})" else name.asString()
            }
            "${name.asString()}: $paramIos"
        }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getParametersJs(): Pair<String, Set<String>> {
    val paramClasses = hashSetOf<String>()
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val jsType = p.type.jsType(p.hasDefault)
            val param = if (jsType.startsWith("typeof")) {
                paramClasses.add(jsType.removePrefix("typeof "))
                "${p.type}ToJson(${name.asString()})"
            } else
                name.asString()
            param
        }
    }.joinToString(", ") to paramClasses
}