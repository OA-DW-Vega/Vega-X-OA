package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaEcuadorOffloadingPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaEcuadorOffloadingDao by inject()
        val api: MasterApi by inject()
        val wbid = inputData.getString(RECEIVING_DATA) ?: ""
        val receivingData = dao.getOffloadingWithLineItem(wbid)
        val postData = mutableListOf<VegaReceiving>()
        receivingData.receiving.wsGate = "WS01"
        receivingData.receiving.weighBridgeType = "PROCURE"
        postData.clear()
        receivingData.lineItems.forEachIndexed { index, it ->
            val receiving = receivingData.receiving.copy()
            receiving.bagType = it.bagType
            receiving.bagCount = it.bagCount
            receiving.bagWeight = it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!.formatThreeDigits()
            receiving.netWeight = it.netWeight
            receiving.grossWeight = it.grossWeight
            receiving.item = index.inc().toString()
            postData.add(receiving)
        }
        try {
            val response =
                api.postReceivingDetail(VegaReceivingPost(getCurrentKey(), getPlantDetails(), postData))
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    receivingData.receiving.weighBridgeId = respData.wbId.toString()
                    receivingData.receiving.isSynced = true
                    receivingData.receiving.isProgress = false
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
