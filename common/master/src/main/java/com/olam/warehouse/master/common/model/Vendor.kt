package com.olam.warehouse.master.common.model

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
data class Vendor(
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
    val werks: String? = "",
    val purchaseOrgType: PurchaseOrgType? = null,
    val storageLocation: StorageLocation? = null
)

data class PurchaseOrgType(
    val id: Int? = 0,
    val companyCodeFk: CompanyCodeFk? = null,
    val purchaseType: String? = "",
    val createdAt: String? = "",
    val createdBy: String? = "",
    val updatedAt: String? = "",
    val updatedBy: String? = ""
)

data class CompanyCodeFk(
    val id: Int? = 0,
    val companyId: String? = "",
    val createdBy: String? = "",
    val createdDateTime: String? = "",
    val product: String? = "",
    val profitCentre: String? = ""
)
