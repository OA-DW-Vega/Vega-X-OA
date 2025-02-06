package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 10/12/2020.
 */
@Entity
data class VegaLastSyncTime(
    @PrimaryKey
    var timeStamp: String = ""
)
