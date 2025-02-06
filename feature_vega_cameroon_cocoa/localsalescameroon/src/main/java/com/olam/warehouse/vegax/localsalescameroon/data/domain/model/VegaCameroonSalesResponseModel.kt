package com.olam.warehouse.vegax.localsalescameroon.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaCameroonSalesOrderModel(
    var salesOrderId: String = "",
    var salesOrderList: List<materialList> = emptyList()
) : Parcelable

@Parcelize
data class materialList(
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
) : Parcelable

data class VegaCameroonLocalSalesWSBagModel(
    var weight: String = "",
    var isPalletMatched: Boolean = true
)

data class VegaCameroonLocalSalesAssignLot(
    var materialName: String = "",
    var mergedLotId: String = ""
)

@Parcelize
data class VegaDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityGrade: String? = "",
    var certification: String? = ""
) : Parcelable
