package com.olam.warehouse.vegax.dispatchindiacoffee.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.vegax.dispatchindiacoffee.data.api.VegaDispatchApi
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
@Suppress("UNCHECKED_CAST")
class VegaDispatchQualityWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaDispatchApi by inject()
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
