package com.olam.warehouse.master.vegaecuador.model

import android.os.Parcelable
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by Keerthi Santhanam on 7/16/2020.
 */
data class VegaEcuadorDeliveryPost(
    val key: String,
    val plant: Plant,
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false,
    var deliveryDetails: List<VegaEcuadorDispatchLotsMerge> = emptyList()
)

data class VegaEcuadorDispatchPurchaseOrder(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaEcuadorDispatchPurchaseOrders> = emptyList()
)

@Parcelize
data class VegaEcuadorDispatchLotsMerge(
    var batchNumber: String = "",
    var mergeStatus: String = "",
    var lots: ArrayList<VegaEcuadorDispatchLots> = ArrayList(),
    var message: String = "",
    var deliveryId: String = "",
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false
) : Parcelable

data class VegaEcuadorDeliveryPostResponse(
    var delFlag: String? = "",
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var deliveryDetails: List<VegaEcuadorDispatchLotsMerge> = emptyList(),
    val key: String = getCurrentKey(),
    var pgi: Boolean = false,
    var pgiFlag: String? = "",
    var pickingBatchFlag: String? = "",
    var picking: Boolean = false,
    val plant: Plant,
    var message: String = ""
)
