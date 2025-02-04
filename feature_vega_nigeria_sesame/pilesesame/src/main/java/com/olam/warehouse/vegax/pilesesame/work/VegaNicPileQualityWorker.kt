package com.olam.warehouse.vegax.pilesesame.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.pilesesame.data.api.VegaSesamePileManagementApi
import com.olam.warehouse.vegax.pilesesame.utils.LOT_ID
import com.olam.warehouse.vegax.pilesesame.utils.MATERIAL_CODE
import com.olam.warehouse.vegax.pilesesame.utils.QUALITY_GRADE
import com.olam.warehouse.vegax.pilesesame.utils.QUALITY_PARAMS_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

@Suppress("UNCHECKED_CAST")
class VegaNicMtnrQualityWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaSesamePileManagementApi by inject()
        val batchNo = inputData.getString(Constants.BATCH_NUMBER) ?: ""
        val material = inputData.getString(MATERIAL_CODE) ?: ""
        var qualityGrade: String = ""

        try {
            val response = api.getQualityParams(getCurrentKey(), batchNo, material).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    qualityGrade = respData[0].qualityGrade.toString()
                    Result.success(
                        workDataOf(
                            LOT_ID to batchNo,
                            QUALITY_GRADE to qualityGrade
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

