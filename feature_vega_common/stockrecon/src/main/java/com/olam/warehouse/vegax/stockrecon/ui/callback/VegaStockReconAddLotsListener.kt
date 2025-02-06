package com.olam.warehouse.vegax.stockrecon.ui.callback

import com.olam.warehouse.master.vega.entity.VegaDispatchLots


/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
interface VegaStockReconAddLotsListener {
    fun addedLots(lots: ArrayList<VegaDispatchLots>)
}
