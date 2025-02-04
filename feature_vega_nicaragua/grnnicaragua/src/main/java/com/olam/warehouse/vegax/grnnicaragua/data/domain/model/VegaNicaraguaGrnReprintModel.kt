package com.olam.warehouse.vegax.grnnicaragua.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 10/9/2020.
 */
@Parcelize
data class VegaNicaraguaGrnReprintModel(
        var weighBridgeId: String = "",
        var receivingData: VegaReceiving = VegaReceiving(),
        var priceInfo: VegaNicaraguaInvoicePriceInfo = VegaNicaraguaInvoicePriceInfo(),
        var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = emptyList()
) : Parcelable


data class grnDetailsWrapper(
        var grnDetails: List<VegaReceiving> = emptyList()
)
