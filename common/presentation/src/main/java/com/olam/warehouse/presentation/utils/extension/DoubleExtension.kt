package com.olam.warehouse.presentation.utils.extension

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Created by SangiliPandian C on 16-11-2019.
 */
fun Double.format(): String {
    val df = DecimalFormat()
    df.maximumFractionDigits = 1
    return df.format(this)
}

fun Double.formatNDigits(n: Int): String {
    var hashString = ""
    for (i in 1..n) {
        hashString += "#"
    }
    val df = DecimalFormat("#.$hashString", DecimalFormatSymbols(Locale.ENGLISH))
    df.maximumFractionDigits = n
    return df.format(this).replace(",", "")
}

fun Double.formatTwoDigits(): String {
    val df = DecimalFormat("#.##", DecimalFormatSymbols(Locale.ENGLISH))
    df.maximumFractionDigits = 2
    return df.format(this).replace(",", "")
}

fun String.formatTwoDigitString(): String {
    val df = DecimalFormat("#.##", DecimalFormatSymbols(Locale.ENGLISH))
    df.maximumFractionDigits = 2
    return df.format(this).replace(",", "")
}

fun formatStringTwoD(input:Double?):String{
 return  String.format("%.2f",input);
}


fun Double.formatThreeDigits(): String {
    val df = DecimalFormat("#.###", DecimalFormatSymbols(Locale.ENGLISH))
    df.maximumFractionDigits = 3
    return df.format(this).replace(",", "")
}

fun String.splitDecimal(precision: Int): String {
    val data1 = this.format().split(".")[0]
    val data2 = this.split(".")[1].take(precision)
    if (data1.contains(",")) {
        data1.replace(",", "")
    }
    return data1.plus(".").plus(data2)
}


fun isNumericToX(toCheck: String): Boolean {
    return toCheck.toDoubleOrNull() != null
}
