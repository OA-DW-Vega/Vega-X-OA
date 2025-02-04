package com.olam.warehouse.master.vegacoffee.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 9/10/2020.
 */
@Parcelize
class VegaCoffeeReceivingMtnrWithLots : Parcelable {
    @Embedded
    lateinit var receiving: VegaCoffeeReceiving

    @Relation(
        parentColumn = "delivery",
        entityColumn = "delivery",
        entity = VegaCoffeeReceiveLots::class
    )
    var lineItems: List<ReceivingLotsWithBagItem> = emptyList()
}

class ReceivingLotsWithBagItem {
    @Embedded
    lateinit var lots: VegaCoffeeReceiveLots
    @Relation(
        parentColumn = "batch",
        entityColumn = "batchNumber",
        entity = VegaCoffeeOffloadingBagMaterial::class
    )
    var bagItem: List<VegaCoffeeOffloadingBagMaterial> = emptyList()
}
