package com.olam.warehouse.vegax.qualityindo.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.model.VegaIndoWeighBridgeWithQualityParams
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeQualityDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.qualityindo.data.api.VegaIndoCoffeeQualityApi
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualityindo.utils.PROCURE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 4/23/2021.
 */

@Suppress("UNCHECKED_CAST")
class VegaIndoCoffeeQualityPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaIndoCoffeeQualityApi by inject()
        val dao: VegaCoffeeQualityDao by inject()
        val tmpWbId = inputData.getString(UIUtils.TEMP_ID) ?: ""
        val wbType = inputData.getString(UIUtils.WB_ID) ?: ""
        val mtnrData = dao.getWBWithLotWithQualitySingleSync(tmpWbId)
        val supplierData = dao.getWBWithQualitySingleSync(tmpWbId)

        try {
            if (wbType.equals(PROCURE)) {
                val dataSupPost = getSupplierPost(supplierData)
                val response = api.postQualitySupplierOffline(dataSupPost).execute()
                if (response.isSuccessful) {
                    val resp = response.body()
                    val respData = response.body()?.data
                    if (respData != null) {
                        dao.updateBatchToGrn(tmpWbId, resp?.data?.charg.toString())
                        dao.updateWBListSuccessStatusWorks(
                            tmpWbId,
                            resp?.data?.charg.toString(),
                            resp?.message.toString()
                        )
                        Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to resp?.data?.charg))
                    } else {
                        val msg = resp?.message ?: resp?.errors
                        dao.updateWBListErrorStatusWorks(tmpWbId, msg.toString())
                        Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                    }
                } else {
                    val msg = response.message()
                    dao.updateWBListErrorStatusWorks(tmpWbId, msg.toString())
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))
                }
            } else {
                val dataPost = getMtnrPost(mtnrData)
                val response = api.postQualityOffline(dataPost).execute()
                if (response.isSuccessful) {
                    val resp = response.body()
                    val respData = response.body()?.data
                    if (respData != null) {
                        dao.updateWBListSuccessStatusWorks(
                            tmpWbId,
                            resp?.data?.charg.toString(),
                            resp?.message.toString()
                        )
                        Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to resp?.data?.charg))
                    } else {
                        val msg = resp?.message ?: resp?.errors
                        dao.updateWBListErrorStatusWorks(tmpWbId, msg.toString())
                        Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                    }
                } else {
                    val msg = response.message()
                    dao.updateWBListErrorStatusWorks(tmpWbId, msg.toString())
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))
                }
            }


        } catch (error: Throwable) {
            dao.updateWBListErrorStatusWorks(tmpWbId, error.message.toString())
            Result.failure(workDataOf(UIUtils.GRN_OUTPUT_DATA to error.message))
        }
//        }
    }
}

private fun getMtnrPost(mtnrData: VegaIndoWeighBridgeWithQualityParams): VegaIndoCoffeeQualityParamPost {
    val isApplicableGrn = mtnrData.lotList.size - 1 == getMtnrLotsData(mtnrData).size
    return VegaIndoCoffeeQualityParamPost(
        grnApplicable = isApplicableGrn,
        grnFlag = false,
        key = getCurrentKey(),
        plant = getPlantDetails(),
        lotDetails = getMtnrLotsData(mtnrData),
        bcMessage = "",
        charg = "",
        currentWbid = "",
        errorMessage = "",
        grnNumber = ""
    )
}

private fun getMtnrLotsData(mtnrData: VegaIndoWeighBridgeWithQualityParams): ArrayList<VegaCoffeeLot> {
    val qualityPostList = arrayListOf<VegaCoffeeLot>()
    var lotDetails = VegaCoffeeLot()
    mtnrData.lotList.forEach {
        var vagaLot = VegaCoffeeLot()
        if (it.lot.isOffline == true) {
            vagaLot = it.lot
            vagaLot.qualityDetails = it.quality
            qualityPostList.add(vagaLot)
        }
    }
    return qualityPostList
}

private fun getSupplierPost(supplierData: VegaWeighBridgeWithQualityParams): VegaIndoCoffeeQualitySupplierParamPost {
    val weighBridgeDetails = supplierData.qualityWBDetails
    val editPlantDetails = getPlantDetails()
    if (weighBridgeDetails.challan?.isNotEmpty() == true) editPlantDetails.plantCode =
        weighBridgeDetails.challan.toString()
    return VegaIndoCoffeeQualitySupplierParamPost(
        grnApplicable = true,
        grnFlag = false,
        key = getCurrentKey(),
        plant = editPlantDetails,
        lotDetails = getSupplierData(supplierData),
        bcMessage = "",
        charg = "",
        currentWbid = weighBridgeDetails.weighBridgeId.toString(),
        errorMessage = "",
        grnNumber = "",
        driverName = weighBridgeDetails.driverName.toString(),
        imageString = "",
        imageUploadMsg = "",
        contactNumber = weighBridgeDetails.contactNumber.toString(),
        transportVendorCode = weighBridgeDetails.transportVendorCode.toString(),
        vehicleNumber = weighBridgeDetails.vehicleNumber.toString()
    )
}

private fun getSupplierData(supplierData: VegaWeighBridgeWithQualityParams): ArrayList<VegaCoffeeLot> {
    val qualityPostList = arrayListOf<VegaCoffeeLot>()
    val weighBridgeDetails = supplierData.qualityWBDetails
    val qualityParameter = supplierData.quality
    val qtyParams = qualityParameter.filter { it.qualityParameterValue?.isNotEmpty() == true }
    weighBridgeDetails.qualityDetails = qtyParams
    weighBridgeDetails.batchNumber = ""
    weighBridgeDetails.finalApproval = weighBridgeDetails.finalApproval
    qualityPostList.clear()
    weighBridgeDetails.let {
        qualityPostList.add(
            VegaCoffeeLot(
                item = weighBridgeDetails.item,
                delivery = weighBridgeDetails.delivery.toString(),
                customerNum = weighBridgeDetails.customerNum.toString(),
                purchaseDocNum = weighBridgeDetails.purchaseDocNum.toString(),
                purchaseDocDesc = weighBridgeDetails.purchaseDocDesc.toString(),
                batchNumber = "",
                materialName = weighBridgeDetails.materialName,
                materialCode = weighBridgeDetails.materialCode,
                supplierName = weighBridgeDetails.supplierName,
                supplierCode = weighBridgeDetails.supplierCode,
                deliveryItem = weighBridgeDetails.deliveryItem,
                bagType = weighBridgeDetails.bagType,
                bagCount = weighBridgeDetails.bagCount,
                bagWeight = weighBridgeDetails.bagWeight,
                unitsOfMeasure = weighBridgeDetails.unitsOfMeasure,
                netWeight = weighBridgeDetails.netWeight,
                grossWeight = weighBridgeDetails.grossWeight,
                weighBridgeId = weighBridgeDetails.weighBridgeId,
                challan = weighBridgeDetails.challan,
                plant = weighBridgeDetails.plant,
                direction = weighBridgeDetails.direction,
                weighBridgeType = weighBridgeDetails.weighBridgeType,
                erdat = weighBridgeDetails.erdat,
                ertim = weighBridgeDetails.ertim,
                qcStatus = weighBridgeDetails.qcStatus,
                vehicleNumber = weighBridgeDetails.vehicleNumber,
                storageLocationCode = weighBridgeDetails.storageLocationCode,
                transportVendorCode = weighBridgeDetails.transportVendorCode,
                contactNumber = weighBridgeDetails.contactNumber,
                driverName = weighBridgeDetails.driverName,
                grnNumber = weighBridgeDetails.grnNumber,
                finalApproval = weighBridgeDetails.finalApproval,
                qualityDetails = qtyParams
            )
        )
    }

    return qualityPostList
}
