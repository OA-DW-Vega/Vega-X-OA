package com.olam.warehouse.odquality.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.dao.DOQualityDao
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.data.api.DOQualityApi
import com.olam.warehouse.odquality.data.domain.model.DOQualityPost
import com.olam.warehouse.odquality.data.domain.usecase.DOQualityUseCase
import com.olam.warehouse.odquality.utils.QUALITY_DATA
import com.olam.warehouse.odquality.utils.QUALITY_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
@Suppress("UNCHECKED_CAST")
class DOQualityPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val qualityOfflineList = mutableListOf<DOQualityWBDetails?>()
        val useCase: DOQualityUseCase by inject()
        val api: DOQualityApi by inject()
        val dao: DOQualityDao by inject()
        val wbid = inputData.getString(QUALITY_DATA) ?: ""
        val offlineList = dao.getWBWithQualitySingle(wbid)
        offlineList.forEach {
            it.qualityWBDetails.qualityDetails = it.quality
            if (it.qualityWBDetails.plant.isNullOrEmpty()) it.qualityWBDetails.plant = getPlantDetails().plantId
            qualityOfflineList.add(it.qualityWBDetails)
        }
        //val quality = Gson().fromJson<QualityPost>(qualityJson)
        try {
            val response =
                api.postQualityDetail(
                    DOQualityPost(
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
                    respData.currentWbid?.let { useCase.updateWBDB(it) }
                    Result.success(workDataOf(QUALITY_OUTPUT_DATA to resp?.data?.charg))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    Result.failure(workDataOf(QUALITY_OUTPUT_DATA to msg))
                }
            } else {
                Result.failure(workDataOf(QUALITY_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(QUALITY_OUTPUT_DATA to error.message))
        }
    }
}
