package com.olam.warehouse.vegax.offloadingindo.ui.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeOffloadDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeReceivingItemWithBags
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.TEMP_ID
import com.olam.warehouse.presentation.utils.UIUtils.WB_ID
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.offloadingindo.data.api.VegaIndoCoffeeOffloadingApi
import com.olam.warehouse.vegax.offloadingindo.data.domain.model.VegaIndoCoffeeOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingindo.data.domain.model.VegaIndoCoffeeOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingindo.data.domain.model.VegaIndoCoffeeOffloadingSupplierPostRequest
import com.olam.warehouse.vegax.offloadingindo.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 4/22/2021.
 */
@Suppress("UNCHECKED_CAST")
class VegaIndoCoffeeOffloadingPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaIndoCoffeeOffloadingApi by inject()
        val dao: VegaCoffeeOffloadDao by inject()
        val tmpWbId = inputData.getString(TEMP_ID) ?: ""
        val wbType = inputData.getString(WB_ID) ?: ""
        val mtnrData = dao.getOBDDetailsIndoOffline(tmpWbId)
        val supplierData = dao.getOBDDetailsSupIndoOffline(tmpWbId)

        try {
            if (wbType.equals(PROCURE)) {
                val response = api.postOffloadingSupplierDetailOffline(prepareSupplierPostData(supplierData)).execute()
                if (response.isSuccessful) {
                    val resp = response.body()
                    val respData = response.body()?.data
                    if (respData != null) {
                        dao.updateTempIdToWbid(resp?.data?.wbId.toString(), tmpWbId)
                        dao.updateOffloadingCompleteSuccess(
                            tmpWbId,
                            "1",
                            resp?.message.toString(),
                            resp?.data?.wbId.toString(),
                            Status.SYNC_COMPLETED,
                            resp?.data?.grnNumber.toString(),
                            true
                        )
                        Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to resp?.data?.wbId))
                    } else {
                        val msg = resp?.message ?: resp?.errors
                        dao.updateOffloadingComplete(tmpWbId, "1", msg.toString())
                        Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                    }
                } else {
                    val msg = response.message()
                    dao.updateOffloadingComplete(tmpWbId, "1", msg.toString())
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))
                }
            } else {
                val response = api.postOffloadingDetailOffline(prepareMtnrPost(mtnrData)).execute()
                if (response.isSuccessful) {
                    val resp = response.body()
                    val respData = response.body()?.data
                    if (respData != null) {
                        var status: Status = Status.SYNC_PENDING
                        var isSync = resp?.data?.wbFlag == true && resp.data.qcFlag && resp.data.grnFlag
                        status = if(resp?.data?.wbFlag == true && resp.data.qcFlag && resp.data.grnFlag) Status.SYNC_COMPLETED else Status.SYNC_ERROR
                        dao.updateTempIdToWbid(resp?.data?.wbId.toString(), tmpWbId)
                        dao.updateOffloadingCompleteSuccess(
                            tmpWbId,
                            "1",
                            resp?.message.toString(),
                            resp?.data?.wbId.toString(),
                            status,
                            resp?.data?.grnNumber.toString(),
                            isSync
                        )
                        if(resp?.data?.deliveryDetails?.isNotEmpty()==true) {
                            resp.data.deliveryDetails.forEach {
                                dao.updateLotStatus(
                                    it.batchNumber.toString(),
                                    resp.data.wbFlag,
                                    resp.data.qcFlag,
                                    resp.data.grnFlag
                                )
                            }
                        }
                        Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to resp?.data?.wbId))
                    } else {
                        val msg = resp?.message ?: resp?.errors
                        dao.updateOffloadingComplete(tmpWbId, "1", msg.toString())
                        if(resp?.data?.deliveryDetails?.isNotEmpty()==true) {
                            resp.data.deliveryDetails.forEach {
                                dao.updateLotStatus(
                                    it.batchNumber.toString(),
                                    resp.data.wbFlag,
                                    resp.data.qcFlag,
                                    resp.data.grnFlag
                                )
                            }
                        }
                        Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                    }
                } else {
                    val msg = response.message()
                    dao.updateOffloadingComplete(tmpWbId, "1", msg.toString())
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))
                }
            }


        } catch (error: Throwable) {
            dao.updateOffloadingComplete(tmpWbId, "1", error.message.toString())
            Result.failure(workDataOf(UIUtils.GRN_OUTPUT_DATA to error.message))
        }
//        }
    }
}

private fun prepareSupplierPostData(supplierData: VegaIndoCoffeeReceivingItemWithBags): VegaIndoCoffeeOffloadingSupplierPostRequest {
    val vegaCoffeeReceivingData = supplierData.receiving
    val bagList = supplierData.bagItem
    val postRequest = VegaIndoCoffeeOffloadingSupplierPostRequest(
        bagList,
        getCurrentKey(),
        if (vegaCoffeeReceivingData.imageString == WEIGHBRIDGE_WEIHSCALE) MISC else PROCURE,
        getPlantDetails(),
        /*vegaCoffeeReceivingData.weighBridgeId*/"",
        vegaCoffeeReceivingData.delivery,
        vegaCoffeeReceivingData.grossWeight,
        vegaCoffeeReceivingData.deliveryItem,
        vegaCoffeeReceivingData.purchaseDocNum,
        vegaCoffeeReceivingData.purchaseDocDesc,
        vegaCoffeeReceivingData.purchaseDocQty,
        vegaCoffeeReceivingData.materialCode,
        vegaCoffeeReceivingData.materialName,
        vegaCoffeeReceivingData.netWeight,
        vegaCoffeeReceivingData.supplierCode,
        vegaCoffeeReceivingData.supplierName,
        unitsOfMeasure = vegaCoffeeReceivingData.unitsOfMeasure,
        wsGate = WS01/*vegaCoffeeReceivingData.wsGate*/,
        truckDirection = vegaCoffeeReceivingData.truckDirection,
        vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
        driverName = vegaCoffeeReceivingData.truckDriverName,
        contactNumber = vegaCoffeeReceivingData.contactNumber,
        erdat = vegaCoffeeReceivingData.erdat,
        ertim = vegaCoffeeReceivingData.ertim,
        direction = vegaCoffeeReceivingData.direction,
        storageLocationCode = vegaCoffeeReceivingData.storageLocationCode?.trim(),
        storageLocationName = vegaCoffeeReceivingData.storageLocationName,
        dstorageLocationCode = vegaCoffeeReceivingData.dstorageLocationCode,
        dstorageLocationName = vegaCoffeeReceivingData.dstorageLocationName,
        declaredBagCount = vegaCoffeeReceivingData.bagCount,
        declaredWeight = vegaCoffeeReceivingData.vendorDeclaredWeight,
        vendorDeclaredWeight = vegaCoffeeReceivingData.vendorDeclaredWeight,
        origin = vegaCoffeeReceivingData.origin,
        department = vegaCoffeeReceivingData.department,
        plantName = getPlantDetails().plantName,
        item = "1"
    )
    return postRequest
}

private fun prepareMtnrPost(mtnrData: VegaIndoCoffeeReceivingMtnrWithLots): VegaIndoCoffeeOffloadingPostRequest {
    val vegaCoffeeReceivingData = mtnrData.receiving
    return VegaIndoCoffeeOffloadingPostRequest(
        key = getCurrentKey(),
        plant = getPlantDetails(),
        operatorName = "",
        batchNumber = "",
        delFlag = "",
        deliveryDetails = prepareDeliveryList(mtnrData),
        weighmentType = if (vegaCoffeeReceivingData.imageString == (WEIGHBRIDGE_WEIHSCALE)) MISC else STO,
        vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
        vehicleType = vegaCoffeeReceivingData.vehicleType,
        contactNumber = vegaCoffeeReceivingData.contactNumber,
        driverName = vegaCoffeeReceivingData.driverName
    )
}

private fun prepareDeliveryList(mtnrData: VegaIndoCoffeeReceivingMtnrWithLots): List<VegaIndoCoffeeOffloadingDeliveryDetail> {
    val vegaCoffeeReceivingData = mtnrData.receiving
    val batchList = mtnrData.lineItems.map { it.lots }
    val bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
    mtnrData.lineItems.forEach { bagList.addAll(it.bagItem) }
    val list = ArrayList<VegaIndoCoffeeOffloadingDeliveryDetail>()
    val year: Int = Calendar.getInstance().get(Calendar.YEAR)
    for (item in batchList) {
        val deliveryDetail = VegaIndoCoffeeOffloadingDeliveryDetail()
        var bagItems = bagList.filter { it.batchNumber == item.batch }
        deliveryDetail.batchNumber = item.batch
        deliveryDetail.materialCode = item.materialNumber
        deliveryDetail.plantId = item.plantId
        deliveryDetail.remarks = vegaCoffeeReceivingData.remarks
        deliveryDetail.netWeight = item.editedWeight?.trim() ?: "0.0"
        deliveryDetail.recStorageLocationCode = vegaCoffeeReceivingData.storageLocationCode?.trim()
        deliveryDetail.storageLocationCode = vegaCoffeeReceivingData.supplierCode?.trim()
        deliveryDetail.purchaseDocNum = vegaCoffeeReceivingData.purchaseDocNum
        deliveryDetail.purchaseDocDesc = vegaCoffeeReceivingData.purchaseDocDesc
        deliveryDetail.delivery = item.delivery
        deliveryDetail.deliveryItem = item.posnr
        deliveryDetail.deliveryFlag = item.deliveryFlag
        deliveryDetail.pickingFlag = item.pickingFlag
        deliveryDetail.wbFlag = item.wbFlag ?: false
        deliveryDetail.qcFlag = item.qcFlag ?: false
        deliveryDetail.grnFlag = item.grnFlag ?: false
        deliveryDetail.unitsOfMeasure = item.uom
        deliveryDetail.year = year.toString()
        deliveryDetail.wsGate = WS01
        deliveryDetail.weighBridgeId = ""/*vegaCoffeeReceivingData.weighBridgeId*/
        //Bag and pallet details
        var grossWeight = 0.0
        var tareWeight = 0.0
        var bagTareWeight = 0.0
        var startTime = ""
        var endTime = ""
        bagItems.forEach { item1 ->
            val palletAvg =
                if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                    ?.div(item1.noOfPallet?.toInt()!!) else 0.0
            grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                .plus(palletAvg!!)
            bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
        }
        deliveryDetail.grossWeight = grossWeight.toString().trim()
        deliveryDetail.startTime = vegaCoffeeReceivingData.startTime
        deliveryDetail.endTime = vegaCoffeeReceivingData.endTime
        deliveryDetail.turnAroundTime = vegaCoffeeReceivingData.turnAroundTime
        if (bagItems.isNotEmpty()) {
            val bagSort = mutableListOf<VegaCoffeeOffloadingBagMaterial>()
            val dat = bagItems.sortedByDescending { it.createdPosition }
            bagSort.addAll(dat)
            bagItems = bagSort.asReversed()
            deliveryDetail.huno = bagItems[0].unitsOfMeasure
            deliveryDetail.huwt = bagTareWeight.formatThreeDigits().toString().trim()
            deliveryDetail.huno2 = "KG"
            deliveryDetail.huwt2 = bagItems[0].palletAverage
            deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
            deliveryDetail.nohu2 = bagItems[0].noOfPallet
            deliveryDetail.bagList = bagItems
        } /*else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }*/
        list.add(deliveryDetail)
    }
    return list
}
