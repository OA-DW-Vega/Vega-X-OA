package com.olam.warehouse.vegax.forwardponicaragua.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 10/9/2020.
 */
@Parcelize
data class VegaNicaraguaForwardPoReprintModel(
    var weighBridgeId: String = "",
    var forwardPoData: VegaNicaraguaForwardPODetails = VegaNicaraguaForwardPODetails(),
) : Parcelable


