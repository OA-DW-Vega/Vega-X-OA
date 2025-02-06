package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.model.VegaCocoaNoWeighmentWithLots
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaDispatchDao
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentModel
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaVirtualDeliveryDetail
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaVirtualPostRequest
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class VegaCocoaVirtualPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    lateinit var model: VegaCocoaNoWeighmentWithLots
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val api: MasterApi by inject()
        val dao: VegaCocoaDispatchDao by inject()
        val wbid = inputData.getString(UIUtils.MTNT_VIRTUAL_DATA) ?: ""
        var bagList = dao.getBagItemsForWorker()
        val offlineList = dao.getNoWeighmentWithLotForWorker(wbid)
        model = offlineList
        val wbData = prepareDeliveryList(offlineList.lineItems, bagList, offlineList.dispatch)
        try {
            val response =
                api.postVirtualDeliveryDetails(
                    VegaCocoaVirtualPostRequest(
                        "",
                        "",
                        wbData,
                        getCurrentKey(),
                        "",
                        "",
                        offlineList.dispatch.vehicleNumber,
                        offlineList.dispatch.driverPhoneNumber,
                        offlineList.dispatch.driverName,
                        offlineList.dispatch.imageString ?: "",
                        getPlantDetails()
                    )
                ).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val originalData = response.body()?.data
                if (originalData != null && resp?.success == true) {
                    updateModel(originalData)
                    /*dao.updateVirtualDispatchStatusByWorker(model.dispatch.weighBridgeId)*/
                    model.dispatch.isSyncStatus = true
                    model.lineItems.forEach { it.isSyncStatus = true }
                    dao.insertNoWeighmentDetail(model.dispatch)
                    dao.saveNoWeighmentLotsInWork(model.lineItems)
                    Result.success(workDataOf(UIUtils.MTNT_VIRTUAL_OUTPUT_DATA to resp.data[0].delivery))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    updateModel(originalData!!)
                    model.dispatch.message = msg
                    dao.insertNoWeighmentDetail(offlineList.dispatch)
                    dao.saveNoWeighmentLotsInWork(model.lineItems)
                    Result.failure(workDataOf(UIUtils.MTNT_VIRTUAL_OUTPUT_DATA to msg))
                }
            } else {
                Result.failure(workDataOf(UIUtils.MTNT_VIRTUAL_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.MTNT_VIRTUAL_OUTPUT_DATA to error.message))
        }
    }

    private fun updateModel(response: List<VegaCocoaVirtualDeliveryDetail>) {
        val success = response
        for (item in success) {
            model.lineItems.single { it.batchNumber == item.batchNumber }.apply {
                weighScaleWbId = item.weighBridgeId
                pickingFlag = item.pickingFlag
                deliveryFlag = item.deliveryFlag
                deliveryItem = item.deliveryItem
                delivery = item.delivery
                isPgiFlag = item.pgiFlag
                storageLossFlag = item.storageLossFlag
                region = item.documentNum
            }
        }
    }
}

private fun prepareDeliveryList(
    dispatchLotsList: List<VegaCocoaNoWeighmentLot>,
    bagList: List<VegaCocoaSweepingBagMaterial>,
    summaryObj: VegaCocoaNoWeighmentModel
): List<VegaCocoaVirtualDeliveryDetail> {
    val list = ArrayList<VegaCocoaVirtualDeliveryDetail>()
    val year: Int = Calendar.getInstance().get(Calendar.YEAR)
    for (item in dispatchLotsList) {
        val deliveryDetail = VegaCocoaVirtualDeliveryDetail()
        var bagItems = bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode }
        deliveryDetail.batchNumber = item.batchNumber
        deliveryDetail.materialCode = item.materialCode
        deliveryDetail.plantId = item.plantId
        deliveryDetail.netWeight = if (item.unitOfMeasure.equals("MT")) convertKgToMT(
            item.editedWeight.toString().trim()
        ) else item.editedWeight.toString().trim()
        deliveryDetail.endLotFlag = item.isEndLot ?: false
        deliveryDetail.createdDate = summaryObj.erdat
        deliveryDetail.purchaseDocNum = summaryObj.purchaseDocNum
        deliveryDetail.purchaseDocDesc = summaryObj.purchaseDocDesc
        deliveryDetail.recStorageLocationCode = item.storageLocationCode
        deliveryDetail.storageLocationCode = item.storageLocationCode
        deliveryDetail.unitsOfMeasure = item.unitOfMeasure
        deliveryDetail.startTime = summaryObj.startTime
        deliveryDetail.endTime = summaryObj.endTime
        deliveryDetail.turnAroundTime = summaryObj.turnAroundTime
        deliveryDetail.weighBridgeId = item.weighScaleWbId ?: ""
        deliveryDetail.deliveryFlag = item.deliveryFlag
        deliveryDetail.pickingFlag = item.pickingFlag
        deliveryDetail.pgiFlag = item.isPgiFlag
        deliveryDetail.storageLossFlag = item.storageLossFlag
        deliveryDetail.delivery = item.delivery
        deliveryDetail.deliveryItem = item.deliveryItem
        deliveryDetail.wayBillNo = summaryObj.wayBillNo
        deliveryDetail.toVendorCode = summaryObj.transportVendorID
        deliveryDetail.year = year.toString()
        val grossWeight = summaryObj.truckOutWeight
        var bagTareWeight = 0.0
        bagItems.forEach { item1 ->
            bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
        }

        deliveryDetail.grossWeight = if (deliveryDetail.unitsOfMeasure.equals("MT")) convertKgToMT(
            grossWeight.toString().trim()
        ) else grossWeight.toString().trim()//
        deliveryDetail.tareWeight =
            deliveryDetail.grossWeight?.toDouble()?.minus(deliveryDetail.netWeight!!.toDouble())
                ?.plus(bagTareWeight).toString()
        if (bagItems.isNotEmpty()) {
            val bagSort = mutableListOf<VegaCocoaSweepingBagMaterial>()
            val dat = bagItems.sortedByDescending { it.createdPosition }
            bagSort.addAll(dat)
            bagItems = bagSort.asReversed()
            deliveryDetail.huno = bagItems[0].unitsOfMeasure
            deliveryDetail.huwt = bagTareWeight.toString().trim()
            deliveryDetail.huno2 = "KG"
            deliveryDetail.huwt2 = bagItems[0].palletAverage
            deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
            deliveryDetail.nohu2 = bagItems[0].noOfPallet
            deliveryDetail.bagList = bagItems
        } else {
            deliveryDetail.huno2 = summaryObj.unitsOfMeasure
            deliveryDetail.grossWeight = grossWeight.toString().trim()
        }
        list.add(deliveryDetail)
    }
    return list
}

fun convertKgToMT(weight: String): String {
    return weight.toDouble().div(1000).formatThreeDigits()
}

fun convertKgToMTNigeria(weight: String): String {
    return (weight.toDouble().div(1000)).toString()
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}
