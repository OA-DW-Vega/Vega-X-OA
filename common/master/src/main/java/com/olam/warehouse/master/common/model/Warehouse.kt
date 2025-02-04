package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Parcelize
class Warehouse(
        val warehouseId: String = "",
        val warehouseName: String? = "",
        val plantId: Long? = 0
) : Parcelable
