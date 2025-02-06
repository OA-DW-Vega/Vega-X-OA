package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.api.VegaGhanaCocoaOffloadingApi
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingPostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

@Suppress("UNCHECKED_CAST")
class VegaGhanaCocoaMtnrPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    var mtnNo = ""
    var id = "SYNC_".plus(Random.nextLong().toString())
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao: VegaEcuadorOffloadingDao by inject()
        val apiCocoa: VegaGhanaCocoaOffloadingApi by inject()
        val wbid = inputData.getString(UIUtils.RECEIVING_DATA) ?: ""
        val receivingData = dao.getMtnrReceivingWithLineItemWbid(wbid)
        val supplierList = dao.getSuppliersOffline()
        try {
            val response =
                apiCocoa.postOffloadingDetailSync(
                    VegaGhanaOffloadingPostRequest(
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        operatorName = "",
                        batchNumber = "",
                        delFlag = "",
                        deliveryDetails = prepareDeliveryList(receivingData),
                        weighmentType = "WS",
                        vehicleNumber = receivingData.receiving.vehicleNumber,
                        vehicleType = receivingData.receiving.vehicleType,
                        contactNumber = receivingData.receiving.contactNumber,
                        driverName = receivingData.receiving.driverName,
                        purchaseOrg = if(supplierList.size>0) supplierList.get(0).purchaseOrgType else " "
                    )
                ).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    dao.updateMtnrOfflineResponse(true, resp.message, Status.RECEVING_COMPLETED, wbid, false, id)
                    dao.deleteMtnrReceivingLotItem(mtnNo)
                    dao.deleteMtnrReceivingLotItemWithBagItems(mtnNo)
                    dao.updateWBId(respData.wbId.toString(), id)
                    dao.updateMtnrQuality(respData.wbId.toString(), wbid)
                    Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to respData.wbId.toString()))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    dao.updateMtnrOfflineResponse(false, msg!!, Status.SYNC_ERROR, wbid, false, id)
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                }

            } else {
                dao.updateMtnrOfflineResponse(false, response.message(), Status.SYNC_ERROR, wbid, false, id)
                Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to error.message))
        }
    }

    private fun prepareDeliveryList(receiving: VegaCoffeeReceivingMtnrWithLots): List<VegaGhanaOffloadingDeliveryDetail> {
        var batchList = mutableListOf<VegaCoffeeReceiveLots>()
        var bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()

        if (receiving.lineItems.size > 0) {
            receiving.lineItems.forEach {
                batchList.add(it.lots)
                bagList.addAll(
                    it.bagItem.filter { it2 -> it2.mtnNumber.equals(it.lots.mtnNumber) }.filter { it1 ->
                        it1.batchNumber.equals(
                            it.lots.batch
                        )
                    }
                )
            }
        }

        val list = ArrayList<VegaGhanaOffloadingDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in batchList) {
            val deliveryDetail = VegaGhanaOffloadingDeliveryDetail()
            var bagItems = bagList.toMutableList()/*.filter { it.batchNumber == item.batch }*/
            mtnNo = item.mtnNumber
            deliveryDetail.batchNumber = item.batch
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.remarks = receiving.receiving.remarks
            deliveryDetail.plantId = receiving.receiving.plantId
            deliveryDetail.recPlantId = receiving.receiving.materialCode
            deliveryDetail.startTime = receiving.receiving.startTime
            deliveryDetail.endTime = receiving.receiving.endTime
            deliveryDetail.year = year.toString()
            deliveryDetail.huno = "MT"
            deliveryDetail.huno2 = "MT"
            deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
            deliveryDetail.netWeight = item.editedWeight?.trim() ?: "0.0"
            deliveryDetail.recStorageLocationCode = receiving.receiving.storageLocationCode
            deliveryDetail.storageLocationCode = receiving.receiving.supplierCode
//            deliveryDetail.storageLocation = receiving.receiving.supplierCode
            deliveryDetail.purchaseDocNum = receiving.receiving.purchaseDocNum
            deliveryDetail.purchaseDocDesc = receiving.receiving.purchaseDocDesc
            deliveryDetail.endLotFlag = false
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.posnr
            deliveryDetail.unitsOfMeasure = item.uom
//            deliveryDetail.weighBridgeId = receiving.receiving.weighBridgeId
//            deliveryDetail.weighBridgeType = receiving.receiving.weighBridgeType
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            bagItems.forEach { item1 ->
                grossWeight = item1.grossWeight.toDouble()/*grossWeight.plus(item1.grossWeight.toDouble())*/
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            }
            deliveryDetail.grossWeight = grossWeight.toString().trim()
            deliveryDetail.huwt =tareWeight.toString().trim()
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeOffloadingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
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

}




