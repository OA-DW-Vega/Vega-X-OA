package com.olam.warehouse.vegax.dummyquality.data.domain.model

import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative


data class VegaCommonDummySampleModel(
    var key: String = "",
    var transactionNumber: String = "",
    var isDummy: Boolean = false,
    var isSap: Boolean = false,
    var batchNumber: String = "",
    var date: String? = "",
    var materialCode: String = "",
    var materialName: String = "",
    var supplierCode: String = "",
    var supplierName: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String = "",
    var uom: String = "",
    var weight: String = "",
    var werks: String = "",
    var qualityDetails: List<QualityParametersValueList> = emptyList()
)

data class QualityParametersValueList(
    var descrChar: String? = "", var nameChar: String? = "", var qualityParameterValue: String? = ""
)

data class VegaCommonDummySampleModelResponse(
    var success: Boolean = false,
    var errorCode: Int = 0,
    var message: String = "",
)

data class Data(
    var isActive: Boolean? = null,
    var qualityParameters: ArrayList<QualityParameters> = arrayListOf(),
    var isDummy: Boolean? = null,
    var createdAt: Int? = null,
    var createdBy: String? = null,
    var updatedAt: Int? = null,
    var updatedBy: String? = null
)

data class QualityParameters(
    var nameChar: String? = null, var descrChar: String? = null, var qualityParameterValue: String? = null
)

data class ResponseDummySample(
    var data: ArrayList<Data> = arrayListOf(),
    var errorCode: Int? = null,
    var message: String? = null,
    var success: Boolean? = null
)

data class CountryDetail (
   var countryId      : Int?    = null,
   var countryName    : String? = null,
   var countryCode    : String? = null,
   var sapCountryCode : String? = null,
   var currency       : String? = null
)

data class VendorDetails(
    var vendorName: String? = null,
    var vendorAddress: String? = null,
    var vendorCity: String? = null,
    var id: Int? = null,
    var vendorCode: String? = null,
    var countryCode: String? = null,
    var bcApprover: String? = null,
    //var purchaseOrgType: String? = null,
    var vendorCustomerCode: String? = null,
    var taxNumber: String? = null,
    var vendorType: String? = null,
    var vendorAdvLimit: String? = null,
    var storageLocation: String? = null
)
data class MaterialDetail (
    var materialName   : String? = null,
    var materialCode   : String? = null,
    var price          : String? = null,
    var languageCode   : String? = null,
    var unitsOfMeasure : String? = null,
    var currency       : String? = null,
    var plant          : String? = null
)
data class Plant (
    var plantId       : String?        = null,
    var plantName     : String?        = null,
    var plantCode     : String?        = null,
    var countryDetail : CountryDetail? = CountryDetail()
)
data class QualityParameter(
    var nameChar: String? = null,
    var descrChar: String? = null,
    var qualityParameterValue: String? = null
)
data class ResponseDummySampleList (
    var id                    : Int?                         = 0,
    var vendorDetails         : VendorDetails?               = VendorDetails(),
    var plant                 : Plant?                       = Plant(),
    var qualityDetails        : String?                      = "",
    var materialDetail        : MaterialDetail?              = MaterialDetail(),
    var storageLocationDetail : String?                      = "",
    var transactionNumber     : String?                      = "",
    var sapLotId              : String?                      = "",
    var isActive              : Boolean?                     = true,
    var qualityParameters     : List<QualityParameter>       = emptyList(),
    var isDummy               : Boolean?                     = true,
    var createdAt             : String?                      = "",
    var createdBy             : String?                      = "",
    var updatedAt             : String?                      = "",
    var updatedBy             : String?                      = ""

)



//data class ResponseVendorAndMaterial (
//   var vendorList   : ArrayList<VendorList>   = arrayListOf(),
//   var materialList : ArrayList<MaterialList> = arrayListOf()
//)

data class ResponseVendorAndMaterial (
    var vendorCode : String? = " ",
    var vendorName : String? = " ",
    var materialList : ArrayList<MaterialList> = arrayListOf()
)

data class VendorList (
    var vendorCode : String? = " ",
     var vendorName : String? = " "
)

data class MaterialList (
    var materialName : String? = " ",
    var materialCode : String? = " "
)

fun prepareVegaQualityParams(qualityParameter: MutableList<VegaQualityParamsWithQualitative>): ArrayList<QualityParametersValueList> {
    val prepareList = ArrayList<QualityParametersValueList>()
    qualityParameter.forEach {
        val qualityParameter = QualityParametersValueList()
        qualityParameter.descrChar = it.qualityParameter.descrChar.toString()
        qualityParameter.nameChar = it.qualityParameter.nameChar
        qualityParameter.qualityParameterValue = it.qualityParameter.qualityParameterValue.toString()
        prepareList.add(qualityParameter)
    }
    return prepareList
}

