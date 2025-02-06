package com.olam.warehouse.vegax.stockrecon.ui.callback

interface VegaStockCallbackListener {
    fun replaceFragment(receivingType: String, data: Any)

    fun replaceFragment(receivingType: String, data: Any, backStackStatus: Boolean)

}
