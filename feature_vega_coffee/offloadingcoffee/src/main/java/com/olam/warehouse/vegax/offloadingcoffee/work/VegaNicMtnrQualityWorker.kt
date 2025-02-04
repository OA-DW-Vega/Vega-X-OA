package com.olam.warehouse.vegax.offloadingcoffee.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingcoffee.data.api.VegaCoffeeOffloadingApi
import com.olam.warehouse.vegax.offloadingcoffee.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject


@Suppress("UNCHECKED_CAST")
class VegaNicMtnrQualityWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaCoffeeOffloadingApi by inject()
        val batchNo = inputData.getString(Constants.BATCH_NUMBER) ?: ""
        val material = inputData.getString(MATERIAL) ?: ""
        var qualityGrade: String = ""
        var certification: String = ""

        try {
            val response = api.getQualityParams(getCurrentKey(), batchNo, material).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    qualityGrade = respData[0].qualityGrade.toString()
                    certification = respData[0].certification.toString()
                    Result.success(
                        workDataOf(
                            LOT_ID to batchNo,
                            QUALITY_GRADE to qualityGrade,
                            CERTIFICATION to certification
                        )
                    )
                } else {
                    val msg = resp?.message ?: resp?.errors
                    Result.failure(workDataOf(QUALITY_PARAMS_DATA to msg))
                }
            } else {
                Result.failure(workDataOf(QUALITY_PARAMS_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(QUALITY_PARAMS_DATA to error.message))
        }
    }
}
