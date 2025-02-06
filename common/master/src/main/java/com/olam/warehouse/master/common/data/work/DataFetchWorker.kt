package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.dorigin.dao.DOQualityDao
import com.olam.warehouse.master.dorigin.dao.DOReceivingDao
import com.olam.warehouse.master.vegacocoa.dao.VegaCoCoaOffloadDao
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaDispatchDao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.DO_QUALITY_OFFLINE_DATA
import com.olam.warehouse.presentation.utils.DO_RECEIVING_OFFLINE_DATA
import com.olam.warehouse.presentation.utils.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DataFetchWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Data downloading...", applicationContext)
        //sleep()
        val receivingDao: DOReceivingDao by inject()
        val qualityDao: DOQualityDao by inject()
        try {
            val receivingData = receivingDao.getOfflineDOReceivingCount()
            val qualityData = qualityDao.getOfflineDOQualityWBDetailsCount()
            Result.success(
                workDataOf(
                    DO_RECEIVING_OFFLINE_DATA to receivingData,
                    DO_QUALITY_OFFLINE_DATA to qualityData
                )
            )

        } catch (error: Throwable) {
//            Result.retry()
            Result.failure()
        }
    }
}

class CocoaDataFetchWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Data downloading...", applicationContext)
        sleep()
        val receivingDao: VegaCocoaDispatchDao by inject()
        val qualityDao: VegaCoCoaOffloadDao by inject()
        try {
            val receivingData = receivingDao.getOfflinePendingCount()
            val qualityData = qualityDao.getOfflinePendingCount()
            Result.success(
                workDataOf(
                    DO_RECEIVING_OFFLINE_DATA to receivingData,
                    DO_QUALITY_OFFLINE_DATA to qualityData
                )
            )

        } catch (error: Throwable) {
            Result.retry()
        }
    }
}

class NicaraguaDataFetchWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Data downloading...", applicationContext)
        sleep()
        val receivingDao: VegaNicaraguaGrnDao by inject()
        try {
            val receivingData = receivingDao.getOfflinePendingCount(Status.SYNC_PENDING)
            Result.success(
                workDataOf(
                    DO_RECEIVING_OFFLINE_DATA to receivingData
                )
            )

        } catch (error: Throwable) {
            Result.retry()
        }
    }
}
