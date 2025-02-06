package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.model.VegaGhanaMtnrQualityPost
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import com.olam.warehouse.presentation.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaGhanaMtnrQualityPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val qualityOfflineList = mutableListOf<VegaGhanaMtnrQualityLot?>()
//        val qualityOfflineList = mutableListOf<VegaQualityWBDetails?>()
        val useCase: MasterUseCase by inject()
        val api: MasterApi by inject()
        val dao: VegaQualityDao by inject()

        var batchNo = ""
        val wbid = inputData.getString(UIUtils.QUALITY_DATA) ?: ""
//        val offlineList = dao.getWBWithQualitySingleSync(wbid)
        val wbDeatils = dao.getWBWithQualitySingleSync(wbid) //VegaQualityWBDetails
        val offlineList = dao.getGhanaMtnrLotDetails(wbDeatils.qualityWBDetails.wbTempId) //VegaCoffeeLot

        offlineList.qualityWBDetails.weighBridgeId = wbDeatils.qualityWBDetails.weighBridgeId

        val qtyParams = offlineList.quality.filter { it.qualityParameterValue!!.isNotEmpty() }

        offlineList.qualityWBDetails.qualityDetails = qtyParams
        offlineList.qualityWBDetails.qualityDetails.forEach {
            it?.wbid = wbid
        }
        batchNo = offlineList.qualityWBDetails.batchNumber.toString()

        if (offlineList.qualityWBDetails.plant.isNullOrEmpty())
            offlineList.qualityWBDetails.plant = getPlantDetails().plantId
        qualityOfflineList.add(offlineList.qualityWBDetails)

        try {
            val response =
                api.postMtnrQualityDetail(
                    VegaGhanaMtnrQualityPost(
                        grnApplicable = true,
                        grnFlag = false,
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        lotDetails = qualityOfflineList,
                        bcMessage = "",
                        charg = "",
                        currentWbid = "",
                        errorMessage = "",
                        grnNumber = ""
                    )
                )
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && resp?.success == true) {
//                    respData.currentWbid?.let {
                    useCase.saveWB(wbid, batchNo, respData.grnNumber.toString(), 4)
//                    }
                    Result.success(workDataOf(UIUtils.QUALITY_OUTPUT_DATA to resp.data.grnNumber))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    useCase.saveWB(wbid, batchNo, msg.toString(), 3)
                    Result.failure(workDataOf(UIUtils.QUALITY_OUTPUT_DATA to msg))
                }
            } else {
                useCase.saveWB(wbid, batchNo, response.message(), 3)
                Result.failure(workDataOf(UIUtils.QUALITY_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.QUALITY_OUTPUT_DATA to error.message))
        }
    }
}
