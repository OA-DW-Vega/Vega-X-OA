package com.olam.warehouse.vegax.mtntnicaragua.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaMtntDao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.mtntnicaragua.data.api.VegaNicaraguaMtntApi
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryPost
import com.olam.warehouse.vegax.mtntnicaragua.utils.WEIGHSCALE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 12/7/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaNicaraguaMtntPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaMtntApi by inject()
        val dao: VegaNicaraguaMtntDao by inject()
        val wbid = inputData.getString(UIUtils.MTNT_DATA) ?: ""
        val mtntData = dao.getMtntWithLotsSync(wbid)
        val vegaNicMtntDeliveryPost = VegaNicMtntDeliveryPost(
            getCurrentKey(),
            getPlantDetails(),
            "",
            prepareDeliveryList(mtntData),
            WEIGHSCALE,
            mtntData.mtnt.contactNumber.toString(),
            mtntData.mtnt.driverName.toString(),
            mtntData.mtnt.vehicleNumber.toString(),
            mtntData.mtnt.remarks.toString(),
            mtntData.mtnt.vehicleType.toString(),
            mtntData.mtnt.mergedNetWeight.toString(),
            mtntData.mtnt.mtntDocSequence.toString()
        )
        try {
            val response = api.syncMtntData(vegaNicMtntDeliveryPost).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    if (resp?.success == true) {
                        mtntData.mtnt.isOfflineData = false
                        mtntData.mtnt.isErrorStatus = true
                        mtntData.mtnt.isSyncStatus = true
                    } else {
                        mtntData.mtnt.isOfflineData = true
                        mtntData.mtnt.isErrorStatus = false
                        mtntData.mtnt.isSyncStatus = false
                    }
                    mtntData.mtnt.message = resp?.message
                    for (item in respData) {
                        mtntData.lineItems.forEach { /*single { it.lots.batchNumber == item.batchNumber }.apply {*/
                            it.lots.weighScaleWbId = item.weighBridgeId
                            it.lots.pickingFlag = item.pickingFlag
                            it.lots.deliveryFlag = item.deliveryFlag
                            it.lots.deliveryItem = item.deliveryItem
                            it.lots.documentNum = item.documentNum
                            it.lots.delivery = item.delivery
                            it.lots.pgiFlag = item.pgiFlag
                            mtntData.mtnt.delivery = item.delivery
                            mtntData.mtnt.batchNumber = item.batchNumber
                            it.lots.storageLossFlag = item.storageLossFlag
                        }
                    }
                    dao.insertTruckInfo(mtntData.mtnt)
                    dao.saveLotList(mtntData.lineItems.map { it.lots })
                    Result.success(workDataOf(UIUtils.MTNT_OUTPUT_DATA to "resp?.data?.wbId"))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    mtntData.mtnt.isOfflineData = true
                    mtntData.mtnt.isErrorStatus = false
                    mtntData.mtnt.isSyncStatus = false
                    mtntData.mtnt.message = msg
                    dao.insertTruckInfo(mtntData.mtnt)
                    Result.failure(workDataOf(UIUtils.MTNT_OUTPUT_DATA to msg))
                }
            } else {

                Result.failure(workDataOf(UIUtils.MTNT_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.MTNT_OUTPUT_DATA to error.message))
        }
//        }
    }
}

private fun prepareDeliveryList(mtntData: VegaMtntWithLotsWithBags): List<VegaNicMtntDeliveryDetail> {
    val list = ArrayList<VegaNicMtntDeliveryDetail>()
    val year: Int = Calendar.getInstance().get(Calendar.YEAR)
    val mtnt = mtntData.mtnt
    val isEndLot = mtntData.lineItems.any { it.lots.isMergedLot==true }
    mtntData.lineItems.filter { it.lots.isMergedLot==false }.forEach { item ->
        val deliveryDetail = VegaNicMtntDeliveryDetail()
        var bagItems =
            item.bagItems.filter { it.batchNumber == item.lots.batchNumber /*&& it.baseMaterial == item.materialCode */ }
        deliveryDetail.batchNumber = item.lots.batchNumber
        deliveryDetail.materialCode = item.lots.materialCode
        deliveryDetail.plantId = item.lots.plantId
        deliveryDetail.recPlantId = mtnt.recPlantId
        deliveryDetail.netWeight = item.lots.weight?.trim()
        if(isEndLot)
            deliveryDetail.endLotFlag = isEndLot
        else
            deliveryDetail.endLotFlag = item.lots.isEndLot ?: false

        deliveryDetail.createdDate = mtnt.startTime
        deliveryDetail.purchaseDocNum = mtnt.purchaseDocNum
        deliveryDetail.purchaseDocDesc = mtnt.purchaseDocDesc
        deliveryDetail.recStorageLocationCode = item.lots.storageLocationCode
        deliveryDetail.storageLocationCode = item.lots.storageLocationCode
        deliveryDetail.unitsOfMeasure = item.lots.unitOfMeasure
        deliveryDetail.startTime = mtnt.startTime
        deliveryDetail.endTime = mtnt.endTime
        deliveryDetail.turnAroundTime = mtnt.turnAroundTime ?: "0"
        deliveryDetail.weighBridgeId = mtnt.weighBridgeId
        deliveryDetail.deliveryFlag = mtnt.deliveryFlag ?: false
        deliveryDetail.pickingFlag = mtnt.pickingFlag ?: false
//            deliveryDetail.isPgiFlag = mtnt.isPgiFlag
        deliveryDetail.storageLossFlag =item.lots.storageLossFlag ?: false
        deliveryDetail.delivery = mtnt.delivery
        deliveryDetail.deliveryItem = mtnt.deliveryItem
        deliveryDetail.year = year.toString()
        //Bag and pallet details
        var grossWeight = 0.0
        var tareWeight = 0.0
        var bagTareWeight = 0.0
        /*var startTime = ""
        var endTime = ""*/
        bagItems.forEach { item1 ->
            grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            /*if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
            if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()*/
//            item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
        }
        if (bagItems.isNotEmpty()) {
            val bagSort = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
            val dat = bagItems.sortedByDescending { it.status }
            bagSort.addAll(dat)
            bagItems = bagSort.asReversed()
            deliveryDetail.huno = bagItems[0].unitsOfMeasure
            deliveryDetail.huwt = bagTareWeight.toString().trim()
            deliveryDetail.huno2 = "KG"
            deliveryDetail.huwt2 = bagItems[0].palletAverage
            deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
            deliveryDetail.nohu2 = bagItems[0].noOfPallet
            deliveryDetail.grossWeight = grossWeight.toString().trim()
            deliveryDetail.bagList = bagItems
        } else {
            deliveryDetail.huwt = "0.0"
            deliveryDetail.huwt2 = "0.0"
            deliveryDetail.huno2 = mtnt.unitsOfMeasure
            deliveryDetail.grossWeight = grossWeight.toString().trim()
        }
        list.add(deliveryDetail)
    }
    return list
}
