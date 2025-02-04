package com.olam.warehouse.navigation

import android.content.Intent

/**
 * Created by SangiliPandian C on 12-11-2019.
 */
private const val PACKAGE_NAME = "com.olam.warehouse.vegax"

private fun intentTo(className: String): Intent {
    val packageName =
        /*if (BuildConfig.BUILD_TYPE == "dev" || BuildConfig.BUILD_TYPE == "debug") PACKAGE_NAME.plus(".debug") else */
        PACKAGE_NAME
    return Intent(Intent.ACTION_VIEW).setClassName(packageName, className)
}

internal fun String.loadIntentOrNull(): Intent? =
    try {
        Class.forName(this).run { intentTo(this@loadIntentOrNull) }
    } catch (e: ClassNotFoundException) {
        null
    }
