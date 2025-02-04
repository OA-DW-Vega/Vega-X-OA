package com.olam.warehouse.master.user.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Plant(
    var plantId: String = "",
    var plantName: String = "",
    var plantCode: String = "",
    var updatedDateTime: String? = "",
    var status: String? = "",
    var countryDetail: CountryDetail = CountryDetail()
) : Parcelable

@Parcelize
data class CountryDetail(var countryCode: String = "", var countryId: Int = 0, var countryName: String = "") :
    Parcelable
