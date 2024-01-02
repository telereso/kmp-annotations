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

package io.telereso.kmp.processor.flutter

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.annotations.SkipFlutterExport
import io.telereso.kmp.processor.camelToSnakeCase
import io.telereso.kmp.processor.getArrayFlowName
import io.telereso.kmp.processor.getCommonFlowListClass
import io.telereso.kmp.processor.getEnumEntries
import io.telereso.kmp.processor.getListFlowName
import io.telereso.kmp.processor.skipFunctions
import java.io.OutputStream

class FlutterModelVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val groupId: String? = null,
    val packageName: String? = null
) : KSVisitorVoid() { //1

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        if (classDeclaration.annotations.any { it.toString() == "@Serializable" }) {
            createModel(classDeclaration)
        } else {
            createAndroidManager(classDeclaration)
        }
    }

    private fun createAndroidManager(classDeclaration: KSClassDeclaration) {

        val packageString = packageName ?: classDeclaration.packageName.asString()
        val modelsPackageString = packageString.removeSuffix(".client").plus(".models")
        val originalClassName = classDeclaration.simpleName.getShortName()

        logger.logging("Create Flutter Android Manager for $originalClassName")


        val className = originalClassName.removeSuffix("Manager")
        if (className.isEmpty()) return

        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()
        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            "flutter-kotlin.${groupId}.flutter_${snakeClassName}",
            fileName = "Flutter${className}Plugin",
            extensionName = "g.kt"
        )

        val enums = hashSetOf<String>()
        val functionsImports = mutableListOf<String>()

        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !it.skipFlutter() && !skipFunctions.contains(
                    it.simpleName.asString()
                )
            ) {
                it.getCommonFlowListClass()?.let { _ ->
                    functionsImports.add(it.getListFlowName())
                    functionsImports.add(it.getArrayFlowName())
                }

            } else null
        }


        val methods = classDeclaration.getAllFunctions().mapNotNull {
            if (it.getVisibility() == Visibility.PUBLIC && !it.skipFlutter() && !skipFunctions.contains(
                    it.simpleName.asString()
                )
            ) {
                """  
                   
                    """.trimMargin()
            } else null
        }

        val modelImportString =
            modelImports.joinToString("\n") { "import ${modelsPackageString}.models.$it" }

        val importExtensionFunctions = functionsImports.joinToString("\n") {
            "import $packageString.$it"
        }

        outputStream.write(
            """
                |package $groupId.flutter_${snakeClassName}
                |
                |import $packageString.$originalClassName
                |import $modelsPackageString.*
                |$importExtensionFunctions
                |import io.flutter.embedding.engine.plugins.FlutterPlugin
                |import io.flutter.plugin.common.MethodCall
                |import io.flutter.plugin.common.MethodChannel
                |import io.flutter.plugin.common.MethodChannel.MethodCallHandler
                |import io.flutter.plugin.common.MethodChannel.Result
                |import io.telereso.kmp.core.Task
                |
                |
                |class Flutter${className}Plugin: FlutterPlugin, MethodCallHandler {
                |  private lateinit var channel : MethodChannel
                |
                |  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
                |    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_${className}")
                |    channel.setMethodCallHandler(this)
                |  }
                |
                |  override fun onMethodCall(call: MethodCall, result: Result) {
                |    if (call.method == "getPlatformVersion") {
                |      result.success("Android")
                |    } else {
                |      result.notImplemented()
                |    }
                |  }
                |
                |  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
                |    channel.setMethodCallHandler(null)
                |  }
                |}
                |
                """.trimMargin().toByteArray()
        )
    }
    //
    //    private fun createIosManager(classDeclaration: KSClassDeclaration) {
    //        val packageString = packageName ?: classDeclaration.packageName.asString()
    //        val originalClassName = classDeclaration.simpleName.getShortName()
    //        val className = originalClassName.removeSuffix("Manager")
    //        if (className.isEmpty()) return
    //
    //        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
    //        val snakeClassName = className.camelToSnakeCase()
    //
    //        logger.logging("Create iOS Manager for $originalClassName")
    //
    //        val outputStream: OutputStream = codeGenerator.createNewFile(
    //            dependencies = Dependencies(false),
    //            "ios",
    //            fileName = className,
    //            extensionName = "swift"
    //        )
    //
    //        val enums = HashMap<String, List<String>>()
    //
    //        val flowEvents = mutableListOf<String>()
    //
    //        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
    //            if (it.getVisibility() == Visibility.PUBLIC
    //                && !it.skipReactNative()
    //                && !skipFunctions.contains(it.simpleName.asString())) {
    //                val res = it.getMethodBodyIos(className, enums)
    //                flowEvents.addAll(res.third)
    //                res.second
    //            } else null
    //        }
    //
    //        val methods = classDeclaration.getAllFunctions().mapNotNull {
    //            if (it.getVisibility() == Visibility.PUBLIC
    //                && !it.skipReactNative()
    //                && !skipFunctions.contains(it.simpleName.asString())) {
    //                """
    //                |    @objc(${
    //                    it.getTypedHeaderParametersIos()
    //                })
    //                |    func ${it.simpleName.asString()}(${
    //                    it.getTypedParametersIos(enums)
    //                        .let { params -> if (params.isNotBlank()) "$params, " else "_ " }
    //                }resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    //                |        ${it.getMethodBodyIos(className,enums).first}
    //                |    }
    //                """.trimMargin()
    //            } else
    //                null
    //        }
    //
    //        val modelImportString =
    //            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }
    //
    //        outputStream.write(
    //            """
    //            |import $className
    //            |
    //            |extension String: Error {
    //            |}
    //            |
    //            |
    //            |@objc($className)
    //            |class $className: RCTEventEmitter {
    //            |
    //            |    private var hasListeners = false;
    //            |
    //            |    override func supportedEvents() -> [String]! {
    //            |        return [${flowEvents.joinToString(",") { "\"$it\"" }}]
    //            |    }
    //            |
    //            |    override func startObserving() {
    //            |        hasListeners = true
    //            |    }
    //            |
    //            |    override func stopObserving() {
    //            |        hasListeners = false
    //            |    }
    //            |
    //            |    override func sendEvent(withName name: String!, body: Any!) {
    //            |        if (hasListeners) {
    //            |            super.sendEvent(withName: name, body: body)
    //            |        }
    //            |    }
    //            |
    //            |${methods.joinToString("\n\n")}
    //            |
    //            |}
    //            """.trimMargin().toByteArray()
    //        )
    //
    //        createIosManagerHeader(classDeclaration)
    //    }
    //
    //    private fun createIosManagerHeader(classDeclaration: KSClassDeclaration) {
    //        val packageString = packageName ?: classDeclaration.packageName.asString()
    //        val originalClassName = classDeclaration.simpleName.getShortName()
    //        val className = originalClassName.removeSuffix("Manager")
    //        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
    //        val snakeClassName = className.camelToSnakeCase()
    //
    //        logger.logging("Create iOS Manager Header for $originalClassName")
    //
    //        val outputStream: OutputStream = codeGenerator.createNewFile(
    //            dependencies = Dependencies(false),
    //            "ios",
    //            fileName = className,
    //            extensionName = "m"
    //        )
    //
    //        val enums = hashMapOf<String, List<String>>()
    //
    //        val modelImports = classDeclaration.getAllFunctions().mapNotNull {
    //            if (it.getVisibility() == Visibility.PUBLIC
    //                && !it.skipReactNative()
    //                && !skipFunctions.contains(it.simpleName.asString())) {
    //                it.getMethodBodyIos(className, enums).second
    //            } else null
    //        }
    //
    //        val methods = classDeclaration.getAllFunctions().mapNotNull {
    //            if (it.getVisibility() == Visibility.PUBLIC
    //                && !it.skipReactNative()
    //                && !skipFunctions.contains(it.simpleName.asString())) {
    //                """
    //                |RCT_EXTERN_METHOD(${it.getTypedHeaderParametersIosOC()})
    //                """.trimMargin()
    //            } else
    //                null
    //        }
    //
    //        val modelImportString =
    //            modelImports.joinToString("\n") { "import ${packageString}.models.$it" }
    //
    //        outputStream.write(
    //            """
    //            |#import <React/RCTBridgeModule.h>
    //            |#import <React/RCTEventEmitter.h>
    //            |
    //            |@interface RCT_EXTERN_MODULE($className, RCTEventEmitter)
    //            |
    //            |RCT_EXTERN_METHOD(supportedEvents)
    //            |
    //            |${methods.joinToString("\n\n")}
    //            |
    //            |+ (BOOL)requiresMainQueueSetup
    //            |{
    //            |  return NO;
    //            |}
    //            |
    //            |@end
    //            |
    //            """.trimMargin().toByteArray()
    //        )
    //    }
    //
    //    private fun createReactNativeIndex(classDeclaration: KSClassDeclaration) {
    //        val packageString = packageName ?: classDeclaration.packageName.asString()
    //        val originalClassName = classDeclaration.simpleName.getShortName()
    //        val className = originalClassName.removeSuffix("Manager")
    //        if (className.isEmpty()) return
    //
    //        val projectName = className.removeSuffix("Client").lowercase()
    //        val modelPackage = packageString.removeSuffix(".client").plus(".models")
    //        val modelClassName = projectName.snakeToUpperCamelCase().plus("Models")
    //        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
    //        val snakeClassName = className.camelToSnakeCase()
    //
    //        logger.logging("Create JS Manager for $originalClassName")
    //
    //        val outputStream: OutputStream = codeGenerator.createNewFile(
    //            dependencies = Dependencies(false),
    //            "js",
    //            fileName = "index",
    //            extensionName = "tsx"
    //        )
    //
    //        val modelImports = hashSetOf<String>()
    //        val modelFromJsonImports = hashSetOf<String>()
    //        val modelToJsonImports = hashSetOf<String>()
    //
    //        val enums = hashMapOf<String, List<String>>()
    //
    //        classDeclaration.getAllFunctions().forEach {
    //            if (it.getVisibility() == Visibility.PUBLIC
    //                && !it.skipReactNative()
    //                && !skipFunctions.contains(it.simpleName.asString())) {
    //                val typedParams = it.getTypedParametersJs(enums).second
    //                it.getMethodBodyJs(className, enums).let { trip ->
    //                    trip.second.forEach { promisedKlass ->
    //                        val klass = promisedKlass
    //                            .removePrefix("Promise<")
    //                            .removePrefix("typeof ")
    //                            .removeSuffix(">")
    //                        typedParams.add(klass)
    //                        modelFromJsonImports.add(klass)
    //                    }
    //                    modelToJsonImports.addAll(trip.third)
    //                }
    //                modelImports.addAll(typedParams)
    //
    //                it.getCommonFlowListClass()?.let { c->
    //                    val arrayClassName = "${c}Array"
    //                    modelImports.add(arrayClassName)
    //                    modelFromJsonImports.add(arrayClassName)
    //                }
    //
    //            }
    //        }
    //
    //        val modelImportString =
    //            modelImports
    //                .filter { m -> !skipClasses.any { s -> m.startsWith(s, true) } && !m.contains(".") }
    //                .map { it.removeSuffix("?") }
    //                .distinct()
    //                .joinToString("\n") {
    //                """
    //                const $it = ${modelClassName}.${it};
    //                """.trimIndent()
    //            }
    //
    //        val modelJsonImportString =
    //            modelFromJsonImports
    //                .filter { m -> !skipClasses.any { s -> m.startsWith(s, true) } && !m.contains(".") }
    //                .map { it.removeSuffix("?") }
    //                .distinct()
    //                .joinToString("\n") {
    //                """
    //                // @ts-ignore
    //                // eslint-disable-next-line @typescript-eslint/no-unused-vars
    //                const ${it}FromJson = ${modelClassName}.${it}FromJson;
    //
    //                // @ts-ignore
    //                // eslint-disable-next-line @typescript-eslint/no-unused-vars
    //                const ${it}FromJsonArray = ${modelClassName}.${it}FromJsonArray;
    //                """.trimIndent()
    //            }
    //
    //        val modelToJsonImportsString =
    //            modelToJsonImports.joinToString("\n") {
    //                """
    //                const ${it}ToJson = ${modelClassName}.${it}ToJson;
    //                """.trimIndent()
    //            }
    //
    //        val methods = classDeclaration.getAllFunctions().mapNotNull {
    //            if (it.getVisibility() == Visibility.PUBLIC
    //                && !it.skipReactNative()
    //                && !skipFunctions.contains(it.simpleName.asString())) {
    //                val res = it.getMethodBodyJs(className, enums)
    //                val typedParams = it.getTypedParametersJs(enums)
    //                res.second.forEach { klass: String ->
    //                    modelImports.add(klass)
    //                }
    //                modelImports.addAll(typedParams.second)
    //
    //
    //                """
    //                |export function ${it.simpleName.asString()}(${typedParams.first}): ${res.second.first()} {
    //                |  ${res.first}
    //                |}
    //                |
    //                """.trimMargin()
    //            } else
    //                null
    //        }
    //
    //        outputStream.write(
    //            """
    //            |import { NativeModules, Platform, NativeEventEmitter, $CLASS_EMITTER_SUBSCRIPTION } from 'react-native';
    //            |
    //            |const LINKING_ERROR =
    //            |  `The package 'react-native-${projectName}-client' doesn't seem to be linked. Make sure: \n\n` +
    //            |  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    //            |  '- You rebuilt the app after installing the package\n' +
    //            |  '- You are not using Expo Go\n';
    //            |
    //            |const ${projectName.snakeToUpperCamelCase()}Models = require('${scope?.let { "@$it/" } ?: ""}$projectName-models').${modelPackage};
    //            |
    //            |
    //            |const $className = NativeModules.${className}
    //            |  ? NativeModules.${className}
    //            |  : new Proxy(
    //            |      {},
    //            |      {
    //            |        get() {
    //            |          throw new Error(LINKING_ERROR);
    //            |        },
    //            |      }
    //            |    );
    //            |
    //            |$modelImportString
    //            |$modelJsonImportString
    //            |$modelToJsonImportsString
    //            |
    //            |${methods.joinToString("\n\n")}
    //            |
    //            """.trimMargin().toByteArray()
    //        )
    //    }
    //
        private fun KSFunctionDeclaration.skipFlutter(): Boolean {
            return annotations.any { it.shortName.asString() == SkipFlutterExport::class.simpleName }
        }

    private fun createModel(classDeclaration: KSClassDeclaration) {
        val modelPackage = "models"
        val className = classDeclaration.simpleName.getShortName()
        val packageNameClient = classDeclaration.packageName.asString().replace("models", "client")
        val flutterProjectName = "flutter_".plus(
            packageNameClient.removePrefix(groupId ?: "").removePrefix(".").replace(".", "_")
        )
        val snakeClassName = className.camelToSnakeCase()

        val innerEnums =
            classDeclaration.declarations.filter { it is KSClassDeclaration && it.classKind == ClassKind.ENUM_CLASS }
                .toList()

        val imports = mutableListOf<String>()
        classDeclaration.getAllProperties().forEach {
            if (!innerEnums.contains(it.type.resolve().declaration) && !it.type.isFlutterType()) imports.add(
                "import 'package:$flutterProjectName/$modelPackage/${
                    it.simpleName.asString().camelToSnakeCase()
                }.dart';"
            )
        }
        val importsString = imports.joinToString("\n")

        val constructorMembers =
            classDeclaration.getAllProperties().map { "this.${it.simpleName.asString()}" }
                .joinToString(",")

        val members = classDeclaration.getAllProperties().map {
                "  @JsonKey(name: \"${
                    it.getJsonKey()
                }\")\n  ${it.type.flutterType()}? ${it.simpleName.asString()};"
            }.joinToString("\n\n")

        val innerEnumsString = innerEnums.map { innerEnum ->
            innerEnum as KSClassDeclaration

            "enum ${innerEnum.simpleName.asString()} { ${
                innerEnum.getEnumEntries().joinToString(",") { it.simpleName.asString() }
            } }"
        }.joinToString("\n\n")

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

$innerEnumsString

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

