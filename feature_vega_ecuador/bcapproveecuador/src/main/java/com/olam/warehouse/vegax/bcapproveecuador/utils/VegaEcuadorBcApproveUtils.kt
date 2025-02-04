package com.olam.warehouse.vegax.bcapproveecuador.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1

const val LIST_DETAILPAGE = "listdetail"
const val BATCHNO = "batchno"
const val MATERIAL_CODE = "material_code"
    @Suppress("DEPRECATION")
    fun getColor(id: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            App.getAppContext().resources.getColor(id, null)
        } else {
            App.getAppContext().resources.getColor(id)
        }
    }

    fun getDrawable(id: Int): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            App.getAppContext().resources.getDrawable(id, null)
        } else {
            App.getAppContext().resources.getDrawable(id)
        }
    }

    inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
        val yy = ArrayList<Y>()
        this.forEach { t: T ->
            yy.add(property.get(t) as Y)
        }
        return yy
    }

    fun convertMtToKg(weight: String): String {
        val converted = weight.toDouble().times(1000)
        return converted.formatThreeDigits()
    }

    fun convertKgToMT(weight: String): String {
        return weight.toDouble().div(1000).formatThreeDigits()
    }
fun showErrorDialog(context: Context, msg: String) {
    MaterialDialog(context).show {
        title(R.string.error)
        message(null, msg)
        positiveButton(text = UIUtils.getSpannedText(context.getString(R.string.ok), true))
        {
            dismiss()
            hide()
            cancel()
        }
    }
}

