package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegaghana.model.TextNavListValues
import com.olam.warehouse.master.vegaghana.model.VegaGhanaMtntDeliveryDetail
import com.olam.warehouse.master.vegaghana.model.VegaGhanaMtntDeliveryPost
import com.olam.warehouse.presentation.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.collections.ArrayList


@Suppress("UNCHECKED_CAST")
class VegaGhanaDispatchPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val dao: VegaCoffeeDispatchDao by inject()
        val api: MasterApi by inject()
        val deliveryId = inputData.getString(UIUtils.DISPATCH_DATA) ?: ""
//        //Passing deliveryDetails[] from previously failed response for resync case
//        val inputResponseObj =
//            Gson().fromJson<VegaEcuadorDeliveryPostResponse>(inputData.getString(UIUtils.DISPATCH_POST_DATA).toString())
//        val inputDeliveryDetails = inputResponseObj?.deliveryDetails ?: emptyList()
        var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
        var jsonData = mutableListOf<String>()
        var textUpdate = ArrayList<String>()
        val JSON_TEXT_UPDATE = "TEXT_UPDATE"

        var materialList = dao.getGhanaMaterialData(deliveryId)
        val dispatchData = dao.getMtntWithLotSingle(deliveryId)
        val deliveryDetails = ArrayList<VegaGhanaMtntDeliveryDetail>()
        val dispatchLotsList = dao.getGhanaDispatchLotsData(deliveryId)
        var textUpdateList = dao.getProcessList(getCurrentKey())
        var bagItems = dao.getGhanaOfflineBagItems(deliveryId)

        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }

        textUpdateList.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_TEXT_UPDATE)) {
                var receiveLocation = (JSONObject(it).getJSONArray(JSON_TEXT_UPDATE).get(0)).toString().split(",")
                receiveLocation.forEach {  it1 ->
                    textUpdate.add(((it1.split(":")[0]).replace("{","").replace("\"", "")).plus(" - ").plus((it1.split(":")[1]).replace("}","").replace("\"", "")))
                }
            }

        }

        //====START ==
//        val postData = preparePostMtntData(dispatchLotsList,bagList,dispatchData,materialList)
        val postData = ArrayList<VegaGhanaMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)

        for (item in dispatchLotsList) {
            val deliveryDetail = VegaGhanaMtntDeliveryDetail()
            var bagItems =
                bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode && it.weighBridgeId == deliveryId }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.recPlantId = dispatchData.dispatch.recPlantId
            deliveryDetail.fromVendorCode = dispatchData.dispatch.fromVendorCode
            deliveryDetail.netWeight = item.editedWeight
            deliveryDetail.frbnr1 = dispatchData.dispatch.frbnr1
            deliveryDetail.vehicleNumber = dispatchData.dispatch.vehicleNumber
            deliveryDetail.endLotFlag = item.isEndLot ?: false
            deliveryDetail.purchaseDocDesc = dispatchData.dispatch.purchaseDocDesc
            deliveryDetail.purchaseDocNum = dispatchData.dispatch.purchaseDocNum
            deliveryDetail.createdDate = dispatchData.dispatch.startTime
            materialList.forEach {
                if (item.materialCode == it.materialCode) {
                    deliveryDetail.purchaseDocNum = it.purchaseOrderNum
                    deliveryDetail.purchaseDocDesc = it.purchaseOrderDesc
                    deliveryDetail.soWeight = it.soWeight
                }
            }
            /* vm.materialModelList.forEach {
                 if (item.materialCode == it.materialCode) {
                     deliveryDetail.purchaseDocNum = it.purchaseOrderNum
                     deliveryDetail.purchaseDocDesc = it.purchaseOrderDesc
                 }
             }*/
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.startTime = "0"
            deliveryDetail.endTime = "0"
            deliveryDetail.turnAroundTime = "0"
//            deliveryDetail.startTime = summaryObj?.startTime
//            deliveryDetail.endTime = summaryObj?.endTime
//            deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
            deliveryDetail.weighBridgeId = item.weighScaleWbId ?: ""
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.isPgiFlag = item.isPgiFlag
            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.frbnr1 = dispatchData.dispatch.frbnr1
            deliveryDetail.year = year.toString()
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
                if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
                if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "MT"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems
            } else {
//                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
            postData.add(deliveryDetail)
        }

        val list = ArrayList<TextNavListValues>()
        textUpdate.forEach {
            if(it.toString().isNotEmpty()) {
                val textNavListValues = TextNavListValues()
                var textValue = (it.toString()).split("-")
                if(textValue[1].trim().equals("Z003")){
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = dispatchData.dispatch.driverName.toString()
                }else if(textValue[1].trim().equals("Z006")){
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = dispatchData.dispatch.vehicleNumber.toString()
                }else {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = textValue[0].trim()
                }
                list.add(textNavListValues)
            }
        }

//        if (inputDeliveryDetails.isNullOrEmpty()) {
//            deliveryDetails.clear()
//            dispatchData.lineItems.forEach label@{
//                if (it.pairId!! <= 0)
//                    return@label
//                when (val mergedDispatchLotList = mergedLotsMap[it.pairId!!]) {
//                    null -> {
//                        val list = ArrayList<VegaEcuadorDispatchLots>()
//                        list.add(it)
//                        mergedLotsMap[it.pairId!!] = list
//                    }
//                    else -> {
//                        mergedDispatchLotList.add(it)
//                        mergedLotsMap[it.pairId!!] = mergedDispatchLotList
//                    }
//                }
//            }
//            for ((key, dispatchLotList) in mergedLotsMap) {
//                val item = VegaEcuadorDispatchLotsMerge()
//
//                item.lots = ArrayList<VegaEcuadorDispatchLots>()
//                item.lots.addAll(dispatchLotList)
//                deliveryDetails.add(item)
//            }
//
//            val unpaired = dispatchData.lineItems.filter { it.pairId == 0 }
//            unpaired.forEach {
//                val item = VegaEcuadorDispatchLotsMerge()
//                item.batchNumber = it.batchNumber
//                val list = ArrayList<VegaEcuadorDispatchLots>(1)
//                list.add(it)
//                item.lots.addAll(list)
//                deliveryDetails.add(item)
//            }
//        } else {
//            deliveryDetails.addAll(inputDeliveryDetails)
//        }
        try {
            val response =
                api.postWeighScaleDeliveryDetails(
                    VegaGhanaMtntDeliveryPost(
                        getCurrentKey(),
                        getPlantDetails(),
                        "",
                        postData, list,"WEIGHSCALE",dispatchData.dispatch.vehicleNumber,dispatchData.dispatch.driverLicenseNumber,
                            "",dispatchData.dispatch.driverPhoneNumber.toString(),dispatchData.dispatch.driverName.toString()
                    )
                )
                    .execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    if(respData.size > 1){
                        var delivery = ""
                        respData.forEach {item1 ->
                            delivery = delivery.plus(item1.delivery).plus(" ")
                        }
                        dispatchData.dispatch.delivery = delivery
                    }
                    else
                        dispatchData.dispatch.delivery = respData[0].delivery

//                    dispatchData.dispatch.delivery = respData[0].delivery
                    dispatchData.dispatch.isSyncStatus = true
                    dispatchData.dispatch.isProgress = false
//                    dispatchData.dispatch.syncStatusMsg = respData.message
//                    dispatchData.dispatch.syncStatus = Status.MTNT_COMPLETED
                    useCase.updateGhanaSuccessData(
                        dispatchData.dispatch.delivery.toString(),
                        dispatchData.dispatch.weighBridgeId,
                        dispatchData.dispatch.isSyncStatus,
                        4,
                        "msg",
                        dispatchLotsList
                    )
//                    useCase.updateMtntDispatchStatus(dispatchData.dispatch)
                    Result.success(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to respData[0].delivery))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    dispatchData.dispatch.isSyncStatus = false
                    dispatchData.dispatch.isProgress = false
//                    dispatchData.dispatch.syncStatus = Status.SYNC_ERROR
                    if (respData != null && isSuccess) {
//                        dispatchData.dispatch.syncStatusMsg = respData.message
                        dispatchData.dispatch.delivery = respData[0].delivery
//                        useCase.updateDispatchStatus(dispatchData.dispatch)
//                        useCase.updateMtntDispatchStatus(dispatchData.dispatch)
                        useCase.updateGhanaSuccessData(
                            dispatchData.dispatch.delivery.toString(),
                            dispatchData.dispatch.weighBridgeId,
                            dispatchData.dispatch.isSyncStatus,
                            3,
                            msg.toString(),
                            dispatchLotsList
                        )
//                        Result.failure(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to Gson().toJson(respData)))
                        Result.failure(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to dispatchData.dispatch.message))

                    } else {
//                        dispatchData.dispatch.syncStatusMsg = msg.toString()
//                        useCase.updateDispatchStatus(dispatchData.dispatch)
//                        useCase.updateMtntDispatchStatus(dispatchData.dispatch)
                        dispatchData.dispatch.isSyncStatus = false
                        dispatchData.dispatch.isProgress = false
//                        dispatchData.dispatch.syncStatus = Status.SYNC_ERROR
                        useCase.updateGhanaSuccessData(
                            dispatchData.dispatch.delivery.toString(),
                            dispatchData.dispatch.weighBridgeId,
                            dispatchData.dispatch.isSyncStatus,
                            3,
                            msg.toString(),
                            dispatchLotsList
                        )
                        Result.failure(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to dispatchData.dispatch.message))
                    }
                }

            } else {
                dispatchData.dispatch.isSyncStatus = false
                dispatchData.dispatch.isProgress = false
//                dispatchData.dispatch.syncStatusMsg = response.message()
//                dispatchData.dispatch.syncStatus = Status.SYNC_ERROR
//                useCase.updateDispatchStatus(dispatchData.dispatch)
//                useCase.updateMtntDispatchStatus(dispatchData.dispatch)

                Result.failure(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to error.message))
        }
    }

/*    private fun preparePostMtntData(
        dispatchLotsList: List<VegaCocoaDispatchLots>,
        bagList: ArrayList<VegaCocoaSweepingBagMaterial>,
        dispatchData: VegaCocoaMtntWithLots,
        materialList: List<VegaCoffeePurchaseOrderMaterialModel>
    ): ArrayList {
        val postData = ArrayList<VegaGhanaMtntDeliveryDetail>()

        val year: Int = Calendar.getInstance().get(Calendar.YEAR)

        for (item in dispatchLotsList) {
            val deliveryDetail = VegaGhanaMtntDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.netWeight = item.editedWeight
//            deliveryDetail.vehicleNumber = summaryObj?.vehicleNumber
            deliveryDetail.endLotFlag = item.isEndLot ?: false
            deliveryDetail.purchaseDocDesc = dispatchData.dispatch.purchaseDocDesc
            deliveryDetail.purchaseDocNum = dispatchData.dispatch.purchaseDocNum
//            deliveryDetail.createdDate = summaryObj?.startTime
            materialList.forEach {
                if (item.materialCode == it.materialCode) {
                    deliveryDetail.purchaseDocNum = it.purchaseOrderNum
                    deliveryDetail.purchaseDocDesc = it.purchaseOrderDesc
                }
            }
            *//* vm.materialModelList.forEach {
                 if (item.materialCode == it.materialCode) {
                     deliveryDetail.purchaseDocNum = it.purchaseOrderNum
                     deliveryDetail.purchaseDocDesc = it.purchaseOrderDesc
                 }
             }*//*
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.startTime = "0"
            deliveryDetail.endTime = "0"
            deliveryDetail.turnAroundTime = "0"
//            deliveryDetail.startTime = summaryObj?.startTime
//            deliveryDetail.endTime = summaryObj?.endTime
//            deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
            deliveryDetail.weighBridgeId = item.weighScaleWbId ?: ""
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.isPgiFlag = item.isPgiFlag
            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.year = year.toString()
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            *//*var startTime = ""
            var endTime = ""*//*
            bagItems.forEach { item1 ->
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                *//*if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
                if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()*//*
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "MT"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems
            } else {
//                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
            postData.add(deliveryDetail)
        }
        return postData
    }*/
}

