package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegaghana.dao.VegaGhanaProcessingDao
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.master.vegaghana.model.GhanaOfflineProcessingFgrnLotDetails
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineProcessingFgrnPost
import com.olam.warehouse.master.vegaghana.model.VegaGhanaProcessingCreatePoReq
import com.olam.warehouse.master.vegaghana.model.VegaGhanaProcessingRMINLotDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.FGRN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.FGRN_STAGE
import com.olam.warehouse.presentation.utils.UIUtils.GRN_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_OUTPUTMATERIALCODE
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

@Suppress("UNCHECKED_CAST")
class VegaFgrnPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: MasterUseCase by inject()
        val api: MasterApi by inject()
        val dao: VegaGhanaProcessingDao by inject()
        val fgrnId = inputData.getString(FGRN_DATA) ?: ""
        val versionID = inputData.getString(RMIN_VERSION) ?: ""
        val outputmaterialcode = inputData.getString(RMIN_OUTPUTMATERIALCODE) ?: ""
        val cfgNo = inputData.getString(FGRN_STAGE) ?: ""

        val offlineItem = dao.getofflineFgrnPostItem(fgrnId)
        val rminId = offlineItem.rminTempId
        val offlineDetails = dao.getofflineFgrnPostLotDetails(fgrnId, rminId!!)
        val offlineRminDetails = dao.getofflineRminPostItem(rminId)
        val lotDetails = prepareLotDetails(offlineDetails, offlineRminDetails)
        val postData = preparePostFgrnData(offlineItem, lotDetails)
        var createporeq = VegaGhanaProcessingCreatePoReq()
        if (offlineDetails.processOrderNum!!.contains("TMP_RMIN_")) {
            createporeq =
                prepareRminofflineCreatePoRequest(cfgNo, outputmaterialcode, postData, versionID)
        }
        try {
            var response = /*if(offlineDetails.processOrderNum!!.contains("TMP_RMIN_"))*/
                api.postCreatePo(createporeq)
                    .execute()/*  else api.postOfflineProcessing(postData).execute()*/
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null && resp?.success == true) {
                    useCase.updateGhanaProcessing(
                        fgrnId,
                        rminId,
                        "FGRN Created Successfully",
                        4
                    )
                    dao.updateStatusResponse(
                        fgrnId,
                        rminId,
                        4
                    )
                    dao.deleteOfflineRminLotDetails(rminId)
                    dao.deleteOfflineRminData(rminId)
                    var msg = ""
                    if (offlineDetails.processOrderNum!!.contains("TMP_RMIN_")) {
                        val successMsgList = resp.data
                        msg = successMsgList.autoPoResponse?.get(0)?.messages?.get(0)?.message ?: ""
                    } else {
                        msg = response.message()
                    }
                    Result.success(workDataOf(GRN_OUTPUT_DATA to msg))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    useCase.updateGhanaProcessing(fgrnId, rminId, msg.toString(), 3)
                    Result.failure(workDataOf(GRN_OUTPUT_DATA to msg))
                }
            } else {
                useCase.updateGhanaProcessing(fgrnId, rminId, response.message(), 3)
                Result.failure(workDataOf(GRN_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            error.printStackTrace()
            Result.failure(workDataOf(GRN_OUTPUT_DATA to error.message))
        }
    }
}

fun preparePostFgrnData(
    wbDetails: VegaGhanaOfflineFgrnData,
    lotDetails: List<GhanaOfflineProcessingFgrnLotDetails>
): VegaGhanaOfflineProcessingFgrnPost {
    val grnPost = VegaGhanaOfflineProcessingFgrnPost()
    grnPost.key = getCurrentKey()
    grnPost.plant = getPlantDetails()
    grnPost.processingStage = wbDetails.processingStage
    grnPost.outputMaterialCode = wbDetails.outputMaterialCode
    grnPost.rmin = false
    grnPost.processingLotDtls = lotDetails
    grnPost.shiftType = wbDetails.shiftType
    grnPost.operatorName = wbDetails.operatorName
    grnPost.versionId = wbDetails.versionId
    return grnPost
}

fun prepareLotDetails(
    offlineDetails: VegaGhanaOfflineFgrnProcessLotDetails,
    offlineRminDetails: List<VegaGhanaOfflineRminProcessLotDetails>
): List<GhanaOfflineProcessingFgrnLotDetails> {
    var list = mutableListOf<GhanaOfflineProcessingFgrnLotDetails>()
    offlineRminDetails.forEach {
        var rminList = GhanaOfflineProcessingFgrnLotDetails()
        rminList.bagCount = it.bagCount
        rminList.batchNumber = it.batchNumber
        rminList.confText = it.confText
        rminList.deliveryItem = "0000"
        rminList.materialCode = it.materialCode
        rminList.menge = it.netWeight
        rminList.movementType = it.movementType
        rminList.netWeight = it.netWeight
        rminList.grossWeight = it.grossWeight
        rminList.startTime = it.startTime
        rminList.endTime = it.endTime
        rminList.phase = it.phase
        rminList.plant = it.plant
        rminList.year = it.year
        rminList.processOrderNum = it.processOrderNum
        rminList.rsnum = it.rsnum
        rminList.rspos = it.rspos
        rminList.storageLocationCode = it.storageLocationCode
        rminList.unitsOfMeasure = it.unitsOfMeasure
        rminList.xchpf = it.xchpf
        rminList.huno = it.huno
        rminList.huwt = it.huwt
        rminList.huno2 = it.huno2
        rminList.huwt2 = it.huwt2
        rminList.nohu1 = it.nohu1
        rminList.nohu2 = it.nohu2
        rminList.endLotFlag = it.endLotFlag!!
        rminList.vendorCode = it.vendorCode
        rminList.storageLossFlag = it.storageLossFlag!!
        rminList.bagCount = it.bagCount
        rminList.batchNumber = it.batchNumber
        rminList.confText = it.confText
        rminList.deliveryItem = it.deliveryItem
        rminList.materialCode = it.materialCode
        rminList.menge = it.menge
        rminList.movementType = it.movementType
        rminList.netWeight = it.netWeight
        rminList.grossWeight = it.grossWeight
        rminList.startTime = it.startTime
        rminList.endTime = it.endTime
        rminList.phase = it.phase
        rminList.plant = it.plant
        rminList.year = it.year
        rminList.processOrderNum = it.processOrderNum
        rminList.rsnum = it.rsnum
        rminList.rspos = it.rspos
        rminList.storageLocationCode = it.storageLocationCode
        rminList.unitsOfMeasure = it.unitsOfMeasure
        rminList.xchpf = it.xchpf
        rminList.huno = it.huno
        rminList.huwt = it.huwt
        rminList.huno2 = it.huno2
        rminList.huwt2 = it.huwt2
        rminList.nohu1 = it.nohu1
        rminList.nohu2 = it.nohu2
        rminList.endLotFlag = it.endLotFlag!!
        rminList.vendorCode = it.vendorCode
        rminList.storageLossFlag = it.storageLossFlag!!
        rminList.bagMaterialCode = it.bagMaterialCode
        rminList.bagType = it.bagType
        list.add(rminList)
    }
    var fgrnList = GhanaOfflineProcessingFgrnLotDetails()
    fgrnList.bagCount = offlineDetails.bagCount
    fgrnList.batchNumber = offlineDetails.batchNumber
    fgrnList.confText = offlineDetails.confText
    fgrnList.deliveryItem = offlineDetails.deliveryItem
    fgrnList.materialCode = offlineDetails.materialCode
    fgrnList.menge = offlineDetails.menge
    fgrnList.movementType = offlineDetails.movementType
    fgrnList.netWeight = offlineDetails.netWeight
    fgrnList.grossWeight = offlineDetails.grossWeight
    fgrnList.startTime = offlineDetails.startTime
    fgrnList.endTime = offlineDetails.endTime
    fgrnList.phase = offlineDetails.phase
    fgrnList.plant = offlineDetails.plant
    fgrnList.year = offlineDetails.year
    fgrnList.processOrderNum = offlineDetails.processOrderNum
    fgrnList.rsnum = offlineDetails.rsnum
    fgrnList.rspos = offlineDetails.rspos
    fgrnList.storageLocationCode = offlineDetails.storageLocationCode
    fgrnList.unitsOfMeasure = offlineDetails.unitsOfMeasure
    fgrnList.xchpf = offlineDetails.xchpf
    fgrnList.huno = offlineDetails.huno
    fgrnList.huwt = offlineDetails.huwt
    fgrnList.huno2 = offlineDetails.huno2
    fgrnList.huwt2 = offlineDetails.huwt2
    fgrnList.nohu1 = offlineDetails.nohu1
    fgrnList.nohu2 = offlineDetails.nohu2
    fgrnList.endLotFlag = offlineDetails.endLotFlag!!
    fgrnList.vendorCode = offlineDetails.vendorCode
    fgrnList.storageLossFlag = offlineDetails.storageLossFlag!!
    if (offlineDetails.bagCount == "0") {
        fgrnList.bagMaterialCode = ""
        fgrnList.bagType = ""
    } else {
        fgrnList.bagMaterialCode = offlineDetails.bagMaterialCode
        fgrnList.bagType = offlineDetails.bagType
    }
    list.add(fgrnList)

    return list
}

fun prepareRminofflineCreatePoRequest(
    cfgNumber: String,
    materialNo: String,
    model: VegaGhanaOfflineProcessingFgrnPost, versionID: String
): VegaGhanaProcessingCreatePoReq {
    val poReq = VegaGhanaProcessingCreatePoReq()
    poReq.fgrn = true
    poReq.cfgNo = cfgNumber
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = ""
    poReq.outputMaterialCode = materialNo
    poReq.materialCode = materialNo
    poReq.plant = getPlantDetails()
    poReq.rmin = true
    poReq.versionId = versionID
    val processList = mutableListOf<VegaGhanaProcessingRMINLotDetails>()
    model.processingLotDtls.forEach {
        val processLot = VegaGhanaProcessingRMINLotDetails()
        processLot.batchNumber = it.batchNumber
        processLot.materialCode = it.materialCode
        processLot.plant = getPlantDetails().plantId
        processLot.netWeight = it.netWeight
        val storeCode = it.storageLocationCode?.split("-")
        processLot.storageLocationCode = storeCode?.get(0)
        processLot.unitsOfMeasure = it.unitsOfMeasure
        processLot.bagCount = it.bagCount
        processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
        processLot.deliveryItem = it.deliveryItem
        processLot.menge = it.netWeight
        processLot.movementType = it.movementType
        processLot.phase = ""
        processLot.processOrderNum = it.processOrderNum
        processLot.rsnum = it.rsnum
        processLot.rspos = it.rspos
        processLot.xchpf = it.xchpf
        processLot.resource = ""
        processLot.shiftType = ""
        processLot.remarks = ""
        processList.add(processLot)
    }
    poReq.processingLotDtls = processList
    return poReq
}
