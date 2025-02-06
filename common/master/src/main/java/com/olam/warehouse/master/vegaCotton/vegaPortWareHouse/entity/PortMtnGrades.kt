package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 3/27/2020.
 */
@Parcelize
@Entity(primaryKeys = ["grade", "mtnNumber"])
data class PortMtnGrades(
    var grade: String = "",
    @SerializedName("deliveryNo")
    var mtnNumber: String = ""
) : Parcelable
