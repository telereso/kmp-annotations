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