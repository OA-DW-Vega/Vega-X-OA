package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.model.VegaDeliveryPost
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaDispatchDao
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaDispatchPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaDispatchDao by inject()
        val api: MasterApi by inject()
        val wbid = inputData.getString(DISPATCH_DATA) ?: ""
        val batchNo = inputData.getString(UIUtils.DISPATCH_BATCH) ?: ""
        val dispatchData = dao.getSingleDispatchWithLots(wbid)
        val deliveryList = mutableListOf<VegaDispatchTrucks>()
        dispatchData.dispatch.batchNumber = batchNo
        dispatchData.lineItems.forEach {
            val dispatchItem = dispatchData.dispatch.copy()
            dispatchItem.batchNumber = it.batchNumber
            dispatchItem.recStorageLocationCode = it.storageLocationCode
            if (dispatchItem.unitsOfMeasure.equals(it.unitOfMeasure))
                dispatchItem.netWeight = it.editedWeight.toString()
            else if (dispatchItem.unitsOfMeasure.equals("MT") && it.unitOfMeasure.equals("KG"))
                dispatchItem.netWeight = (it.editedWeight?.toDouble()?.div(1000)).toString()
            else if (dispatchItem.unitsOfMeasure.equals("KG") && it.unitOfMeasure.equals("MT"))
                dispatchItem.netWeight = (it.editedWeight?.toDouble()?.times(1000)).toString()
            deliveryList.add(dispatchItem)
        }

        try {
            val response =
                api.postDeliveryDetail(VegaDeliveryPost(getCurrentKey(), getPlantDetails(), batchNo, deliveryList))
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    dispatchData.dispatch.isSyncStatus = true
                    dispatchData.dispatch.isProgress = false
                    dispatchData.dispatch.message = resp.message
                    dispatchData.dispatch.status = 4
                    useCase.savedispatch(dispatchData.dispatch)
                    Result.success(workDataOf(DISPATCH_OUTPUT_DATA to resp.data.delivery))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    dispatchData.dispatch.isSyncStatus = false
                    dispatchData.dispatch.isProgress = false
                    dispatchData.dispatch.message = msg
                    dispatchData.dispatch.status = 3
                    useCase.savedispatch(dispatchData.dispatch)
                    Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to msg))
                }

            } else {
                dispatchData.dispatch.isSyncStatus = false
                dispatchData.dispatch.isProgress = false
                dispatchData.dispatch.message = response.message()
                dispatchData.dispatch.status = 3
                useCase.savedispatch(dispatchData.dispatch)
                Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to error.message))
        }
    }
}
