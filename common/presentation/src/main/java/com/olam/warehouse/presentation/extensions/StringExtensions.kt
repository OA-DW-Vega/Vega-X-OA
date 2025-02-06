package com.olam.warehouse.presentation.extensions

import java.nio.charset.StandardCharsets

fun String.encrypt(): String {
    return android.util.Base64.encodeToString(toByteArray(), android.util.Base64.DEFAULT)
}

fun String.decrypt(): String {
    val byte = android.util.Base64.decode(this, android.util.Base64.DEFAULT)
    return String(byte, StandardCharsets.UTF_8)
}

fun String.valueOrDefault(default: String): String {
    return if (isNullOrBlank()) default else this
}

fun String.noOfLetters(): Int = Regex("[0-9 ]").replace(this, "").length

fun String.removeSpecialCharacters(): String {
    return if (isNullOrBlank()) this else Regex("[^A-Za-z0-9 ]").replace(this.replace("\\s".toRegex(), ""), "")
}
