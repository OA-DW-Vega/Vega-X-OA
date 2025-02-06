package com.olam.warehouse.vegax.forwardponicaragua.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 10/9/2020.
 */
@Parcelize
data class VegaNicaraguaForwardPoReprintModel(
    var weighBridgeId: String = "",
    var forwardPoData: VegaNicaraguaForwardPODetails = VegaNicaraguaForwardPODetails(),
) : Parcelable


