package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.presentation.utils.AppUtils.PLANT_ID
import com.olam.warehouse.presentation.utils.INVENTORY_SYNC_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 4/7/2020.
 */
class InventorySyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Data downloading...", applicationContext)
        sleep()
        val plantId = inputData.getString(PLANT_ID) ?: ""
        val api: MasterApi by inject()
        try {
            val response = api.syncInventory(plantId).execute()
            if (response.isSuccessful) {
                val data = response.body()
                if (data?.data?.isNotEmpty()!!) {
                    Result.success(workDataOf(INVENTORY_SYNC_OUTPUT_DATA to data.data))
                } else {
                    Result.failure(workDataOf(INVENTORY_SYNC_OUTPUT_DATA to data.errors))
                }

            } else if (response.body() == null) {
                Result.failure(workDataOf(INVENTORY_SYNC_OUTPUT_DATA to response.raw().message))
            } else {
                Result.failure(workDataOf(INVENTORY_SYNC_OUTPUT_DATA to response.raw().message))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(INVENTORY_SYNC_OUTPUT_DATA to error.message))
        }
    }
}
