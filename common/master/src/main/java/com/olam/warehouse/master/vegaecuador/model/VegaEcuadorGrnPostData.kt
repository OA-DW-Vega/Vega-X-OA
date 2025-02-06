package com.olam.warehouse.master.vegaecuador.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import kotlinx.parcelize.Parcelize

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
data class VegaEcuadorGrnPostData(
    var batchNumber: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String? = "",
    var weighMethod: String? = "",
    var unitsOfMeasure: String? = "",
    var supplierCode: String? = "",
    var storageLocationCode: String? = "",
    var price: String? = "",
    var plant: String? = "",
    var netWeight: String? = "",
    var materialCode: String? = "",
    var item: String? = "",
    var currency: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var billOfLading: String? = "",
    var deliveryNote: String? = "",
    var headerText: String? = "",
    var reference: String? = "",
    var qualityDetails: List<VegaQualityParams> = emptyList()
)

data class VegaEcuadorGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaEcuadorGrnPostData?> = emptyList(),
    var qualityDetails: List<VegaGrnqualityList> = emptyList(),
)

@Parcelize
data class VegaGrnqualityList(
    var descrChar: String? = "",
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
): Parcelable
