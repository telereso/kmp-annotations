package io.telereso.kmp.processor

import com.google.devtools.ksp.symbol.KSTypeReference


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



fun KSTypeReference.iosType(): String {
    return when (val type = this.toString()) {
        "Boolean" -> "Bool"
        else -> type
    }
}