package com.olam.warehouse.vegax.ginningwarehouse.ui.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnWithBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.ReceivingModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.IncomingMtnApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.INCOMING_MTN
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.MTN_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("UNCHECKED_CAST")
class IncomingMtnPostWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: IncomingMtnApi by inject()
        val model: ReceivingModel by inject()
        val mtnNo = inputData.getString(INCOMING_MTN) ?: ""
        val mtnWithBales = model.getMtnWithBales(mtnNo)
        val mtnModel = getMtnModel(mtnWithBales)
        try {
            val response = api.postOfflineMtnWithBales(getCurrentKey(), mtnModel).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && respData.success) {
                    model.deleteMtnsByMtnId(mtnNo)
                    model.deleteMtnBalesByMtnId(mtnNo)
                    Result.success(workDataOf(MTN_OUTPUT_DATA to resp?.data?.message))
                } else {
                    Result.failure(workDataOf(MTN_OUTPUT_DATA to resp?.data?.message))
                }
            } else {
                Result.failure(workDataOf(MTN_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(MTN_OUTPUT_DATA to error.message))
        }
    }

    private fun getMtnModel(mtnWithBales: MtnWithBales): Mtn {
        val mtn = mtnWithBales.mtn
        val bales = mtnWithBales.bales.filter { it.isOfflineData }

        /*bales.forEach {
            it.toSloc = BaleStatus.Good.id.toString()
            it.baleStatus = BaleStatus.Good.status
        }*/

        return Mtn(
            mtnNumber = mtn.mtnNumber,
            suplierPlantId = mtn.suplierPlantId,
            suplierPlantDesc = mtn.suplierPlantDesc,
            recievedPlantId = mtn.recievedPlantId,
            recievedPlantDesc = mtn.recievedPlantDesc,
            lineItem = mtn.lineItem,
            containerNo = mtn.containerNo,
            grade = mtn.grade,
            uom = mtn.uom,
            sourceNetWeight = mtn.sourceNetWeight,
            baleCount = mtn.baleCount,
            truckNumber = mtn.truckNumber,
            istogrnPost = "X",
            itransferPost = "",
            mtnBales = bales
        )
    }
}
