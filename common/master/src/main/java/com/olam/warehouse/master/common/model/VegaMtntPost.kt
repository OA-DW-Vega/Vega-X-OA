package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

data class VegaMtntPost(val key: String, val plant: Plant, val weighDetails: List<VegaMtnt>)

data class VegaMtntResponse(
    val wbId: String? = "",
    val grnNumber: String? = "",
    val delivery: String? = "",
    val txnId: String? = "",
    val mtnNumber: String? = "",
    val encodedImageContent: String? = "",
    val data: String? = "",
    var weighbridgeType: String? = "",
    var wbFlag: Boolean = false,
    var qcFlag: Boolean = false,
    var grnFlag: Boolean = false,
    var deliveryDetails: List<VegaIndoCoffeeOffloadingDeliveryDetailCom> = emptyList()
)

data class Data(
    val data: String? = ""
)

data class VegaIndoCoffeeOffloadingDeliveryDetailCom(
    var batchNumber: String? = "",
    var bagList: List<VegaCoffeeOffloadingBagMaterial> = emptyList(),
    var wsGate: String = "",
    var wbFlag: Boolean = false,
    var qcFlag: Boolean = false,
    var grnFlag: Boolean = false
)
