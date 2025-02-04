package com.olam.warehouse.vegax.grnindo.works

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPostData
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.grnindo.data.api.VegaIndoCoffeeGrnApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 4/26/2021.
 */
@Suppress("UNCHECKED_CAST")
class VegaIndoCoffeeGrnPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaIndoCoffeeGrnApi by inject()
        val dao: VegaEcuadorGrnDao by inject()
        val tmpWbId = inputData.getString(UIUtils.TEMP_ID) ?: ""
        val grnData = dao.getGrnWBDetails(tmpWbId)
        val wbData = preparePostGrnData(grnData)
        val vegaEcuadorGrnPost = VegaEcuadorGrnPost(
            key = getCurrentKey(),
            plant = getPlantDetails(),
            grnData = listOf(wbData)
        )

        try {
            val response = api.postGrnTrans(vegaEcuadorGrnPost).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    dao.updateGrnSuccess(
                        grnData.weighBridgeId.toString(),
                        respData.grnNumber.toString(),
                        respData.batchNumber.toString(),
                        resp?.message.toString(),
                        4
                    )
                    Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to respData.weighBridgeId.toString()))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    dao.updateGrnIndo(
                        grnData.weighBridgeId.toString(),
                        grnData.grnNumber.toString(),
                        grnData.batchNumber.toString(),
                        msg.toString(),
                        3
                    )
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                }
            } else {
                val msg = response.message()
                dao.updateGrnIndo(
                    grnData.weighBridgeId.toString(),
                    grnData.grnNumber.toString(),
                    grnData.batchNumber.toString(),
                    msg.toString(),
                    3
                )
                Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))
            }
        } catch (error: Throwable) {
            dao.updateGrnIndo(
                grnData.weighBridgeId.toString(),
                grnData.grnNumber.toString(),
                grnData.batchNumber.toString(),
                error.message.toString(),
                3
            )
            Result.failure(workDataOf(UIUtils.GRN_OUTPUT_DATA to error.message))
        }
//        }
    }
}

private fun preparePostGrnData(wbDetails: VegaGrnWeighBridgeId): VegaEcuadorGrnPostData {
    val grnPost = VegaEcuadorGrnPostData()
    grnPost.batchNumber = wbDetails.batchNumber
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialCode
    grnPost.netWeight = wbDetails.netWeight
    grnPost.plant = wbDetails.plantId
    grnPost.price = wbDetails.unitPrice
    grnPost.storageLocationCode = wbDetails.storageLocationCode
    grnPost.supplierCode = wbDetails.supplierCode?.replace("000000", "000")
    grnPost.unitsOfMeasure = wbDetails.unitsOfMeasure
    grnPost.weighBridgeType = wbDetails.weighBridgeType
    grnPost.weighBridgeId = wbDetails.weighBridgeId
    grnPost.currency = "IDR"
    grnPost.purchaseDocNum = wbDetails.purchaseDocNum
    return grnPost
}
