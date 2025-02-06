package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.UIUtils.QUALITY_DATA
import com.olam.warehouse.presentation.utils.UIUtils.QUALITY_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaQualityPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val qualityOfflineList = mutableListOf<VegaQualityWBDetails?>()
        val useCase: MasterUseCase by inject()
        val api: MasterApi by inject()
        val dao: VegaQualityDao by inject()
        var paidWeight: String = ""
        var grnQty: Double = 0.0
        var refraction: Double = 0.0
        var discWeight: Double = 0.0

        var batchNo = ""
        val wbid = inputData.getString(QUALITY_DATA) ?: ""
        val offlineList = dao.getWBWithQualitySingleSync(wbid)
        val qtyParams = offlineList.quality.filter { it.qualityParameterValue!!.isNotEmpty() }
        qtyParams.forEach { it ->
            if (it.nameChar.equals("B_GRNQTY1")) {
                grnQty = it.qualityParameterValue?.toDouble() ?: 0.0
            } else if (it.nameChar.equals("B_SECONDARY_REFR")) {
                refraction = it.qualityParameterValue?.toDouble() ?: 0.0
            }
        }
        discWeight = 100 - refraction
        paidWeight = ((discWeight * grnQty) / 100).formatThreeDigits()

        offlineList.qualityWBDetails.qualityDetails = qtyParams
        batchNo = offlineList.qualityWBDetails.batchNumber.toString()
        if (offlineList.qualityWBDetails.plant.isNullOrEmpty())
            offlineList.qualityWBDetails.plant = getPlantDetails().plantId
        offlineList.qualityWBDetails.paidWeight = paidWeight
        qualityOfflineList.add(offlineList.qualityWBDetails)
        //val quality = Gson().fromJson<QualityPost>(qualityJson)

        try {
            val response =
                api.postQualityDetail(
                    VegaQualityPost(
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        lotDetails = qualityOfflineList
                    )
                )
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && resp?.success == true) {
                    respData.currentWbid?.let {
                        useCase.saveWB(it, batchNo, respData.charg.toString(), 4)
                    }
                    Result.success(workDataOf(QUALITY_OUTPUT_DATA to resp.data.charg))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    useCase.saveWB(wbid, batchNo, msg.toString(), 3)
                    Result.failure(workDataOf(QUALITY_OUTPUT_DATA to msg))
                }
            } else {
                useCase.saveWB(wbid, batchNo, response.message(), 3)
                Result.failure(workDataOf(QUALITY_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(QUALITY_OUTPUT_DATA to error.message))
        }
    }
}
