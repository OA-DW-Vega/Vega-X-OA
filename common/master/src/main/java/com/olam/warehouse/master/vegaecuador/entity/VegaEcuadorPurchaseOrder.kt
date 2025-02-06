package com.olam.warehouse.master.vegaecuador.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by Keerthi Santhanam on 24/6/2020.
 */
@Entity
@Parcelize
data class VegaEcuadorPurchaseOrder(
    @PrimaryKey
    var poId: String = "",
    var warehouseId: String = "",
    var ebelp: String = "",
    var poType: String = "",
    var material: String = "",
    var supplier: String = "",
    var createdDate: String = "",
    var menge: String = "",
    var meins: String = "",
    var charg: String = "",
    var materialNumber: String = "",
    var materialDesc: String? = "",
    var supplierName: String? = "",
    var supplierCode: String? = "",
    var bsart: String = "",
    var ekorg: String = "",
    var ekgrp: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String = "",
    var recPlantId: String = "",
    var year: String? = "",
    var openQuantity: String? = "",
    var unitPrice: String? = "0"
    //var
) : Parcelable
