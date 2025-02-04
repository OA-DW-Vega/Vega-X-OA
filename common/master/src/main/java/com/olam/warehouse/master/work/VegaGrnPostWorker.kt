package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPostData
import com.olam.warehouse.presentation.utils.UIUtils.GRN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.GRN_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaGrnPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val api: MasterApi by inject()
        val dao: VegaEcuadorGrnDao by inject()
        val wbid = inputData.getString(GRN_DATA) ?: ""
        val offlineList = dao.getOfflineWeighBridgeDetail(wbid)
        val wbData = preparePostGrnData(offlineList)
        try {
            val response =
                api.postGrn(
                    VegaEcuadorGrnPost(
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        grnData = listOf(wbData)
                    )
                ).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && resp?.success == true) {
                    useCase.updateGrn(
                        wbid,
                        respData.grnNumber.toString(),
                        respData.batchNumber.toString(),
                        "GRN Created Successfully",
                        4
                    )
                    useCase.updateGrnNoToQuality(wbid, respData.grnNumber.toString(), respData.batchNumber.toString())
                    Result.success(workDataOf(GRN_OUTPUT_DATA to resp.data.grnNumber))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    useCase.updateGrn(wbid, "", "", msg.toString(), 3)
                    Result.failure(workDataOf(GRN_OUTPUT_DATA to msg))
                }
            } else {
                useCase.updateGrn(wbid, "", "", response.message(), 3)
                Result.failure(workDataOf(GRN_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(GRN_OUTPUT_DATA to error.message))
        }
    }
}

fun preparePostGrnData(wbDetails: VegaGrnWeighBridgeId): VegaEcuadorGrnPostData {
    val grnPost = VegaEcuadorGrnPostData()
    grnPost.batchNumber = wbDetails.batchNumber
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialCode
    grnPost.netWeight = wbDetails.netWeight
    grnPost.plant = wbDetails.plantId
    grnPost.price = wbDetails.unitPrice
    grnPost.storageLocationCode = "1000" //wbDetails.storageLocationCode
    grnPost.supplierCode = wbDetails.supplierCode
    grnPost.unitsOfMeasure = wbDetails.unitsOfMeasure
    grnPost.weighBridgeType = wbDetails.weighBridgeType
    grnPost.weighBridgeId = wbDetails.weighBridgeId
    grnPost.purchaseDocNum = wbDetails.purchaseDocNum
    return grnPost
}
