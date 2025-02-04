package com.olam.warehouse.vegax.ginningwarehouse.ui.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.data.api.GinningTransMasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.VegaCottonGinningDispatchDao
import com.olam.warehouse.presentation.utils.COTTON_GINNING_TRANS_DATA
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 3/26/2020.
 */
class GinningBaleTransMasterDataWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sleep()
        val wbid = inputData.getString(COTTON_GINNING_TRANS_DATA) ?: ""
        val api: GinningTransMasterApi by inject()
        val dao: VegaCottonGinningDispatchDao by inject()
        val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
        val key= getCurrentKey()
        try {
            val response = api.fetchInventoryBaleList(key).execute()
            if (response.isSuccessful) {
                val data = response.body()
                data?.let {
                    it.data.forEach { item ->
                        if (dao.isBaleExist(item.baleID).isNotEmpty()) return@forEach
                        dao.saveBaleDetails(item)
                    }
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (error: Throwable) {
            Result.retry()
        }
    }
}

class GinningTransDeliveryMasterDataWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sleep()
        val wbid = inputData.getString(COTTON_GINNING_TRANS_DATA) ?: ""
        val api: GinningTransMasterApi by inject()
        val dao: VegaCottonGinningDispatchDao by inject()
        val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
        val key= getCurrentKey()
        try {
            val response = api.fetchDeliveryDetails(key).execute()
            if (response.isSuccessful) {
                val data = response.body()
                data?.let {
                    it.data.forEach { item ->
                        item.gradeDTO.forEach { grade ->
                            grade.deliveryNumber = item.deliveryNumber
                            dao.insertOrReplaceGrade(grade)
                        }
                        if (dao.isDeliveryExistOffline(item.deliveryNumber).isNotEmpty()) return@forEach
                        item.userName = PreferenceHelper.get(Constants.USER_NAME, "")
                        dao.insertOrReplaceDelivery(item)
                    }
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (error: Throwable) {
            Result.retry()
        }
    }
}
