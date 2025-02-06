package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.prepareVegaVendor
import com.olam.warehouse.master.user.model.Key
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.presentation.utils.VENDORY_SYNC_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 4/7/2020.
 */
class VendorSyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Data downloading...", applicationContext)
        sleep()
        val key = Key()
        key.backOffice = getCurrentKey().split("_")[3]
        key.product = getCurrentKey().split("_")[2]
        key.companyCode = getCurrentKey().split("_")[1]
        key.receptionType = getCurrentKey().split("_")[0]
        key.keys = listOf(getCurrentKey())
        val api: MasterApi by inject()
        val dao: VegaReceivingDao by inject()
        try {
            val response = api.syncVendor(key, true).execute()
            if (response.isSuccessful) {
                val data = response.body()
                val vendorList = data?.data?.toList() ?: emptyList()
                dao.insertVendor(prepareVegaVendor(vendorList))
                Result.success(workDataOf(VENDORY_SYNC_OUTPUT_DATA to data?.message))
            } else if (response.body() == null) {
                Result.failure(workDataOf(VENDORY_SYNC_OUTPUT_DATA to response.raw().message))
            } else {
                Result.failure(workDataOf(VENDORY_SYNC_OUTPUT_DATA to response.raw().message))
            }
        } catch (error: Throwable) {
            Result.failure(workDataOf(VENDORY_SYNC_OUTPUT_DATA to error.message))
        }
    }
}
