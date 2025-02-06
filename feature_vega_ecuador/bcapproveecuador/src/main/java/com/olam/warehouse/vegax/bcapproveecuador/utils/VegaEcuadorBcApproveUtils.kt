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
const val LIST__WB_DETAILPAGE = "listWbdetail"
const val BATCHNO = "batchno"
const val MATERIAL_CODE = "material_code"
const val GRN_QUALITY_DETAILS = "GRN_QUALITY_DETAILS"
const val BC_QUALITY_DETAILS = "BC_QUALITY_DETAILS"
const val BC_SUMMARY_DETAILS = "BC_SUMMARY_DETAILS"
const val BC_WB_DETAILS = "BC_WB_DETAILS"
const val WEIGHSCALE = "WEIGHSCALE"
const val WEIGHBRIDGE = "WEIGHBRIDGE"
const val USAGE_POST = "Usage Decision"
const val USAGE_DECISION_ACCEPT = "OL-RM    A"
const val LOBM_UDCODE = "LOBM_UDCODE"

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

