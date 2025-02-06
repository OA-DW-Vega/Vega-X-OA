package com.olam.warehouse.vegax.processing.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.vegax.processing.data.api.VegaProcessingApi
import com.olam.warehouse.vegax.processing.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaProcessingQualityWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaProcessingApi by inject()
        val batchNo = inputData.getString(BATCH_NUMBER) ?: ""
        val material = inputData.getString(MATERIAL) ?: ""
        var regionValue: String = ""
        var korValue: String = ""

        try {
            val response = api.getQualityParams(getCurrentKey(), batchNo, material).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    respData[0].qualityParameters.forEach {
                        if (it.sapQCName?.equals("CI_RCN_REGION")!!) {
                            regionValue = it.satNam.toString()
                        } else if (it.sapQCName?.equals("CI_RCN_KOR")!!) {
                            korValue = it.satNam.toString()
                        }
                    }
                    Result.success(workDataOf(REGION to regionValue, KOR to korValue))
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
