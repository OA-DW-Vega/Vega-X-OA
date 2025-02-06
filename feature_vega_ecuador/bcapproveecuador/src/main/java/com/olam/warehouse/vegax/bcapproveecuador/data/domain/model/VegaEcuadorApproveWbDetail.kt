package com.olam.warehouse.vegax.bcapproveecuador.data.domain.model


import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vegacameroon.model.QualityDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaEcuadorApproveWbDetail(
        var charg: String? = "",
        var materialNumber: String? = "",
        var qualityParameters: List<VegaQualityParams>? = emptyList(),
        var qualityParams: Quality = Quality()
    ):Parcelable
@Parcelize
data class Quality(
    var B_MOIST: String = "",
    var B_SECONDARY_REFR: String = "",
    var B_GRNPRICE1: String = "",
    var B_PAID_WT: String = "",
    var B_BEANCOUNT: String = ""
) : Parcelable

@Parcelize
data class VegaEcuadorApproveQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

@Parcelize
data class VegaEcuadorApproveQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaEcuadorApproveQualityParams> = emptyList()
) : Parcelable

data class VegaEcuadorQualityApprove(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaQualityParams>? = emptyList()
)
data class VegaEcuadorPostApprovalRequest(
    val key: String,
    val plant: Plant,
    var approvalDetails: VegaQualityApproveEcuadorPostData? = VegaQualityApproveEcuadorPostData()
)
@Parcelize
data class VegaQualityApproveEcuadorPostData(
    var weighBridgeId: String? = "",
    var item: String? = "",
    var materialCode: String? = "",
    var supplierCode: String? = "",
    var batchNumber: String? = "",
    var waers: String? = "",
    var plant: String? = "",
    var discount: String? = "",
    var priceCharacter: String? = "",
    var qchar: String? = "",
    var paidWeight: String? = "",
    var grnQty: String? = "",
    var finalApproval: String? = "",
    var autoTransfer: String? = "",
    var receivingStorageLoc: String? = "",
    var sendingStorageLoc: String? = "",
    var uom: String? = "",
    var qualityDetails: List<QualityDetails> = emptyList()
) : Parcelable

data class VegaEcuadorBcApprovePostResponse(
    var message: String? = "",
    var status: String? = ""
)

