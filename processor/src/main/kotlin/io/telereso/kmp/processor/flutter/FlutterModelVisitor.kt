package io.telereso.kmp.processor.flutter

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.processor.camelToSnakeCase
import java.io.OutputStream

class FlutterModelVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() { //1

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        val modelPackage = "models"
        val className = classDeclaration.simpleName.getShortName()
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()
        val imports = mutableListOf<String>()
        classDeclaration.getAllProperties().forEach {
            if (!it.type.isFlutterType())
                imports.add(
                    "import 'package:flutter_food_client/$modelPackage/${
                        it.simpleName.asString().camelToSnakeCase()
                    }.dart';"
                )
        }
        val importsString = imports.joinToString("\n")

        val constructorMembers =
            classDeclaration.getAllProperties().map { "this.${it.simpleName.asString()}" }
                .joinToString(",")
        val members = classDeclaration.getAllProperties()
            .map {
                "  @JsonKey(name: \"${
                    it.getJsonKey()
                }\")\n  ${it.type.flutterType()}? ${it.simpleName.asString()};"
            }.joinToString("\n\n")


//        val arguments = classDeclaration.annotations.iterator().next().arguments
//        val annotatedParameter = arguments[0].value as KSType //2
//        val parcelKey = arguments[1].value as String //3

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "flutter/$modelPackage",
            fileName = className.camelToSnakeCase(),
            extensionName = "dart"
        )
        val classVariable = "\$${className}"

        outputStream.write(
            """
import 'package:json_annotation/json_annotation.dart';
$importsString

part '$snakeClassName.g.dart';

@JsonSerializable(explicitToJson: true)
class $className {
  $className($constructorMembers);

$members

  factory ${className}.fromJson(Map<String, dynamic> json) => _${classVariable}FromJson(json);

  Map<String, dynamic> toJson() => _${classVariable}ToJson(this);
}
    """.trimMargin().toByteArray()
        )
    }

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

