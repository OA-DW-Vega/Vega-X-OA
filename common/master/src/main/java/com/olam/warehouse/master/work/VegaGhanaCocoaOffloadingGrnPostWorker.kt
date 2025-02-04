package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.WAREHOUSE_NUMBER
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Roshna Parambil on 6/19/2021.
 */

@Suppress("UNCHECKED_CAST")
class VegaGhanaCocoaOffloadingGrnPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaEcuadorOffloadingDao by inject()
        val api: MasterApi by inject()
        val wbid = inputData.getString(RECEIVING_DATA) ?: ""
        val whNumber = inputData.getString(WAREHOUSE_NUMBER) ?: ""
        val receivingData = dao.getOffloadingWithLineItem(wbid)
        val materialData = dao.getMaterialData()
        var price = materialData.filter {
            receivingData.receiving.materialCode.toString().contains(it.materialCode)
        }.map { it.price }.single()
        var uomDetails = dao.getuomDetailSync()
        val grnPrice =
            calculatePrice(price.toString(), receivingData.receiving.materialCode, uomDetails)
        val postData = mutableListOf<VegaReceiving>()
        receivingData.receiving.wsGate = "WS01"
        receivingData.receiving.weighBridgeType = "PROCURE"
        postData.clear()
        receivingData.lineItems.forEachIndexed { index, it ->
            val receiving = receivingData.receiving.copy()
            receiving.bagType = it.bagType
            receiving.bagCount = it.bagCount
            receiving.bagWeight =
                it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!.formatThreeDigits()
            receiving.netWeight = it.netWeight
            receiving.grossWeight = it.grossWeight
            receiving.item = index.inc().toString()
            postData.add(receiving)
        }
        if(postData.size==0){
            postData.add(receivingData.receiving.copy())
        }
        postData.forEach {
            it.weighBridgeId = ""
            it.grnNumber = ""
            it.direction = ""
            it.erdat = DateUtils.getCurrentTimeInMills().toString()
        }
        var grnList: ArrayList<VegaGhanaCocoaGrnData> = ArrayList()
        var grnData = VegaGhanaCocoaGrnData(
            receivingData.receiving.batchNumber,
            "",
            "",
            "",
            receivingData.receiving.item,
            receivingData.receiving.materialCode,
            receivingData.receiving.netWeight,
            receivingData.receiving.plantId,
            grnPrice.toString(),
            receivingData.receiving.bagCount,
            receivingData.receiving.purchaseDocDesc,
            receivingData.receiving.purchaseDocNum,
            receivingData.receiving.storageLocationCode,
            receivingData.receiving.supplierCode,
            receivingData.receiving.unitsOfMeasure,
            receivingData.receiving.weighBridgeId,
            receivingData.receiving.weighBridgeType
        )
        grnList.add(grnData)
        var qualityDetails = ArrayList<OffloadingGhanaCocoaQualityDetails>()
        var cameroonOffloadingQualityDetails = GhanaCocoaOffloadingQualityDetails(
            receivingData.receiving.materialCode,
            receivingData.receiving.batchNumber,
            qualityDetails
        )
        var lotDetails = ArrayList<GhanaCocoaOffloadingQualityDetails>()
        lotDetails.add(cameroonOffloadingQualityDetails)
        try {
            val response =
                api.postGhanaCocoaOffloadingDetail(
                    VegaGhanaCocoaOffloadingPost(
                        batchNumber = receivingData.receiving.batchNumber,
                        cascara = "",
                        certificate = "",
                        encodedImageContent = "",
                        errorMessage = "",
                        exchangeRate = "",
                        grade = "",
                        grnData = grnList,
                        grnFlag = true,
                        grnNumber = "",
                        grnType = "",
                        humedad = "",
                        imageUploadMsg = "",
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        weighDetails = postData,
                        lotDetails = lotDetails,
                        whReceiptNum = whNumber
                    )
                ).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    receivingData.receiving.weighBridgeId = respData.wbId.toString()
                    receivingData.receiving.isSynced = true
                    receivingData.receiving.isProgress = false
                    receivingData.receiving.grnNumber = respData.grnNumber.toString()
                    receivingData.receiving.syncStatusMsg = resp.message
                    receivingData.receiving.status = Status.RECEVING_COMPLETED
                    receivingData.receiving.encodedImageContent = respData.encodedImageContent
                    useCase.saveReceiving(receivingData.receiving)

                    if (wbid.contains("TMP"))
                        useCase.updateWBToQualityAndGrnTable(wbid, respData.wbId.toString())
                    Result.success(workDataOf(RECEIVING_OUTPUT_DATA to resp.data.wbId))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    receivingData.receiving.isSynced = false
                    receivingData.receiving.isProgress = false
                    receivingData.receiving.syncStatusMsg = msg
                    receivingData.receiving.status = Status.SYNC_ERROR

                    useCase.saveReceiving(receivingData.receiving)
                    Result.failure(workDataOf(RECEIVING_OUTPUT_DATA to msg))
                }

            } else {
                receivingData.receiving.isSynced = false
                receivingData.receiving.isProgress = false
                receivingData.receiving.syncStatusMsg = response.message()
                receivingData.receiving.status = Status.SYNC_ERROR
                useCase.saveReceiving(receivingData.receiving)
                Result.failure(workDataOf(RECEIVING_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(RECEIVING_OUTPUT_DATA to error.message))
        }
    }
}

private fun calculatePrice(
    price: String,
    materialCode: String?,
    uomDetails: List<VegaUomDetails>
): String {
    var mPrice = 0.0
    var singleBags = 0.0
    var grnPrice = price.toDouble()
    var uom =
        ((uomDetails.filter { (("000000".plus(it.materialCode)).equals(materialCode)) }).filter {
            it.fromUom.equals(
                "BAG"
            )
        }).single()
    singleBags =
        (((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))) ?: 0.0)
    mPrice = singleBags.times(grnPrice)
    return mPrice.formatTwoDigits()
}
