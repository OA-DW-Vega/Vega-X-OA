package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.PortTransMasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VegaScanDetailsWorkerForPort(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sleep()
        val key = getCurrentKey()
        val api: PortTransMasterApi by inject()
        val qrValue = inputData.getString(UIUtils.QR_VALUE) ?: ""
        try {
            val response = api.getScanDetails(qrValue, key).execute()
            if (response.isSuccessful) {
                val data = response.body()
                if (data?.errors?.isNotEmpty() == true) {
                    Result.failure(workDataOf(TRANS_OUTPUT_DATA to data.errors))
                } else {
                    if (data!!.data != null) {
                        data.data.let { scandata ->
                            val gson = GsonUtils()
                            PreferenceHelper.save(TRANS_OUTPUT_DATA, gson.toJson(data.data))
                        }
                        Result.success()
                    } else {
                        Result.failure(workDataOf(TRANS_OUTPUT_DATA to data.message))
                    }
                }
            } else if (response.body() == null) {
                Result.failure(workDataOf(TRANS_OUTPUT_DATA to response.raw().message))
            } else {

                Result.retry()
            }

        } catch (error: Throwable) {
            Result.retry()
        }
    }
}
