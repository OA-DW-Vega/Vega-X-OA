package com.olam.warehouse.vegax.bcapproveecuador.data.domain.model


import android.os.Parcelable
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import kotlinx.android.parcel.Parcelize


data class VegaEcuadorApproveWbDetail(
        var charg: String? = "",
        var materialNumber: String? = "",
        var qualityParameters: List<VegaQualityParams>? = emptyList(),
        var qualityParams: Quality = Quality()
    )
@Parcelize
data class Quality(
    var B_MOIST: String = "",
    var B_SECONDARY_REFR: String = "",
    var B_GRNPRICE1: String = "",
    var B_BEANCOUNT: String = ""
) : Parcelable

