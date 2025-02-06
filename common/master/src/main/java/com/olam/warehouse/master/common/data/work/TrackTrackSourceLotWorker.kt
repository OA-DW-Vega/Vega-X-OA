package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.dao.TrackTraceDao
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.FARMER_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.SOURCE_LOT_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TrackTrackSourceLotWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO){
        val api: MasterApi by inject()
        val trackTraceDao: TrackTraceDao by inject()

        try {
            val response = api.getSourceLotDetails(getCurrentKey(),null).execute()
            if (response.isSuccessful) {
                trackTraceDao.clearTTSourceLotDetails()
                val data = response.body()
                data?.data?.let { trackTraceDao.insertMultipleSourceLotDetails(it) }
                Result.success()
            } else if (response.body() == null) {
                Result.failure(workDataOf(SOURCE_LOT_OUTPUT_DATA to response.raw().message))
            } else {
                Result.retry()
            }
            //Result.success()
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.failure(workDataOf(SOURCE_LOT_OUTPUT_DATA to error.message))
        }
    }
}
