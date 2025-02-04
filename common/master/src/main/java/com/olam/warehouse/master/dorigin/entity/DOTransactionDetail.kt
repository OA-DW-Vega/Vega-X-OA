package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
@Entity
data class DOTransactionDetail(
    @PrimaryKey
    var lotTransactionId: String = "",
    var materialId: String? = "",
    var materialName: String? = "",
    var weightUOM: String? = "",
    var isLot: String? = "",
    var netweight: String? = "",
    var supplierId: String? = "",
    var supplierName: String? = "",
    var weightThreshold: Int? = 0,
    var noOfBags: Int? = 0,
    var unitsOfMeasure: String? = "",
    var isMapped: Int? = 0,
    var displaySupplierId: String? = "",
    var creationDate: String? = "",
    var emptyBagWeight: String? = "",
    var bagType: String? = "",
    var weightConversionToKG: String? = ""
)
