package com.olam.warehouse.vegax.advanceniicaragua.utils

import android.os.Build
import com.olam.warehouse.vegax.App
import java.util.*
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 10/12/2020.
 */
const val ADVANCE_DETAILS_FRAG = "advance_details_frag"
const val ADVANCE_SUMMARY_FRAG = "advance_summary_frag"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun covertToDouble(value: String?): Double {
    if (value != null && value.length > 0) {
        val str = value.format(Locale.ENGLISH).replace(",", ".")
        return str.toDouble()
    }
    return 0.00
}

fun removeNonNumeric(value: String?): String {
    if (value != null && value.length > 0) {
        val str = Regex("[^0-9.]").replace(value, "")
        return str
    }
    return ""
}

fun postfixToPrefix(value: String?): String {
    if (value != null && value.length > 0 && value.contains("-")) {
        val str = value.format(Locale.ENGLISH).split("-")
        return if (str.size > 1)
            "-".plus(str[0])
        else
            value.toString()
    }
    return value.toString()
}

fun covertToInt(value: String?): Int {
    if (value != null && value.length > 0) {
        val str = value.format(Locale.ENGLISH).replace(",", ".")
        return str.toInt()
    }
    return 0
}

fun getTmpId() = "TMP_".plus(Random.nextInt().toString())
