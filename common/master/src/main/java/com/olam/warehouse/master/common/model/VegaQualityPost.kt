package com.olam.warehouse.master.common.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Parcelize
data class VegaQualityPostLot(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityPreParameter?> = emptyList()
) : Parcelable

data class VegaQualityPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList(),
    var notificationFlag: Boolean = false,
    var nextWorkFlowRole: String? = "",
    var navId: String? = "",
    var currentWorkFlowRole: String? = "",
    var environment: String? = ""
)

data class VegaGhanaMtnrQualityPost(
    val grnApplicable: Boolean,
    val grnFlag: Boolean,
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaGhanaMtnrQualityLot?> = emptyList(),
    val bcMessage: String,
    val charg: String,
    val currentWbid: String,
    val errorMessage: String,
    val grnNumber: String
)

data class VegaQualityNigeriaPost(
    val contactNumber: String,
    val driverName: String,
    val grnApplicable: Boolean = false,
    val grnFlag: Boolean = false,
    val grnNumber: String,
    val image: String,
    val imageUploadMsg: String,
    val key: String,
    val message: String,
    val remarks: String,
    val plant: Plant,
    val success: Boolean = false,
    val transportVendorCode: String,
    val userName: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val wayBillNo: String,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList(),
    var notificationFlag: Boolean = false,
    var nextWorkFlowRole: String? = "",
    var navId: String? = "",
    var currentWorkFlowRole: String? = ""
)

data class VegaQualityNigeriaLotPost(
    val contactNumber: String,
    val driverName: String,
    val grnApplicable: Boolean = false,
    val grnFlag: Boolean = false,
    val grnNumber: String,
    val image: String,
    val imageUploadMsg: String,
    val key: String,
    val message: String,
    val remarks: String,
    val plant: Plant,
    val success: Boolean = false,
    val transportVendorCode: String,
    val userName: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val wayBillNo: String,
    var lotDetails: List<VegaQualityPreParameter?> = emptyList()
)

data class QualityNigeriaDetails(
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
)


