package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorDispatchDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPostResponse
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchLotsMerge
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_POST_DATA
import com.olam.warehouse.presentation.utils.fromJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Keerthi Santhanam on 8/2/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaEcuadorDispatchPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaEcuadorDispatchDao by inject()
        val api: MasterApi by inject()
        val deliveryId = inputData.getString(DISPATCH_DATA) ?: ""
        //Passing deliveryDetails[] from previously failed response for resync case
        val inputResponseObj =
            Gson().fromJson<VegaEcuadorDeliveryPostResponse>(inputData.getString(DISPATCH_POST_DATA).toString())
        val inputDeliveryDetails = inputResponseObj?.deliveryDetails ?: emptyList()
        val dispatchData = dao.getDispatchWithLineItem(deliveryId)
        val mergedLotsMap = mutableMapOf<Int, ArrayList<VegaEcuadorDispatchLots>>()
        val deliveryDetails = ArrayList<VegaEcuadorDispatchLotsMerge>()
        if (inputDeliveryDetails.isNullOrEmpty()) {
            deliveryDetails.clear()
            dispatchData.lineItems.forEach label@{
                if (it.pairId!! <= 0)
                    return@label
                when (val mergedDispatchLotList = mergedLotsMap[it.pairId!!]) {
                    null -> {
                        val list = ArrayList<VegaEcuadorDispatchLots>()
                        list.add(it)
                        mergedLotsMap[it.pairId!!] = list
                    }
                    else -> {
                        mergedDispatchLotList.add(it)
                        mergedLotsMap[it.pairId!!] = mergedDispatchLotList
                    }
                }
            }
            for ((key, dispatchLotList) in mergedLotsMap) {
                val item = VegaEcuadorDispatchLotsMerge()
                if (dispatchLotList.isNotEmpty())
                    item.batchNumber = dispatchLotList[0].binBatch.toString()
                item.lots = ArrayList<VegaEcuadorDispatchLots>()
                item.lots.addAll(dispatchLotList)
                deliveryDetails.add(item)
            }

            val unpaired = dispatchData.lineItems.filter { it.pairId == 0 }
            unpaired.forEach {
                val item = VegaEcuadorDispatchLotsMerge()
                item.batchNumber = it.batchNumber
                val list = ArrayList<VegaEcuadorDispatchLots>(1)
                list.add(it)
                item.lots.addAll(list)
                deliveryDetails.add(item)
            }
        } else {
            deliveryDetails.addAll(inputDeliveryDetails)
        }
        try {
            val response =
                api.postDeliveryDetail(
                    VegaEcuadorDeliveryPost(
                        getCurrentKey(),
                        getPlantDetails(),
                        dispatchData.dispatch.binFormation,
                        dispatchData.dispatch.delivery,
                        dispatchData.dispatch.pgi,
                        dispatchData.dispatch.picking,
                        deliveryDetails
                    )
                )
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess && respData.delivery && respData.picking && respData.pgi) {
                    dispatchData.dispatch.deliveryId = respData.deliveryDetails[0].deliveryId
                    dispatchData.dispatch.isSynced = true
                    dispatchData.dispatch.isProgress = false
                    dispatchData.dispatch.syncStatusMsg = respData.message
                    dispatchData.dispatch.status = Status.MTNT_COMPLETED
                    dispatchData.dispatch.binFormation = respData.binFormation
                    dispatchData.dispatch.delivery = respData.delivery
                    dispatchData.dispatch.pgi = respData.pgi
                    dispatchData.dispatch.picking = respData.picking
                    useCase.updateDispatchStatus(dispatchData.dispatch)
                    Result.success(workDataOf(DISPATCH_OUTPUT_DATA to respData.deliveryDetails[0].deliveryId))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    dispatchData.dispatch.isSynced = false
                    dispatchData.dispatch.isProgress = false
                    dispatchData.dispatch.status = Status.SYNC_ERROR
                    if (respData != null && isSuccess) {
                        dispatchData.dispatch.syncStatusMsg = respData.message
                        dispatchData.dispatch.binFormation = respData.binFormation
                        dispatchData.dispatch.delivery = respData.delivery
                        dispatchData.dispatch.pgi = respData.pgi
                        dispatchData.dispatch.picking = respData.picking
                        dispatchData.dispatch.deliveryDetail = Gson().toJson(respData)
                        useCase.updateDispatchStatus(dispatchData.dispatch)
                        Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to Gson().toJson(respData)))
                    } else {
                        dispatchData.dispatch.syncStatusMsg = msg.toString()
                        useCase.updateDispatchStatus(dispatchData.dispatch)
                        Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to dispatchData.dispatch.syncStatusMsg))
                    }
                }

            } else {
                dispatchData.dispatch.isSynced = false
                dispatchData.dispatch.isProgress = false
                dispatchData.dispatch.syncStatusMsg = response.message()
                dispatchData.dispatch.status = Status.SYNC_ERROR
                useCase.updateDispatchStatus(dispatchData.dispatch)
                Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            dispatchData.dispatch.syncStatusMsg = error.message.toString()
            dispatchData.dispatch.status = Status.SYNC_ERROR
            useCase.updateDispatchStatus(dispatchData.dispatch)
            Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to error.message))
        }
    }
}
