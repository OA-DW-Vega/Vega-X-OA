package com.olam.warehouse.vegax.qualitycoffee.data.domain.model

import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.CompanyCode
import okhttp3.MultipartBody

class NicaraguaSavePrintTicket(

    var moduleName: String = "",
    var type: String = "",
    var moduleNo: String = "",
    var trasanctionNo: String = "",
    var file: String = "",
    var materialName: String = "",
    var date: String = "",
    var companyCode: String = getCurrentKey().split("_")[1]

)
