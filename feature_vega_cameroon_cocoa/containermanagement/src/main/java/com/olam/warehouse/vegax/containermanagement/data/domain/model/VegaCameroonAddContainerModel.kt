package com.olam.warehouse.vegax.containermanagement.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry

data class VegaCameroonAddContainer(
    var containerNum: String = "",
    var containerWeight: String? = "",
    var containerSize: String? = "",
    var shippingLine: String? = "",
    var status: String? = "",
    var uom: String? = "",
    var entryDate: String? = ""
    )

data class VegaCameroonContainer(
    var id: Int = 0,
    var containerNum: String = "",
    var containerWeight: String? = "",
    var containerSize: String? = "",
    var shippingLine: String? = "",
    var status: String? = "",
    var uom: String? = "",
    var entryDate: String? = "",
    val plantDto: Plant
)

data class VegaCameroonAddContainerPost(
    val containerDto: VegaCameroonContainer,
    var editFlag: Boolean = false,
    var editedContainerNum: String? = "",
    val key: String
    )

data class VegaCameroonAddContainerResponse(
    val id :String ="",
    val containerNum: String? = "",
    val containerWeight: String? = "",
    val uom: String? = "",
    val status: String? = "",
    val entryDate: String? = "",
    val createdBy: String? = "",
    val updatedBy: String? = "",
    val message: String? = ""
)



