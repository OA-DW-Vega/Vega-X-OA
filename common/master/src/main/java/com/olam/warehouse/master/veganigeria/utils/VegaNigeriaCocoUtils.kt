package com.olam.warehouse.master.veganigeria.utils

import com.olam.warehouse.presentation.utils.extension.formatTwoDigits

const val PORT_PLANTLIST = "PORT_PLANTLIST"
var portPlantIdList = ArrayList<String>()
var B_MOIST_DISC = "0"
var ADMIX_DISC = "0"
var GRN_PAID_PRICE = "0"
var ZNGCOCOA_ACTBW = "0"
var B_DCTBW1 = "0"
var TOTAL_DISC = "0"


fun calcualateB_MoistDiscount(grnPrice: String, b_moist: String): String {
    //B_MOIST_DISC = ZNGCOCOA_GRNPRICE * (B_MOIST - 8)/100
    if (B_DCTBW1.toDouble() < ZNGCOCOA_ACTBW.toDouble()) {
        B_MOIST_DISC = grnPrice.toDouble().times(b_moist.toDouble().minus(8)).div(100).formatTwoDigits()
    }
    return B_MOIST_DISC
}

fun calcualateAdmixDiscount(grnPrice: String, ng_admix: String): String {
    // ADMIX_DISC = ZNGCOCOA_GRNPRICE * (NG_ADMIX  - 4)/100
    if (ng_admix.toDouble() > 4) {
        ADMIX_DISC = grnPrice.toDouble().times(ng_admix.toDouble().minus(4)).div(100).formatTwoDigits()
    }
    return ADMIX_DISC
}

fun calcualateGrnPaidPrice(
    grnPrice: String,
    mouldDiscount: String,
    beanWeightDiscount: String,
    beanSlatyDiscount: String,
    discountOnOthers: String
): String {
    // ZNGCOCOA_GRNPAIDPRICE = ZNGCOCOA_GRNPRICE -(B_MOIST_DISC + ADMIX_DISC + ZNGCOCOA_DIS_MOULD +ZNGCOCOA_DIS_BW + ZNGCOCOA_DIS_BS+ Discount_On_OTHERS
    try {
        val total =
            B_MOIST_DISC.toDouble() + ADMIX_DISC.toDouble() + mouldDiscount.toDouble() + beanWeightDiscount.toDouble() + beanSlatyDiscount.toDouble() + discountOnOthers.toDouble()
        TOTAL_DISC = total.formatTwoDigits()
        GRN_PAID_PRICE = grnPrice.toDouble().minus(total).formatTwoDigits()
        return GRN_PAID_PRICE
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        return ""
    }
}

/*fun calculateFinalPrice(discountOnOthers:String) :String {
    FINAL_PAID_PRICE = grnPrice.toDouble().minus(B_MOIST_DISC.toDouble() + ADMIX_DISC.toDouble() + mouldDiscount.toDouble() + beanWeightDiscount.toDouble() + beanSlatyDiscount.toDouble()).formatThreeDigits()
    FINAL_PAID_PRICE = GRN_PAID_PRICE.toDouble().minus(discountOnOthers.toDouble()).formatThreeDigits()
    return FINAL_PAID_PRICE
}*/





