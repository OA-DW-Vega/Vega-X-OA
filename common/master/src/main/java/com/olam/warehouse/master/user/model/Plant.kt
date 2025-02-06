package com.olam.warehouse.master.user.model

import android.os.Parcelable
import com.olam.warehouse.master.common.model.StorageLocation
import kotlinx.parcelize.Parcelize


@Parcelize
data class Plant(
    var plantId: String = "",
    var plantName: String = "",
    var plantCode: String = "",
    var updatedDateTime: String? = "",
    var status: String? = "",
    var countryDetail: CountryDetail = CountryDetail(),
    var companyDetail: CompanyCode = CompanyCode(),
    var storageLocation: List<StorageLocation> = emptyList(),
    var materialCode: String = "",
    var materialName: String = ""
) : Parcelable

@Parcelize
data class CountryDetail(var countryCode: String = "", var countryId: String = "",var currency: String = "", var countryName: String = "") :
    Parcelable
@Parcelize
data class CompanyCode(var companyId: String = "", var createdBy: String = "", var createdDateTime: String = "",
                       var id: String = "", var plantWiseConfig: String = "", var product: String = "",
                       var profitCentre: String = "", var reportProfitCentre: String = "", var system: String = ""
) : Parcelable
