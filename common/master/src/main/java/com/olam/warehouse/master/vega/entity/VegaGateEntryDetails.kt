package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.user.model.Plant
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGateEntryDetails(
    @ColumnInfo(index = true)
    var id: Int = 0,
    var tmpWbId: String? = "",
    var wtype: String? = "",
    var wsgate: String? = "",
    var wbid: String? = "",
    var updatedBy: String? = "",
    var updatedAt: String? = "",
    var truckNumber: String? = "",
    var tareWeight: String? = "",
    var storageLocationCode: String? = "",
    var sampleId: String? = "",
    var netWeight: String? = "",
    var item: String? = "",
    var grossWeight: String? = "",
    var grntNumber: String? = "",
    var geStatus: String? = "",
    var driverNumber: String? = "",
    var driverName: String? = "",
    var currentYear: String? = "",
    var imageString: String? = "",
    var createdBy: String? = "",
    var grnModel: String? = "",
    var procurementType: String? = "",
    var challan: String? = "",
    var weighBridgeType: String? = "",
    @Ignore
    var plant: Plant = Plant(),
    @Ignore
    var vendorCode: String? = "",
    @Ignore
    var materialCode: String? = "",
    @Ignore
    var remarks: String? = ""
) : Parcelable

@Parcelize
data class VendorCode(
    val vendorCode: String = "",
    val vendorAddress: String? = "",
    val vendorCity: String? = "",
    val taxNumber: String? = "",
    val countryCode: String? = "",
    val vendorName: String? = "",
    val vendorType: String? = "",
    val vendorAdvLimit: String? = "",
    val bcApprover: String? = "",
    val vendorCustomerCode: String? = "",
    val purchaseOrgType: PurchaseOrgType? = null
) : Parcelable

@Parcelize
data class PurchaseOrgType(
    val id: Int? = 0,
    val companyCodeFk: CompanyCodeFk? = null,
    val purchaseType: String? = "",
    val createdAt: String? = "",
    val createdBy: String? = "",
    val updatedAt: String? = "",
    val updatedBy: String? = ""
) : Parcelable

@Parcelize
data class CompanyCodeFk(
    val id: Int? = 0,
    val companyId: String? = "",
    val createdBy: String? = "",
    val createdDateTime: String? = "",
    val product: String? = "",
    val profitCentre: String? = ""
) : Parcelable

@Parcelize
data class MaterialCode(
    val bltEnabled: Boolean = false,
    val currency: String? = "",
    val id: String? = "",
    val languageCode: String? = "",
    val materialCode: String? = "",
    val materialName: String? = "",
    val materialType: String? = "",
    val plant: String? = "",
    val price: String? = "",
    val scanLevelId: String? = "",
    val scanLevelName: String? = "",
    val thirdPartyFlag: String? = "",
    val thirdPartyMaterialCode: String? = "",
    val typeCode: String? = "",
    val unitsOfMeasure: String? = ""
) : Parcelable
