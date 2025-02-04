package com.olam.warehouse.master.vegaghana.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Entity(primaryKeys = ["vehicleId", "vehicleNumber"])
@Parcelize
data class VehicleDetails(
    var vehicleId: Int = 0,
    var vehicleNumber: String = "",
    var qrCodeNumber: String? = "",
    var driverLicenseNumber: String? = "",
    var driverName: String? = "",
    var driverPhone: String? = "",
    var vendorCode: String? = "",
    var name: String? = ""
) : Parcelable
