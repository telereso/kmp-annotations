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

package io.telereso.kmp.processor

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.*


val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

// String extensions
fun String.camelToSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase()
}

fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "").uppercase()
    }
}

fun String.snakeToUpperCamelCase(): String {
    return this.snakeToLowerCamelCase().replaceFirstChar { it.uppercase() }
}

fun KSClassDeclaration.getEnumEntries(): Sequence<KSDeclaration> {
    return declarations.filter { it.closestClassDeclaration()?.classKind == ClassKind.ENUM_ENTRY }
}

fun String.isPrimitiveKotlin(): Boolean {
    return this in listOf("String", "Boolean", "Int", "Long", "Double", "Flout")
}

fun KSTypeReference.kotlinType(): Pair<String,Boolean> {
    return when (this.toString()) {
        "Boolean" -> "Boolean" to false
        "Int" -> "Int" to false
        "Long" -> "Int" to false
        "String" -> "String" to false
        else -> "String" to true
    }
}

fun KSTypeReference.swiftType(): Pair<String,Boolean> {
    return when (this.toString()) {
        "Boolean" -> "Bool" to false
        "Int" -> "Int" to false
        "Long" -> "Int" to false
        "String" -> "String" to false
        else -> "String" to true
    }
}

fun KSTypeReference.objectiveCType(): String {
    return when (val type = this.toString()) {
        "String" -> "NSString"
        "Boolean" -> "NSNumber"
        "Int" -> "NSNumber"
        "Long" -> "NSNumber"
        else -> "NSString"
    }
}

fun KSTypeReference.jsType(hasDefault: Boolean): String {
    val t = resolve()
    return resolveJsType(t.declaration.simpleName.asString(), t.isMarkedNullable, hasDefault).removeSuffix("?")
}

fun String?.jsType(hasDefault: Boolean = false): String {
    val isNullable = this?.endsWith("?") ?: false
    return resolveJsType(this, isNullable, hasDefault).removeSuffix("?")
}

private fun resolveJsType(type: String?, isNullable: Boolean, hasDefault: Boolean): String {
    return when (type) {
        null -> "void"
        "string", "boolean", "number" -> type
        "String" -> if (isNullable || hasDefault) "String = ''" else "string"
        "Boolean" -> if (isNullable || hasDefault) "Boolean = false" else "boolean"
        "Int" -> if (isNullable || hasDefault) "Number = 0" else "number"
        "Long" -> if (isNullable || hasDefault) "Number = 0" else "number"
        else -> "typeof $type"
    }
}

//private fun resolveJsType(type: String?, isNullable: Boolean, hasDefault: Boolean): String {
//    return when (type) {
//        null -> "void"
//        "Boolean" -> if (isNullable || hasDefault) "Boolean = ${if (hasDefault) "false" else "undefined"}" else "boolean"
//        "String" -> if (isNullable || hasDefault) "String = ${if (hasDefault) "\"\"" else "undefined"}" else "string"
//        "Int" -> if (isNullable || hasDefault) "Number = ${if (hasDefault) "0" else "undefined"}" else "number"
//        "Long" -> if (isNullable || hasDefault) "Number = ${if (hasDefault) "0" else "undefined"}" else "number"
//        else -> "typeof $type"
//    }
//}