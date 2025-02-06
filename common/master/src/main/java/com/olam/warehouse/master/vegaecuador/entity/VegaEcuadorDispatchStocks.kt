package com.olam.warehouse.master.vegaecuador.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

/**
 * Created by Keerthi Santhanam on 7/14/2020.
 */

@Entity(primaryKeys = ["batchNumber", "materialCode", "storageLocationCode"])
@Parcelize
data class VegaEcuadorDispatchStocks(
    @ColumnInfo(index = true)
    var batchNumber: String = "",
    var bkBez: String? = "",
    var bkLas: String? = "",
    var cinsm: String? = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var materialText: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var vendor: String? = "",
    var vendorName: String? = "",

    /*for stock recon, we have added 3 more coloums*/
    var bagType: String? = "",
    var totalBagWeight: String? = "",
    var totalNoOfBags: String? = ""

) : Parcelable
