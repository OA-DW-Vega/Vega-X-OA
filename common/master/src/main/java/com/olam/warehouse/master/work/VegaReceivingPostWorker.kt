package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.utils.getBase64FromFile
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getLineItemFromReceivingLineItem
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONIN
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaReceivingPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaReceivingDao by inject()
        val api: MasterApi by inject()
        val wbid = inputData.getString(RECEIVING_DATA) ?: ""
        val receivingData = dao.getReceivingWithLineItemWbid(wbid)
        var postData = mutableListOf<VegaReceiving>()
        if (receivingData.receiving.truckDirection.equals(DIRECTIONIN)) {
            postData.add(receivingData.receiving)
            postData.forEachIndexed { index, vegaReceiving ->
                vegaReceiving.item = index.inc().toString()
            }
        } else {
            postData = getLineItemFromReceivingLineItem(receivingData.lineItems as MutableList<VegaReceivingLineItem>)
            postData.forEachIndexed { index, vegaReceiving ->
                vegaReceiving.bagWeight = (vegaReceiving.bagCount?.toDouble()?.let { it1 ->
                    vegaReceiving.bagTareWeight?.toDouble()?.times(it1)
                }).toString()
                if (index == 0) {
                    val charset = Charsets.UTF_8
                    val byteArray =
                        getBase64FromFile(receivingData.receiving.imagePath.toString())?.toByteArray(charset)
                    vegaReceiving.imageString = byteArray?.let { it1 -> String(it1) }
                }
                vegaReceiving.netWeight = vegaReceiving.netWeight.replace(",", "").split(" ")[0]
            }
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
                    useCase.saveReceiving(receivingData.receiving)
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
