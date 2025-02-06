package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.model.VegaMtntPost
import com.olam.warehouse.master.common.utils.getBase64FromFile
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getMtntFromMtntLineItem
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaMtntDao
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaMtntPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaMtntDao by inject()
        val api: MasterApi by inject()
        val wbid = inputData.getString(UIUtils.MTNT_DATA) ?: ""
        val receivingData = dao.getMtntWithLineItemSingle(wbid)
        var postData = mutableListOf<VegaMtnt>()
        if (receivingData.mtnt.truckDirection.equals(UIUtils.DIRECTIONIN)) {
            postData.add(receivingData.mtnt)
            postData.forEachIndexed { index, vegaReceiving ->
                vegaReceiving.item = index.inc().toString()
            }
        } else {
            postData = getMtntFromMtntLineItem(receivingData.lineItems as MutableList<VegaMtntLineItem>)
            postData.forEachIndexed { index, vegaReceiving ->
                vegaReceiving.bagWeight = (vegaReceiving.bagCount?.toDouble()?.let { it1 ->
                    vegaReceiving.bagTareWeight?.toDouble()?.times(it1)
                }).toString()
                if (index == 0) {
                    val charset = Charsets.UTF_8
                    val byteArray = getBase64FromFile(receivingData.mtnt.imagePath.toString())?.toByteArray(charset)
                    vegaReceiving.imageString = byteArray?.let { it1 -> String(it1) }
                }
                vegaReceiving.netWeight = vegaReceiving.netWeight.replace(",", "").split(" ")[0]
            }
        }

        try {
            val response =
                api.postMtntData(VegaMtntPost(getCurrentKey(), getPlantDetails(), postData))
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    receivingData.mtnt.weighBridgeId = respData.wbId.toString()
                    receivingData.mtnt.isSynced = true
                    receivingData.mtnt.isProgress = false
                    receivingData.mtnt.syncStatusMsg = resp.message
                    receivingData.mtnt.status = Status.MTNT_COMPLETED
                    useCase.saveMtnt(receivingData.mtnt)
                    Result.success(workDataOf(UIUtils.MTNT_OUTPUT_DATA to resp.data.wbId))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    receivingData.mtnt.isSynced = false
                    receivingData.mtnt.isProgress = false
                    receivingData.mtnt.syncStatusMsg = msg
                    receivingData.mtnt.status = Status.SYNC_ERROR
                    useCase.saveMtnt(receivingData.mtnt)
                    Result.failure(workDataOf(UIUtils.MTNT_OUTPUT_DATA to msg))
                }

            } else {
                receivingData.mtnt.isSynced = false
                receivingData.mtnt.isProgress = false
                receivingData.mtnt.syncStatusMsg = response.message()
                receivingData.mtnt.status = Status.SYNC_ERROR
                useCase.saveMtnt(receivingData.mtnt)
                Result.failure(workDataOf(UIUtils.MTNT_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.MTNT_OUTPUT_DATA to error.message))
        }
    }
}
