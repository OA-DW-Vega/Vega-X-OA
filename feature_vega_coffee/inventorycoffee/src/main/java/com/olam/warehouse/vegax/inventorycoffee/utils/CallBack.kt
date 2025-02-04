package com.olam.warehouse.vegax.inventorycoffee.utils

import android.os.Bundle

interface CallBack {
    fun replaceFragment(
        moveFrag: String, bundle: Bundle
        /*
        dispatchLotsList: MutableList<VegaDispatchLots>,
        dispatchData: VegaDispatchTrucks*/
    )

    fun replaceFragment(moveFrag: String, bundle: Bundle, fullFilter: ArrayList<String>)

    fun filterList(list: ArrayList<String>) {

    }
}
