package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaDispatchDao
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaDeliveryPost
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_REMARK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaCocoaDispatchMtntPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
                                                                                    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaCocoaDispatchDao by inject()
        val api: MasterApi by inject()
        val wbid = inputData.getString(DISPATCH_DATA) ?: ""
        val remark = inputData.getString(DISPATCH_REMARK) ?: ""
        val dispatchData = dao.getMtntWithLotSingle(wbid)
        val deliveryList = mutableListOf<VegaCocoaDispatchWB>()
        for (item in dispatchData.lineItems) {
            val vegaCocoaDispatchWB = dispatchData.dispatch.copy()
            vegaCocoaDispatchWB.batchNumber = item.batchNumber
            vegaCocoaDispatchWB.netWeight = if (item.editedWeight?.isNotEmpty()!!) item.editedWeight else item.weight
            //vegaCocoaDispatchWB.purchaseDocNum = summaryObj?.purchaseOrder?.purchaseDocNum
            //vegaCocoaDispatchWB.purchaseDocDesc = summaryObj?.purchaseOrder?.purchaseDocDesc
            vegaCocoaDispatchWB.unitsOfMeasure = item.unitOfMeasure
            vegaCocoaDispatchWB.storageLocationCode = item.storageLocationCode
            vegaCocoaDispatchWB.recStorageLocationCode = item.storageLocationCode
            vegaCocoaDispatchWB.materialCode = item.materialCode
            vegaCocoaDispatchWB.materialName = item.materialName
            vegaCocoaDispatchWB.remarks = remark
            //vegaCocoaDispatchWB.turnAroundTime = summaryObj?.duration ?: "0"
            deliveryList.add(vegaCocoaDispatchWB)
        }

        try {
            val response =
                api.postDeliveryDetail(VegaCocoaDeliveryPost(getCurrentKey(), getPlantDetails(), "",false, deliveryList))
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
                    useCase.savedispatchMtnt(dispatchData.dispatch)
                    Result.success(workDataOf(DISPATCH_OUTPUT_DATA to resp.data.delivery))
                }
                else {
                    val msg = resp?.errors ?: resp?.message
                    dispatchData.dispatch.isSyncStatus = false
                    dispatchData.dispatch.isProgress = false
                    dispatchData.dispatch.message = msg
                    dispatchData.dispatch.status = 3
                    useCase.savedispatchMtnt(dispatchData.dispatch)
                    Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to msg))
                }

            }
            else {
                dispatchData.dispatch.isSyncStatus = false
                dispatchData.dispatch.isProgress = false
                dispatchData.dispatch.message = response.message()
                dispatchData.dispatch.status = 3
                useCase.savedispatchMtnt(dispatchData.dispatch)
                Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(DISPATCH_OUTPUT_DATA to error.message))
        }
    }
}
