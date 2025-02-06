package com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.enums

enum class BaleStatus(val id: Int, val status: String) {
    Good(1001, "Good"),
    CottonClean(5001, "Bale wrap damage and cotton clean"),
    CottonDirty(5002, "Bale wrap damage and cotton dirty"),
    TieDamage(5003, "Bale tie damage");

    companion object {
        fun from(value: String): BaleStatus? = values().find { it.status == value }

        fun isGoodOrDamaged(value: String?): String {
            when (values().find { it.status == value }) {
                Good -> return "Good"
                CottonClean, CottonDirty, TieDamage -> return "Damaged"
                else -> return ""
            }
        }
    }

}