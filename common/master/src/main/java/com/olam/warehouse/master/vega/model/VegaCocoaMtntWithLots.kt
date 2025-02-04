package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaGhanaPurchaseOrderMaterialModel
import com.olam.warehouse.master.vegacocoa.entity.*

class VegaCocoaMtntWithLots {

    @Embedded
    lateinit var dispatch: VegaCocoaDispatchWB

    @Relation(parentColumn = "weighBridgeId", entityColumn = "weighBridgeId", entity = VegaCocoaDispatchLots::class)
    var lineItems: List<VegaCocoaDispatchLots> = emptyList()

    @Relation(
        parentColumn = "weighBridgeId",
        entityColumn = "weighBridgeId",
        entity = VegaCoffeePurchaseOrderMaterialModel::class
    )
    var materialList: List<VegaCoffeePurchaseOrderMaterialModel> = emptyList()
}


class VegaGhanaCocoaMtntWithLots {

    @Embedded
    lateinit var dispatch: VegaCocoaDispatchWB

    @Relation(
        parentColumn = "weighBridgeId",
        entityColumn = "weighBridgeId",
        entity = VegaGhanaCocoaDispatchLots::class
    )
    var lineItems: List<VegaGhanaCocoaDispatchLots> = emptyList()

    @Relation(
        parentColumn = "weighBridgeId",
        entityColumn = "weighBridgeId",
        entity = VegaGhanaPurchaseOrderMaterialModel::class
    )
    var materialList: List<VegaGhanaPurchaseOrderMaterialModel> = emptyList()
}

class VegaCocoaNoWeighmentWithLots {

    @Embedded
    lateinit var dispatch: VegaCocoaNoWeighmentModel

    @Relation(parentColumn = "weighBridgeId", entityColumn = "weighBridgeId", entity = VegaCocoaNoWeighmentLot::class)
    var lineItems: List<VegaCocoaNoWeighmentLot> = emptyList()
}
