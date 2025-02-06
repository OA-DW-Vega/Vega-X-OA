package com.olam.warehouse.vegax.salescocoa.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaCocoaSalesOrderModel(
    var salesOrderId: String? = "",
    var salesItemNum: String? = "",
    var materialNumber: String? = "",
    var materialDesc: String? = "",
    var plantId: String? = "",
    var Vkorg: String? = "",
    var Vtweg: String? = "",
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
