package com.olam.warehouse.vegax.exportsalesindo.works

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeExportSalesDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.model.IndoContainerWithLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.exportsalesindo.data.api.VegaIndoCoffeeExportSalesApi
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesDeliveryDetail
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalesindo.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 4/28/2021.
 */
@Suppress("UNCHECKED_CAST")
class VegaIndoCoffeeExportSalesPostWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao: VegaCoffeeExportSalesDao by inject()
        val api: VegaIndoCoffeeExportSalesApi by inject()
        val tmpId = inputData.getString(UIUtils.DISPATCH_DATA) ?: ""
        val otWithContainerWithLots = dao.getOTWithContainerIndoWork(tmpId)
        val salesOrder = otWithContainerWithLots.salesOrder
        val salesOderId = salesOrder.saleOrderId
        val materialList = prepareSalesOrderToMaterialList(salesOrder)
        val containerList = otWithContainerWithLots.lineItems as ArrayList<IndoContainerWithLots>
        val postData = PreparPost(containerList, materialList)

        try {
            val response = api.postDeliveryDetailOffline(postData).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                val data = respData?.deliveryDetails
                val postedLot = arrayListOf<VegaCoffeeExportSalesLots>()
                data?.forEach {
                    val lot = VegaCoffeeExportSalesLots()
                    lot.saleOrderId = salesOderId.toString()
                    lot.batchNumber = it.batchNumber.toString()
                    lot.delivery = it.delivery.toString()
                    lot.deliveryItem = it.deliveryItem.toString()
                    lot.weighBridgeId = it.weighBridgeId.toString()
                    lot.deliveryFlag = it.deliveryFlag
                    lot.pickingFlag = it.pickingFlag
                    lot.pgiFlag = it.pgiFlag
                    lot.containerFlag = it.containerFlag
                    lot.tmpId = tmpId
                    lot.synStatusMsg = resp.message
                    postedLot.add(lot)
                }
                val isFailure = postedLot.any { !it.deliveryFlag!! || !it.containerFlag!! || !it.pickingFlag!! }
                postedLot.forEach { lot ->
                    if (lot.batchNumber.isEmpty()) {
                        if (lot.synStatusMsg?.isNotEmpty() == true) dao.updateSalesOrderStatus(
                            lot.tmpId,
                            lot.synStatusMsg,
                            3,
                            false,
                            false,
                            false,
                            false
                        )
                        else dao.updateSalesOrderOfflineStatus(lot.tmpId)
                    } else {
                        if (!isFailure) {
                            dao.updateSalesOrderStatus(
                                lot.tmpId,
                                lot.synStatusMsg,
                                4,
                                lot.deliveryFlag ?: false,
                                lot.pickingFlag ?: false,
                                lot.containerFlag ?: false,
                                true
                            )
                            dao.updateLotStatus(
                                lot.batchNumber,
                                lot.delivery.toString(),
                                lot.deliveryItem.toString(),
                                lot.weighBridgeId.toString(),
                                lot.deliveryFlag ?: false,
                                lot.pickingFlag ?: false,
                                lot.containerFlag ?: false,
                                lot.tmpId.toString()
                            )
                        } else {
                            dao.updateSalesOrderStatus(
                                lot.tmpId,
                                lot.synStatusMsg,
                                3,
                                lot.deliveryFlag ?: false,
                                lot.pickingFlag ?: false,
                                lot.containerFlag ?: false,
                                false
                            )
                            dao.updateLotStatus(
                                lot.batchNumber,
                                lot.delivery.toString(),
                                lot.deliveryItem.toString(),
                                lot.weighBridgeId.toString(),
                                lot.deliveryFlag ?: false,
                                lot.pickingFlag ?: false,
                                lot.containerFlag ?: false,
                                lot.tmpId.toString()
                            )
                        }
                    }
                }
                Result.success(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to respData?.batchNumber))
            } else {
                dao.updateSalesOrderStatus(tmpId, response.message(), 3, false, false, false,false)
                Result.failure(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            dao.updateSalesOrderStatus(tmpId, error.message, 3, false, false, false,false)
            Result.failure(workDataOf(UIUtils.DISPATCH_OUTPUT_DATA to error.message))
        }
    }
}

private fun PreparPost(
    containerList: ArrayList<IndoContainerWithLots>,
    materialList: ArrayList<IndoExporSalesMaterialList>
): VegaIndoCoffeeExportSalesPostRequest {
    return VegaIndoCoffeeExportSalesPostRequest(
        key = getCurrentKey(),
        plant = getPlantDetails(),
        operatorName = "",
        batchNumber = "",
        delFlag = "",
        deliveryDetails = prepareDeliveryList(containerList, materialList),
        weighmentType = ""
    )
}

private fun prepareDeliveryList(
    containerList: ArrayList<IndoContainerWithLots>,
    materialList: ArrayList<IndoExporSalesMaterialList>
): List<VegaIndoCoffeeExportSalesDeliveryDetail> {
    val list = ArrayList<VegaIndoCoffeeExportSalesDeliveryDetail>()
    val year: Int = Calendar.getInstance().get(Calendar.YEAR)
    var materialUOM: String = ""
    if (materialList.size > 0) materialUOM = materialList[0].meins.toString()
    containerList.forEach { con ->
        con.lots.forEach { item ->
            val deliveryDetail = VegaIndoCoffeeExportSalesDeliveryDetail()
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.netWeight = convertWeight(
                item.editedWeight?.trim() ?: "0.0",
                materialUOM, item.unitOfMeasure ?: ""
            )
            deliveryDetail.salesOrderNum = item.saleOrderId
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            val salesItem = materialList.filter { it.salesOrderId.equals(item.saleOrderId) }
                .filter { it.materialNumber.equals(item.materialCode) }
            if (salesItem.size > 0) deliveryDetail.salesItem = salesItem[0].salesItemNum
            deliveryDetail.weighBridgeId = item.weighBridgeId
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.pgiFlag = item.pgiFlag
            deliveryDetail.containerFlag = item.containerFlag
            deliveryDetail.unitsOfMeasure = materialUOM
            deliveryDetail.year = year.toString()
            deliveryDetail.grossWeight = deliveryDetail.netWeight
            deliveryDetail.grossWeight = item.weight
            deliveryDetail.containerNum = con.container.containerNumber
            deliveryDetail.toVendorCode = item.vendor
            list.add(deliveryDetail)
        }
    }
    return list
}

private fun convertWeight(weight: String, soUom: String, lotUom: String): String {
    if (soUom == lotUom) {
        return weight
    } else {
        if (soUom == UNIT_MT && lotUom == UNIT_KG) {
            return convertKgToMT(weight)
        } else if (soUom == UNIT_KG && lotUom == UNIT_MT) return convertMtToKg(weight)
    }
    return "0"
}
