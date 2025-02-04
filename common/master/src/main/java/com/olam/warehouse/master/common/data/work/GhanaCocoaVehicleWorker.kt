package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.prepareVegaVehicle
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.presentation.data.api.TruckManageOffflineApi
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.VEHICLE_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class GhanaCocoaVehicleWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sleep()
        val seasonId = inputData.getString(AppUtils.SEASON_ID) ?: ""
        val api: TruckManageOffflineApi by inject()
        val dao: VegaReceivingDao by inject()
        try {
            val response = api.getTruckDetailsOffline(seasonId).execute()
            if (response.isSuccessful) {
                val data = response.body()
                val vehicleList = data?.vehicleList
                if (vehicleList != null) {
                    prepareVegaVehicle(vehicleList)
                    dao.insertVehicleDetails(prepareVegaVehicle(vehicleList))
                }
                Result.success()
            } else if (response.body() == null) {
                Result.failure(workDataOf(VEHICLE_OUTPUT_DATA to response.raw().message))
            } else {
                Result.failure(workDataOf(VEHICLE_OUTPUT_DATA to response.raw().message))
            }
        } catch (error: Throwable) {
            Result.failure(workDataOf(VEHICLE_OUTPUT_DATA to error.message))
        }
    }
}

