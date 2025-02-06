package com.olam.warehouse.vegax.qualityofanylot.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class VegaAnyLotListResponse(
    val data: List<VegaAnyLotListData>?=null,
    val errorCode: String? = "",
    val message: String? = "",
    val success: Boolean
)

@Parcelize
data class VegaAnyLotListData(
    val id: Int,
    val vendorDetails: VendorDetails?,
    val plant: PlantLot,
    val qualityDetails: String,
    val materialDetail: MaterialLot,
    val storageLocationDetail:StorageLocationDetail?,
    val transactionNumber:String?="",
    val sapLotId:String?="",
    val isActive:Boolean,
    val qualityParameters:List<QualityDetails> = emptyList(),
    val isDummy:Boolean,
    val createdAt:String,
    val createdBy:String,
    val updatedAt:String,
    val updatedBy:String,
    val secretId:String?="",

):Parcelable

@Parcelize
data class StorageLocationDetail(
    val plant:String?="",
val storageLocationCode:String?= "",
val storageLocationName:String?=""
):Parcelable


@Parcelize
data class VendorDetails(
    val vendorName: String? = "",
    val vendorAddress: String? = "",
    val vendorCity: String? = "",
    val id: String? = "",
    val vendorCode: String? = "",
    val countryCode: String? = "",
    val bcApprover: String? = "",
    val vendorCustomerCode: String? = "",
    val taxNumber: String? = "",
    val vendorType: String? = "",
    val vendorAdvLimit: String? = "",
    var storageLocation: String? = ""
):Parcelable

@Parcelize
data class PlantLot(
    val plantId: String? = "",
    val plantName: String? = "",
    val plantCode: String? = "",
    val countryDetail: CountryLotDetail
):Parcelable

@Parcelize
data class CountryLotDetail(
    val countryId: Int,
    val countryName: String? = "",
    val countryCode: String? = "",
    val sapCountryCode: String? = "",
    val currency: String? = ""
):Parcelable

@Parcelize
data class MaterialLot(
    val materialName: String? = "",
    val materialCode: String? = "",
    val price: String? = "",
    val languageCode: String? = "",
    val unitsOfMeasure: String? = "",
    val currency: String? = "",
    val plant: String? = ""
):Parcelable
