package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaProcessingDao
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaRminPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaProcessingDao by inject()
        val api: MasterApi by inject()
        val batchNo = inputData.getString(RMIN_DATA) ?: ""
        val dispatchData = dao.getSingleRminLots(batchNo)
        dispatchData.rminPo.key = getCurrentKey()
        dispatchData.rminPo.plant = getPlantDetails()
        dispatchData.rminPo.processingLotDtls = dispatchData.lineItems

        try {
            val response =
                api.postCreatePo(dispatchData.rminPo).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    var msg = ""
                    if (!respData.autoPoResponse.isNullOrEmpty()) {
                        msg = resp.data.autoPoResponse?.get(0)?.messages?.get(0)?.message.toString()
                        dao.updateRminData(
                            batchNo,
                            resp.data.autoPoResponse?.get(0)?.messages?.get(0)?.message.toString(),
                            true,
                            Status.RMIN_COMPLETED
                        )
                    } else {
                        msg = resp.message.toString()
                        dao.updateRminData(batchNo, msg, true, Status.RMIN_COMPLETED)
                    }
                    Result.success(workDataOf(DISPATCH_OUTPUT_DATA to msg))
                } else {
                    val msg = resp?.message
                    dao.updateRminData(batchNo, msg.toString(), false, Status.SYNC_ERROR)
                    Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to msg))
                }

            } else {
                dao.updateRminData(batchNo, response.message().toString(), false, Status.SYNC_ERROR)
                Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to error.message))
        }
    }
}
