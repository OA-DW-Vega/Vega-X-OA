package com.olam.warehouse.vegax.historytransactionsghanacocoa.utils

import android.os.Build
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App

const val HISTORY_TRANSACTIONS_MTNT = "history_transactions_mtnt"
const val HISTORY_TRANSACTIONS_LOT_MTNT = "history_transactions_lot_mtnt"
const val HISTORY_DATA_MTNT = "history_intent_data_mtnt"
const val HISTORY_TRANSACTIONS_MTNR = "history_transactions_mtnr"
const val HISTORY_TRANSACTIONS_LOT_MTNR = "history_transactions_lot_mtnr"
const val HISTORY_DATA_MTNR = "history_intent_data_mtnr"
const val HISTORY_TRANSACTIONS_GRN = "history_transactions_grn"
const val HISTORY_TRANSACTIONS_LOT_GRN = "history_transactions_lot_grn"
const val HISTORY_DATA_GRN = "history_intent_data_grn"
const val BAG = "BAG"
const val KG = "KG"
const val MATERIAL_CODE = "000000"
var uomDetails =  ArrayList<VegaUomDetails>()

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
fun convertKgToBag(materialsCode: String,quantity:String): Int {
    var netWeight = 0.0
//    val materialCode = if(materialsCode.startsWith("1")) MATERIAL_CODE.plus(materialsCode)  else materialsCode
    if(uomDetails.isNotEmpty()) {
        val uom = uomDetails.filter { it.materialCode == materialsCode }.singleOrNull()

        if(uom?.value1?.isNotEmpty() == true && uom.value2?.isNotEmpty() == true)
            netWeight = uom.value2?.toInt()?.toDouble()?.div(uom.value1?.toInt()?.toDouble()?:0.0)?:0.0
    }

    val bagCount = (netWeight *  quantity.toDouble() ).toInt()
    return bagCount
}
fun allPlants(): List<Plant> {
    return Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
}
