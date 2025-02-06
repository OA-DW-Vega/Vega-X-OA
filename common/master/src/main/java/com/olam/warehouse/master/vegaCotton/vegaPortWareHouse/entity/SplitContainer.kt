package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by SangiliPandian C on 12-02-2019.
 */
@Entity(indices = [Index(value = ["oldOtNumber", "otNumber"], unique = true)])
@Parcelize
data class SplitContainer(
    @PrimaryKey
    var oldOtNumber: String = "",
    var otNumber: String? = "",
    var cntrList: ArrayList<String>
) : Parcelable
