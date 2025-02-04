package com.olam.warehouse.vegax.containermanagement.data.domain.model

import java.io.Serializable

data class VegaCameroonContainerInventoryModel(
    val containerDTOs: List<ContainerInventory> = emptyList()
)

data class ContainerInventory(
    var id: Int = 0,
    var containerNum: String ="",
    var status: String,
    var containerWeight: String,
    var containerSize: String,
    var shippingLine: String,
    var entryDate: String,
    var uom: String
) : Serializable

data class VegaCameroonContainerStuffingDetails(
    var salesOrderNo:String="",
    var lotList: List<ContainerLotDetails>
):Serializable

data class ContainerLotDetails(
    var lotId: String ="",
    var weight: String ="",
    var materialCode: String ="",
    var stLocation: String =""
)
