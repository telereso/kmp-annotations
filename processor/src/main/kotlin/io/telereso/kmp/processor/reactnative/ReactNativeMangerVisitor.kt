package io.telereso.kmp.processor.reactnative

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.camelToSnakeCase
import io.telereso.kmp.processor.iosType
import java.io.OutputStream

class ReactNativeMangerVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() { //1

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {

        createAndroidManager(classDeclaration)
        createIosManager(classDeclaration)
    }

    private fun createAndroidManager(classDeclaration: KSClassDeclaration) {

        val packageString = packageName ?: classDeclaration.packageName.asString()
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

        val skipFunctions = listOf("equals", "hashCode", "toString")

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyAndroid().second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                "  @ReactMethod\n" +
                        "  fun ${it.simpleName.asString()}(${
                            it.getTypedParametersAndroid()
                                .let { params -> if (params.isNotBlank()) "$params, " else params }
                        }promise: Promise) {\n" +
                        it.getMethodBodyAndroid().first +
                        "   }"
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }

        outputStream.write(
            """
            |package $packageString.rn
            |
            |import $packageString.DatabaseDriverFactory
            |$modelImportString
            |import $packageString.models.toJson
            |import $originalClassName
            |import com.facebook.react.bridge.*
            |import org.json.JSONArray
            |import org.json.JSONObject
            |import kotlin.js.ExperimentalJsExport
            | 
            |
            |@OptIn(ExperimentalJsExport::class)
            |class ${className}Module(reactContext: ReactApplicationContext) :
            |  ReactContextBaseJavaModule(reactContext) {
            | 
            |  private val manager = $originalClassName.Builder(DatabaseDriverFactory(reactApplicationContext)).build()
            |
            | override fun getName(): String {
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

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "ios",
            fileName = "$className",
            extensionName = "swift"
        )

        val skipFunctions = listOf("equals", "hashCode", "toString")

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                it.getMethodBodyAndroid().second
            } else null
        }

        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !skipFunctions.contains(it.simpleName.asString())) {
                """
                |    @objc(${
                    it.getTypedHeaderParametersIos()
                        .let { params -> params.ifBlank { it.simpleName.asString() } }
                }:withResolver:withRejecter:)
                |    func ${it.simpleName.asString()}(${
                    it.getTypedParametersIos()
                        .let { params -> if (params.isNotBlank()) "$params, " else "_" }
                } resolve: @escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
                |        ${it.getMethodBodyIos().first}
                |    }
                """.trimMargin()
            } else
                null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }

        outputStream.write(
            """
            |import shared
            |
            |@objc($className)
            |class $className: NSObject {
            |    let manager = ${className}Manager.Builder(databaseDriverFactory: DatabaseDriverFactory()).build()
            |
            |    @objc(hi:withRejecter:)
            |    func hi(_ resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
            |      resolve("Hello World!")
            |    }
            |
            |${methods.joinToString("\n\n")}
            |
            |}
            """.trimMargin().toByteArray()
        )
    }

}

const val PREFIX_TASK = "Task<"
const val PREFIX_TASK_ARRAY = "Task<Array<"
val REGEX_TASK = Regex("(?<=Task<)(.*?)(?=>)")
val REGEX_TASK_ARRAY = Regex("(?<=Task<Array<)(.*?)(?=>)")

private fun KSFunctionDeclaration.getResultAndroid(): Pair<String, String?> {

    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "it" to null
        type.startsWith(PREFIX_TASK_ARRAY) -> {
            val klass = REGEX_TASK_ARRAY.find(type)?.value
            "${klass}.toJson(it)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            "it.toJson()" to null
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
            "${klass}.companion.toJson(array: res)" to klass
        }
        type.startsWith(PREFIX_TASK) -> {
            "res.companion.toJson()" to null
        }
        else -> {
            "res" to null
        }
    }
}

private fun KSFunctionDeclaration.getMethodBodyAndroid(): Pair<String, String?> {
    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "" to null
        type.startsWith(PREFIX_TASK) -> {
            val res = getResultAndroid()
            "       manager.${simpleName.asString()}(${getParametersAndroid()}).onSuccess {\n" +
                    "           promise.resolve(${res.first})\n" +
                    "       }.onFailure {\n" +
                    "           promise.reject(it)\n" +
                    "       }\n" to res.second
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

private fun KSFunctionDeclaration.getMethodBodyIos(): Pair<String, String?> {
    val type = returnType?.resolve()?.toString()
    return when {
        type == null -> "" to null
        type.startsWith(PREFIX_TASK) -> {
            val res = getResultIos()
            """
            |        manager.${simpleName.asString()}(${getTypedParametersIos()})
            |              .onSuccess { result in
            |                    guard let res = result else {return}
            |                    resolve(${res.first})
            |              }.onFailure { KotlinThrowable in
            |                    reject("${simpleName.asString()} error", "${simpleName.asString()} error", KotlinThrowable.asError())
            |              }
            """.trimMargin() to res.second
        }
        else -> {
            """
            |do {
            |           try resolve(manager.${simpleName.asString()}(${getParametersIos()}))
            |        } catch {
            |           reject.reject("${simpleName.asString()} error", "${simpleName.asString()} error","\(error)")
            |        }
            |       
            """.trimMargin() to null
        }
    }

}

private fun KSFunctionDeclaration.getTypedParametersAndroid(): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name -> "${name.asString()}: ${p.type}" }
    }.joinToString(",")
}

private fun KSFunctionDeclaration.getTypedParametersIos(): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name -> "${name.asString()}: ${p.type.iosType()}" }
    }.joinToString(",")
}

private fun KSFunctionDeclaration.getTypedHeaderParametersIos(): String {
    return parameters.mapIndexedNotNull { index, p ->
        if (index == 0) simpleName.asString()
        else
            p.name?.asString()
    }.joinToString(":")
}

private fun KSFunctionDeclaration.getParametersAndroid(): String {
    return parameters.mapNotNull { p -> p.name?.asString() }.joinToString(",")
}

private fun KSFunctionDeclaration.getParametersIos(): String {
    return parameters.mapNotNull { p ->
        p.name?.let { name ->
            "${name.asString()}: ${name.asString()}"
        }
    }.joinToString(", ")
}