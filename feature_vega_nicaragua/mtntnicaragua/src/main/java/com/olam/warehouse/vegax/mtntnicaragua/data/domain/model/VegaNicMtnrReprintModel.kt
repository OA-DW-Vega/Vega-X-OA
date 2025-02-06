package com.olam.warehouse.vegax.mtntnicaragua.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNicMtnrReprintModel(
    var weighBridgeId: String = "",
    var receivingData: VegaReceiving = VegaReceiving(),
    var priceInfo: VegaNicaraguaInvoicePriceInfo = VegaNicaraguaInvoicePriceInfo(),
    var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = emptyList()
) : Parcelable
