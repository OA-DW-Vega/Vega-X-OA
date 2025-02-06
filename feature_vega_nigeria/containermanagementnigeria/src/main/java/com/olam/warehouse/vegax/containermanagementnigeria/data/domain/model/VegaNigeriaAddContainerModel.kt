package com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaNigeriaAddContainer(
    var containerNum: String = "",
    var containerWeight: String? = "",
    var containerSize: String? = "",
    var shippingLine: String? = "",
    var status: String? = "",
    var uom: String? = "",
    var entryDate: String? = ""
    )

data class VegaNigeriaContainer(
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

data class VegaNigeriaAddContainerPost(
    val containerDto: VegaNigeriaContainer,
    var editFlag: Boolean = false,
    var editedContainerNum: String? = "",
    val key: String
    )

data class VegaNigeriaAddContainerResponse(
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



