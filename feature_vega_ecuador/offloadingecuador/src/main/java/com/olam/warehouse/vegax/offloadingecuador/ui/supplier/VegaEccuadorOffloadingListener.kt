package com.olam.warehouse.vegax.offloadingecuador.ui.supplier

import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial

interface VegaEccuadorOffloadingListener {
    fun replaceFragment(receivingType: String, data: Any)
   fun replaceFragment(moveFrag: String, weighBridgeData: VegaQualityWBDetails)

    fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>,
        bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
    )

    fun replaceFragment(moveFrag: String)
}
