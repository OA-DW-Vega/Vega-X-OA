package com.olam.warehouse.presentation.utils.extension

import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Created by SangiliPandian C on 16-11-2019.
 */
fun String.encrypt(): String {
    return android.util.Base64.encodeToString(toByteArray(), android.util.Base64.DEFAULT)
}

fun String.decrypt(): String {
    val byte = android.util.Base64.decode(this, android.util.Base64.DEFAULT)
    return String(byte, StandardCharsets.UTF_8)
}

fun String.upperCase(): String {
    return toUpperCase(Locale.getDefault())
}
