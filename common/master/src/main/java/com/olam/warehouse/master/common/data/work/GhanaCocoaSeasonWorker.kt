package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.presentation.data.api.TruckManageOffflineApi
import com.olam.warehouse.presentation.data.domain.model.TruckManagementData
import com.olam.warehouse.presentation.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.SimpleDateFormat
import java.util.*

class GhanaCocoaSeasonWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sleep()
        val api: TruckManageOffflineApi by inject()
        try {
            var seasonList = mutableListOf<TruckManagementData>()
            val response = api.getSeasonDetailsOffline().execute()
            if (response.isSuccessful) {
                val data = response.body()
                if (data?.seasonList?.isNotEmpty() == true) {
                    data.let { it1 ->
                        seasonList = it1.seasonList as MutableList<TruckManagementData>
                    }
                    var currentSeason: String = fetchCurrentSeason(seasonList)
                    Result.success(workDataOf(SEASON_OUTPUT_DATA to currentSeason))
                } else {
                    Result.failure(workDataOf(SEASON_OUTPUT_DATA to (data?.statusMessage ?: "")))
                }
            } else if (response.body() == null) {
                Result.failure(workDataOf(SEASON_OUTPUT_DATA to response.raw().message))
            } else {
                Result.failure(workDataOf(SEASON_OUTPUT_DATA to response.raw().message))
            }

        } catch (error: Throwable) {
            Result.retry()
        }
    }

    private fun fetchCurrentSeason(seasonData: List<TruckManagementData>): String {
        val currentDate = Calendar.getInstance().time
        var currentSeason: String = ""
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var seasonList = seasonData as MutableList<TruckManagementData>
        seasonList.forEach {
            var startDate = sdf.parse(it.startingPeriod.split("T")[0])
            var endDate = sdf.parse(it.endingPeriod.split("T")[0])

            if (currentDate.after(startDate) && currentDate.before(endDate)) {
                currentSeason = it.seasonID
                return@forEach
            }
        }
        return currentSeason
    }
}

