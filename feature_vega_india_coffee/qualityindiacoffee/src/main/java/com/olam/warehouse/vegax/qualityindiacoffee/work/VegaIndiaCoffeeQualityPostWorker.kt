package com.olam.warehouse.vegax.qualityindiacoffee.work


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.vegax.qualityindiacoffee.data.api.VegaIndiaCoffeeQualityApi
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPost
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.usecase.VegaIndiaCoffeeQualityUseCase
import com.olam.warehouse.vegax.qualityindiacoffee.utils.QUALITY_DATA
import com.olam.warehouse.vegax.qualityindiacoffee.utils.QUALITY_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject


@Suppress("UNCHECKED_CAST")
class VegaIndiaCoffeeQualityPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val qualityOfflineList = mutableListOf<VegaQualityWBDetails?>()
        val useCaseIndiaCoffee: VegaIndiaCoffeeQualityUseCase by inject()
        val apiIndiaCoffee: VegaIndiaCoffeeQualityApi by inject()
        val dao: VegaQualityDao by inject()
        val wbid = inputData.getString(QUALITY_DATA) ?: ""
        val offlineList = dao.getWBWithQualitySingle(wbid)
        offlineList.forEach {
            it.qualityWBDetails.qualityDetails = it.quality
            qualityOfflineList.add(it.qualityWBDetails)
        }
        //val quality = Gson().fromJson<QualityPost>(qualityJson)
        try {
            val response =
                apiIndiaCoffee.postQualityDetail(
                    VegaIndiaCoffeeQualityPost(
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        lotDetails = qualityOfflineList
                    )
                )
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && respData.success) {
                    respData.currentWbid?.let {
                        useCaseIndiaCoffee.updateWBDB(
                            it,
                            respData.charg.toString(),
                            respData.message.toString(),
                            4
                        )
                    }
                    Result.success(workDataOf(QUALITY_OUTPUT_DATA to resp?.data?.charg))
                } else {
                    Result.failure(workDataOf(QUALITY_OUTPUT_DATA to resp?.message))
                }
            } else {
                Result.failure(workDataOf(QUALITY_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(QUALITY_OUTPUT_DATA to error.message))
        }
    }
}
