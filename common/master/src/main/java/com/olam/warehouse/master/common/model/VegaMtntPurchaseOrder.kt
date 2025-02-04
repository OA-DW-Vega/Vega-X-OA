package com.olam.warehouse.master.common.model

import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VegaMtntPurchaseOrder(
    var materialCode: String = "",
    @Ignore
    var purchaseOrders: List<MtntPurchaseOrder> = emptyList()
): Parcelable

