package com.olam.warehouse.vegax.pilesesame.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.pilesesame.data.api.VegaSesamePileManagementApi
import com.olam.warehouse.vegax.pilesesame.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("UNCHECKED_CAST")
class VegaNicMtnrQualityWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaSesamePileManagementApi by inject()
        val batchNo = inputData.getString(Constants.BATCH_NUMBER) ?: ""
        val material = inputData.getString(MATERIAL_CODE) ?: ""
        var qualityGrade: String = ""
        var ticketNumber: String = ""

        try {
            val response = api.getQualityParams(getCurrentKey(), batchNo, material).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    qualityGrade = respData[0].qualityGrade.toString()
                    if(respData[0].qualityParameters.isNotEmpty()) {
                        if(respData[0].qualityParameters.filter { it.sapQCName == "NICERTI" }.isNotEmpty()) {
                            ticketNumber =
                                respData[0].qualityParameters.filter { it.sapQCName == "NICERTI" }[0].satNam.toString()
                        }
                    }
                    Result.success(
                        workDataOf(
                            LOT_ID to batchNo,
                            QUALITY_GRADE to qualityGrade,
                            TICKET_NUMBER to ticketNumber
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

