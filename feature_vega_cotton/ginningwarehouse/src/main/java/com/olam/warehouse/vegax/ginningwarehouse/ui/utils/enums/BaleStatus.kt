package com.olam.warehouse.ginning.utils.enums

enum class BaleStatus(val id: Int, val status: String) {
    Good(1001, "Good"),
    CottonClean(5001, "Bale wrap damage and cotton clean"),
    CottonDirty(5002, "Bale wrap damage and cotton dirty"),
    TieDamage(5003, "Bale tie damage"),
    WetBale(5004, "Wet Bale"),
    NoBaleTag(5005, "No Bale Tag");

    companion object {
        fun from(value: String): BaleStatus? = values().find { it.status == value }

        fun isGoodOrDamaged(value: String?):String {
            when(values().find { it.status == value }) {
                Good -> return "Good"
                CottonClean, CottonDirty, TieDamage, WetBale, NoBaleTag -> return "Damaged"
                else -> return ""
            }
        }
    }

}
