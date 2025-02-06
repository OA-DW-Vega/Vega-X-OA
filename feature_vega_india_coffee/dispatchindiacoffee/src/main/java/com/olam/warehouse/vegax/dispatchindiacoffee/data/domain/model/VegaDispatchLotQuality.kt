package com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 2/20/2020.
 */
@Parcelize
data class VegaDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaDispatchLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaDispatchLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

@Parcelize
data class VegaIndiaCoffeeMtntMaterial(
    var salesOrderId: String? = "",
    var salesItemNum: String? = "",
    var materialNumber: String? = "",
    var materialDesc: String? = "",
    var plantId: String? = "",
    var vkorg: String? = "",
    var vtweg: String? = "",
    var Spart: String? = "",
    var soldToPartyName: String? = "",
    var createdDate: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var openQuantity: String? = "",
    var soldToPartyCode: String? = "",
    var shipToPartyName: String? = "",
    var shipToPartyCode: String? = ""
):Parcelable


data class VegaIndiaCoffeeMtntAssignLot (
    var materialName:String? ="",
    var mergedLotId:String? = ""
)

