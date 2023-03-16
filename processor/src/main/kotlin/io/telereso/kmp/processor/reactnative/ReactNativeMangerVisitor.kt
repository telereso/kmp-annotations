/*
 * MIT License
 *
 * Copyright (c) 2023 Telereso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.telereso.kmp.processor.reactnative

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.*
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


const val CLASS_COMMON_FLOW = "CommonFlow"

class ReactNativeMangerVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    private val scope: String? = null,
    private val packageName: String? = null
) : KSVisitorVoid() {
    private val skipFunctions = listOf("equals", "hashCode", "toString")
    private val skipClasses = listOf("CommonFlow","String","Boolean","Int" , "Unit" )

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

        val enums = hashSetOf<String>()

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyAndroid(enums).second
            } else null
        }


        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                """  
                |  @ReactMethod
                |  fun ${it.simpleName.asString()}(${
                    it.getTypedParametersAndroid(enums)
                        .let { params -> if (params.isNotBlank()) "$params, " else params }
                }promise: Promise) {
                |${it.getMethodBodyAndroid(enums).first}
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

        val enums = HashMap<String, List<String>>()

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyIos(className,enums).second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                """
                |    @objc(${
                    it.getTypedHeaderParametersIos()
                })
                |    func ${it.simpleName.asString()}(${
                    it.getTypedParametersIos(enums)
                        .let { params -> if (params.isNotBlank()) "$params, " else "_ " }
                }resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
                |        ${it.getMethodBodyIos(className,enums).first}
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

        val enums = hashMapOf<String, List<String>>()

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyIos(className, enums).second
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

        val enums = hashMapOf<String, List<String>>()

        classDeclaration.getAllFunctions().forEach {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                val typedParams = it.getTypedParametersJs(enums).second
                it.getMethodBodyJs(className, enums).let { trip ->
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
            modelImports
                .filter { m -> !skipClasses.any { s -> m.startsWith(s,true) } }
                .map { it.removeSuffix("?") }
                .distinct()
                .joinToString("\n") {
                """
                const $it = ${modelClassName}.${it};
                """.trimIndent()
            }

        val modelJsonImportString =
            modelFromJsonImports
                .filter { m -> !skipClasses.any { s -> m.startsWith(s,true) } }
                .map { it.removeSuffix("?") }
                .distinct()
                .joinToString("\n") {
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
                val res = it.getMethodBodyJs(className, enums)
                val typedParams = it.getTypedParametersJs(enums)
                res.second?.let { klass: String ->
                    modelImports.add(klass)
                }
                modelImports.addAll(typedParams.second)

                """
                |export function ${it.simpleName.asString()}(${typedParams.first}): Promise<${res.second.jsType()}> {
                |  return new Promise<${res.second.jsType()}>((resolve, reject) => {
                |${res.first}
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
            |const ${projectName.snakeToUpperCamelCase()}Models = require('${scope?.let { "@$it/" } ?: ""}$projectName-models').${modelPackage};
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
val REGEX_COMMON_FLOW = Regex("(?<=$CLASS_COMMON_FLOW<)(.*?)(?=>)")

private fun KSFunctionDeclaration.getResultAndroid(enums: HashSet<String>): Pair<String, String?> {


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
            val commonFlowClass = REGEX_COMMON_FLOW.find(typeString)?.value

            val nullabilityString = if (klass?.endsWith("?") == true) "?" else ""
//            val nullabilityString = when (type.nullability) {
//                Nullability.NULLABLE -> "?"
//                else -> ""
//            }
            var res = "it"

            when {
                klass == null -> {}
                klass == "Unit" -> {
                    res = "\"\""
                }
                !commonFlowClass.isNullOrEmpty() -> {}
                enums.contains(klass) -> {}
                !klass.isPrimitiveKotlin() -> {
                    res = "${res}${nullabilityString}.toJson()"
                }
            }

            res to null
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
        type == "Unit" -> "\"\"" to null
        type.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(type)?.value
            "${klass}.Companion().toJson(array: res)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            val klass = REGEX_TASK.find(type)?.value
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value
            when {
                commonFlowClass != null || klass == null || klass == "Unit"-> "\"\"" to null
                klass in listOf("String", "Long", "Int", "Boolean") -> "res" to null
                else -> {
                    "res.toJson()" to null
                }
            }
        }
        else -> {
            "res" to null
        }
    }
}

private fun KSFunctionDeclaration.getResultJs(): Pair<String, String?> {

    val type = returnType?.resolve()?.toString()
    return when {
        type == null || type ==  "Unit" -> "" to null
        type.startsWith("String") -> "data" to "String"
        type.startsWith("Boolean") -> "data" to "boolean"
        type.startsWith("Int") -> "data" to "number"
        type.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(type)?.value?.jsType()?.removePrefix("typeof ")
            "${klass}FromJsonArray(${klass}.Companion, data)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            var klass = REGEX_TASK.find(type)?.value?.jsType()?.removePrefix("typeof ")
            val commonFlowClass = REGEX_COMMON_FLOW.find(type)?.value

            if (commonFlowClass != null)
                klass = commonFlowClass.removeSuffix("?")

            when (klass) {
                "Unit", "unit",  -> "" to null
                "string" , "boolean" , "number" -> "data" to klass
                else -> {
                    "${klass}FromJson(${klass}.Companion, data)" to klass
                }
            }

        }
        else -> {
            "data" to null
        }
    }
}

private fun KSFunctionDeclaration.getMethodBodyAndroid(enums: HashSet<String>): Pair<String, String?> {
    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "" to null
        type.startsWith(PREFIX_TASK) -> {
            val res = getResultAndroid(enums)
            """       
            |    manager.${simpleName.asString()}(${getParametersAndroid(enums)}).onSuccess {
            |      promise.resolve(${res.first})
            |    }.onFailure {
            |      promise.reject(it)
            |    }
           """.trimMargin() to res.second
        }
        else -> {
            "       try {\n" +
                    "          promise.resolve(manager.${simpleName.asString()}(${getParametersAndroid(enums)})) \n" +
                    "       } catch (e:Exception){\n" +
                    "          promise.reject(e)\n" +
                    "       }\n" to null
        }
    }

}

private fun KSFunctionDeclaration.getMethodBodyIos(
    className: String,
    enums: HashMap<String, List<String>>
): Pair<String, String?> {
    val type = returnType?.resolve()?.toString()
    val paramsRes = getParametersIos(enums)
    return when {
        type == null -> "" to null
        type.startsWith(PREFIX_TASK) -> {
            val res = getResultIos()
            """
            |if (manager == nil) {
            |            reject("${simpleName.asString()} error", "${className}Manager was not initialized", "${className}Manager was not initialized")
            |        } else {
            |            ${paramsRes.second}
            |            manager!.${simpleName.asString()}(${paramsRes.first})
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
            |            ${paramsRes.second}
            |            resolve(manager!.${simpleName.asString()}(${paramsRes.first}))
            |        }
            """.trimMargin() to null
        }
    }

}

private fun KSFunctionDeclaration.getMethodBodyJs(className: String, enums: HashMap<String, List<String>>): Triple<String, String?, Set<String>> {
    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> Triple("void", null, emptySet())
//        type.startsWith(PREFIX_TASK) -> {
//        type.startsWith(CLASS_COMMON_FLOW) -> Triple("void", null, emptySet())
        else -> {
            val res = getResultJs()
            val dataString = when (type){
                "Unit", "Task<Unit>" -> ""
                "Boolean", "Task<Boolean>" -> "data: boolean"
                "Int", "Task<Int>" -> "data: number"
                else -> "data: string"
            }
            val resolveString = if (type == "Unit") "" else res.first


            val params = getParametersJs(enums)
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

private fun KSFunctionDeclaration.getTypedParametersAndroid(enums: HashSet<String>): String {
    return parameters.mapNotNull { p ->
        val t = p.type.resolve()
        val nullabilityString = when (t.nullability) {
            Nullability.NULLABLE -> "?"
            else -> ""
        }

        val paramClass = t.declaration.closestClassDeclaration()
        if (paramClass?.classKind == ClassKind.ENUM_CLASS)
            enums.add(paramClass.simpleName.asString())

        val defaultString = if (p.hasDefault) " = ${p.type.getDefault(t.nullability)}" else ""
        p.name?.let { name -> "${name.asString()}: ${p.type.kotlinType().first}$nullabilityString$defaultString" }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getTypedParametersIos(enums: HashMap<String,List<String>>): String {
    return parameters.mapNotNull { p ->
        val t = p.type.resolve()
        val paramClass = t.declaration.closestClassDeclaration()
        if (paramClass?.classKind == ClassKind.ENUM_CLASS){
            enums[paramClass.simpleName.asString()] =
                paramClass.getEnumEntries().map { it.simpleName.asString() }.toList()
        }

        p.name?.let { name -> "${name.asString()}: ${p.type.swiftType().first}" }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getTypedParametersJs(enums: HashMap<String, List<String>>): Pair<String, HashSet<String>> {
    val paramClasses = hashSetOf<String>()
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val type = p.type.jsType(p.hasDefault)
            val t = p.type.resolve()
            val paramClass = t.declaration.closestClassDeclaration()
            if (paramClass?.classKind == ClassKind.ENUM_CLASS){
                enums[paramClass.simpleName.asString()] =
                    paramClass.getEnumEntries().map { it.simpleName.asString() }.toList()
            }
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

private fun KSFunctionDeclaration.getParametersAndroid(enums: HashSet<String>): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val type = p.type.resolve()
            val kotlinType = p.type.kotlinType()
            when {
                kotlinType.second && enums.contains(p.type.toString()) ->{
                    val res = "${p.type}.valueOf(${name.asString()})"

                    if(p.hasDefault && type.isMarkedNullable){
                        "${name.asString()}?.let { $res }"
                    }else {
                        res
                    }
                }
                kotlinType.second ->{
                    "${p.type}.fromJson(${name.asString()})"
                }
                else -> {
                    p.name?.asString()
                }
            }
        }
    }.joinToString(", ")
}

private fun KSFunctionDeclaration.getParametersIos(enums: HashMap<String, List<String>>): Pair<String, String> {
    val enumSwitch = StringBuilder()
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val iosType = p.type.swiftType()
            val typeString = p.type.toString()
            val paramIos = when  {
                iosType.second && enums.keys.contains(typeString) -> {
                    val enumValue = "${name.asString()}Value"
                    val enumEntries = enums[typeString] ?: emptyList()
                    enumSwitch.appendLine("""
                        |let $enumValue: ${p.type}
                        |            switch ${name.asString()} {
                    """.trimMargin())

                    enumEntries.forEach { e->
                        enumSwitch.appendLine("""
                        |            case "$e":
                        |                $enumValue = ${p.type}.${e.lowercase().snakeToLowerCamelCase()}
                    """.trimMargin())
                    }

                    enumSwitch.appendLine("""
                        |            default:
                        |                $enumValue = ${p.type}.${enumEntries.first().lowercase().snakeToLowerCamelCase()}
                        |            }
                    """.trimMargin())

                    enumValue
                }
                iosType.second -> {
                    "${p.type}.companion.fromJson(json: ${name.asString()})"
                }
                iosType.first == "Bool" -> "${name.asString()} ? true : false"
                else -> name.asString()
            }
            "${name.asString()}: $paramIos"
        }
    }.joinToString(", ") to enumSwitch.toString()
}

private fun KSFunctionDeclaration.getParametersJs(enums: HashMap<String, List<String>>): Pair<String, Set<String>> {
    val paramClasses = hashSetOf<String>()
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            val typeString = p.type.toString()
            val jsType = p.type.jsType(p.hasDefault)
            when{
                enums.keys.contains(typeString) -> {
                    "${typeString.replaceFirstChar { it.lowercase(Locale.getDefault()) }}.name"
                }
                jsType.startsWith("typeof") -> {
                    paramClasses.add(jsType.removePrefix("typeof "))
                    "${p.type}ToJson(${name.asString()})"
                }
                else -> {
                    name.asString()
                }
            }
        }
    }.joinToString(", ") to paramClasses
}