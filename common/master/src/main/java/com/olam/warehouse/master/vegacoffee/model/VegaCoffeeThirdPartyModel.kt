package com.olam.warehouse.master.vegacoffee.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel

class VegaCoffeeThirdPartyModelWithLots {
    @Embedded
    lateinit var model: VegaCoffeeThirdPartyRequestModel

    @Relation(
        parentColumn = "vendorWithTransferType",
        entityColumn = "vendorWithTransferType",
        entity = VegaCocoaDispatchLots::class
    )
    var lots: List<VegaCocoaDispatchLots>? = emptyList()
}
