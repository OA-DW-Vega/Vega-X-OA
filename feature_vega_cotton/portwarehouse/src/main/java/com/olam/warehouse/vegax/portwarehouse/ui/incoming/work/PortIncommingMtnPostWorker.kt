package com.olam.warehouse.vegax.portwarehouse.ui.incoming.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.PortReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.INCOMING_MTN
import com.olam.warehouse.vegax.portwarehouse.data.api.IncomingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

@Suppress("UNCHECKED_CAST")
class PortIncommingMtnPostWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: IncomingApi by inject()
        val dao: PortReceivingDao by inject()
        val mtnNo = inputData.getString(INCOMING_MTN) ?: ""
        val mtnWithBales = dao.getMtnWithBales(mtnNo)
        val mtnModel = getMtnModel(mtnWithBales)
        try {
            val response = api.postOfflineMtnWithBales(getCurrentKey(), mtnModel).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && respData.success) {
                    dao.deleteMtnsByMtnId(mtnNo)
                    dao.deleteMtnBalesByMtnId(mtnNo)
                    Result.success(workDataOf(PortWHUtil.MTN_OUTPUT_DATA to resp?.data?.message))
                } else {
                    Result.failure(workDataOf(PortWHUtil.MTN_OUTPUT_DATA to resp?.data?.message))
                }
            } else {
                Result.failure(workDataOf(PortWHUtil.MTN_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(PortWHUtil.MTN_OUTPUT_DATA to error.message))
        }
    }

    private fun getMtnModel(mtnWithBales: MtnWithBales): PortMtn {
        val mtn = mtnWithBales.mtn
        val bales = mtnWithBales.bales.filter { it.isOfflineData }

        /*bales.forEach {
            it.toSloc = BaleStatus.Good.id.toString()
            it.baleStatus = BaleStatus.Good.status
        }*/

        return PortMtn(
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
