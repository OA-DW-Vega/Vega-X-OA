package com.olam.warehouse.ginning.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.ginning.data.api.GinningDispatchApi
import com.olam.warehouse.ginning.utils.DISPATCH_DATA
import com.olam.warehouse.ginning.utils.DISPATCH_OUTPUT_DATA
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.VegaCottonGinningDispatchDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 3/26/2020.
 */

@Suppress("UNCHECKED_CAST")
class GinningDispatchPostWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val offlineList = mutableListOf<VegaCottonGinningDispatchDelivery?>()
        var deliveryDtoVegaCotton: VegaCottonGinningDispatchDelivery? = null
//        val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
        //val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val api: GinningDispatchApi by inject()
        val daoVegaCotton: VegaCottonGinningDispatchDao by inject()

        val deliveryNo = inputData.getString(DISPATCH_DATA) ?: ""
        val balesData = daoVegaCotton.getDeliveryWithBalesOffline(deliveryNo)
        deliveryDtoVegaCotton = balesData.deliveryVegaCotton
        deliveryDtoVegaCotton.baleDTO = balesData.bales
        deliveryDtoVegaCotton.userName = PreferenceHelper.get(Constants.USER_NAME, "")

        try {
            val response = api.postOfflineDispatch(getCurrentKey(), deliveryDtoVegaCotton).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && respData.success) {
                    daoVegaCotton.deleteDelivery(deliveryNo)
                    daoVegaCotton.deleteBalesFromDB(deliveryNo)
                    Result.success(workDataOf(DISPATCH_OUTPUT_DATA to deliveryNo))
                } else {
                    val msg = resp?.data?.message
                    Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to msg))
                }
            } else {
                Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to error.message))
        }
    }
}
