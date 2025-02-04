package com.olam.warehouse.vegax.mtntindo.ui.callback

import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
interface VegaIndoCoffeeDispatchAddLotsListener {
    fun addedLots(lots: ArrayList<VegaEcuadorDispatchLots>)
}
