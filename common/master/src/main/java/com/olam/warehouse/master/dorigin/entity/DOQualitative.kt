package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity(primaryKeys = ["materialCode", "nameChar", "charValue"])
data class DOQualitative(
    var charValue: String = "",
    var materialCode: String = "",
    var nameChar: String = "",
    var descValue: String? = ""
)
