package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DispatchDetail(

    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var sapMaterialId: String = "",
    var totalDispatched: Int = 0,
    var totalReceived: Int = 0,
    var totalQCComplete: Int = 0,
    var productId: String? = "",
    var originId: String? = "",
    var totalGrnPosted: Int = 0
)
