package io.telereso.kmp.processor.model

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.camelToSnakeCase
import java.io.OutputStream

class ModelVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() {

    var shouldGenerateUtilFile = true

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.getShortName()
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "kotlin.$packageString",
            fileName = className,
            extensionName = "g.kt"
        )
        val filePackageString = packageString.let {
            if (it.isBlank()) "" else "package $it"
        }

        val importClassString = filePackageString.let {
            if (it.isBlank()) "" else "import $className"
        }

        outputStream.write(
            """
            |$filePackageString
            |
            |// $importClassString
            |import kotlinx.serialization.builtins.ListSerializer
            |import kotlinx.serialization.decodeFromString
            |import kotlinx.serialization.json.Json
            |import kotlin.js.JsExport
            |import kotlin.js.JsName
            |
            |@JsExport()
            |@JsName("${className}ToJson")
            |fun $className.toJson(): String {
            |   return jsonSerializer.encodeToString($className.serializer(), this)
            |}
            |
            |fun $className.toJsonPretty(): String {
            |   return jsonPrettySerializer.encodeToString($className.serializer(), this)
            |}
            |
            |@JsExport
            |@JsName("${className}FromJson")
            |fun $className.Companion.fromJson(json:String): $className{
            |   return jsonSerializer.decodeFromString(json)
            |}
            |
            |@JsExport
            |@JsName("${className}ToJsonArray")
            |fun $className.Companion.toJson(array: Array<$className>): String {
            |   return jsonSerializer.encodeToString(ListSerializer($className.serializer()), array.toList())
            |}
            |
            |@JsExport
            |@JsName("${className}FromJsonArray")
            |fun $className.Companion.fromJsonArray(json:String): Array<$className> {
            |   return jsonSerializer.decodeFromString(ListSerializer($className.serializer()), json).toTypedArray()
            |}
            |
            |
            """.trimMargin().toByteArray()
        )

        if(shouldGenerateUtilFile){
            generateUtilFile(codeGenerator,packageString,filePackageString)
        }
    }

}

private fun generateUtilFile(
    codeGenerator: CodeGenerator,
    packageString: String,
    filePackageString: String
) {
    val outputStream: OutputStream = codeGenerator.createNewFile(
        dependencies = Dependencies(false),
        "kotlin.$packageString",
        fileName = "Utils",
        extensionName = "g.kt"
    )
    outputStream.write(
        """
            |$filePackageString
            |
            |import kotlinx.serialization.json.Json
            |
            |val jsonSerializer = Json {
            |        prettyPrint = false
            |        isLenient = true
            |        ignoreUnknownKeys = true
            |}
            |
            |val jsonPrettySerializer = Json {
            |        prettyPrint = true
            |        isLenient = true
            |        ignoreUnknownKeys = true
            |}
            |
            """.trimMargin().toByteArray()
    )
}

private fun KSPropertyDeclaration.getJsonKey(): String {
    return annotations.find { it.toString() == "@SerialName" }
        ?.arguments?.find { it.name?.asString() == "value" }
        ?.value?.toString()
        ?: simpleName.asString()
}

private fun KSTypeReference.isFlutterType(): Boolean {
    this.resolve()
    return when (this.flutterType()) {
        "Object", "double", "String", "bool", "int" -> true
        else -> false
    }
}

private fun KSTypeReference.flutterType(): String {
    this.resolve()
    return when ("$this") {
        "Int" -> "int"
        "Boolean" -> "bool"
        else -> this.toString()
    }
}

