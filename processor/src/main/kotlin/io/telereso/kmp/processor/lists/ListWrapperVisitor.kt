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

package io.telereso.kmp.processor.lists

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import io.telereso.kmp.annotations.SkipListWrappers
import io.telereso.kmp.processor.*
import io.telereso.kmp.processor.reactnative.getParametersAndroid
import io.telereso.kmp.processor.reactnative.getTypedParametersAndroid
import java.io.OutputStream


class ListWrapperVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    val packageName: String? = null
) : KSVisitorVoid() {

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val classDeclaration = function.closestClassDeclaration()!!
        val packageString = (packageName ?: classDeclaration.packageName.asString())
        val commonFlowListClass =
            REGEX_COMMON_FLOW_LIST.find(function.returnType?.resolve().toString())?.value ?: return

        val enums = hashSetOf<String>()

//        val modelsPackageString = originalPackageString.removeSuffix("client").plus("models")

        createFunctionsWrapper(
            packageString,
            commonFlowListClass,
            function,
            enums
        )
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ) {
        val packageString = packageName ?: classDeclaration.packageName.asString()
        var className = classDeclaration.simpleName.getShortName()
        val memberClassName = className.replaceFirst(className[0], className[0].lowercaseChar())
        val snakeClassName = className.camelToSnakeCase()

        val enums = hashSetOf<String>()


        if (classDeclaration.modifiers.contains(Modifier.DATA)) {
            createModelsWrapper(packageString, className)
        } else {
            className = className.removeSuffix("Manager")
            if (className.isEmpty()) return

            classDeclaration.getAllFunctions().filter {
                it.getVisibility() == Visibility.PUBLIC
                        && !it.skipListWrapper()
                        && !skipFunctions.contains(it.simpleName.asString())
            }.forEach { function ->
                function.getCommonFlowListClass()?.let { c ->
                    createFunctionsWrapper(packageString, c, function, enums)
                }
            }
        }
    }

    private fun createModelsWrapper(
        packageString: String,
        className: String
    ) {
        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "${className}ListWrappers",
            extensionName = "kt"
        )

        outputStream.write(
            """
                |package $packageString
                |import kotlin.js.JsExport
                |import kotlinx.serialization.Serializable
                |
                |@Serializable
                |data class ${className}List(val list: List<$className>)
                |
                |@Serializable
                |@JsExport
                |data class ${className}Array(val array: Array<${className}>) {
                |    override fun equals(other: Any?): Boolean {
                |        if (this === other) return true
                |        if (other == null || this::class != other::class) return false
                |
                |        other as RocketLaunchArray
                |
                |        if (!array.contentEquals(other.array)) return false
                |
                |        return true
                |    }
                |
                |    override fun hashCode(): Int {
                |         return array.contentHashCode()
                |    }
                |}
            """.trimMargin().toByteArray()
        )
    }

    private fun createFunctionsWrapper(
        packageString: String,
        modelClass: String,
        function: KSFunctionDeclaration,
        enums: HashSet<String>
    ) {
        val modelPackageString = packageString.removeSuffix("client").plus("models")
        val originalClassName = function.closestClassDeclaration()

        val outputStream: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageString,
            fileName = "${originalClassName}${
                function.simpleName.asString().snakeToUpperCamelCase()
            }Extension",
            extensionName = "kt"
        )

        val funName = function.simpleName.asString()
        val listFunName = function.getListFlowName()
        val arrayFunName = function.getArrayFlowName()

        val typedParams = function.getTypedParametersAndroid(enums)
        val params = function.getParametersAndroid(enums)

        outputStream.write(
            """
            |package $packageString
            |
            |import io.telereso.kmp.core.CommonFlow
            |import io.telereso.kmp.core.Task
            |import io.telereso.kmp.core.asCommonFlow
            |import kotlinx.coroutines.flow.map
            |
            |import $modelPackageString.${modelClass}List
            |import $modelPackageString.${modelClass}Array
            |
            |fun $originalClassName.$arrayFunName($typedParams): Task<CommonFlow<${modelClass}Array>> {
            |   return Task.execute {
            |       repo.$funName($params).map { ${modelClass}Array(it.toTypedArray()) }.asCommonFlow()
            |   }
            |}
            |
            |fun $originalClassName.$listFunName($typedParams): Task<CommonFlow<${modelClass}List>> {
            |    return Task.execute {
            |        repo.$funName($params).map { ${modelClass}List(it) }.asCommonFlow()
            |    }
            |}
            """.trimMargin().toByteArray()
        )
    }

    private fun KSFunctionDeclaration.skipListWrapper(): Boolean {
        return annotations.any { it.shortName.asString() == SkipListWrappers::class.simpleName }
    }

}