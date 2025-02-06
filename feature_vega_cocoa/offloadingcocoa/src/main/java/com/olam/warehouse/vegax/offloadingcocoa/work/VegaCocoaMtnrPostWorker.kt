package com.olam.warehouse.vegax.offloadingcocoa.work

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getBase64FromFile
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.dao.VegaCoCoaOffloadDao
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.offloadingcocoa.data.api.VegaCoCoaOffloadingApi
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.utils.convertKgToMT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaCocoaMtnrPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao: VegaCoCoaOffloadDao by inject()
        val api: VegaCoCoaOffloadingApi by inject()
        val wbid = inputData.getString(UIUtils.RECEIVING_DATA) ?: ""
        val receivingData = dao.getReceivingWithLineItemWbid(wbid)

        val charset = Charsets.UTF_8
        val byteArray =
            resizeBase64Image(getBase64FromFile(receivingData.receiving.imagePath.toString())!!).toByteArray(charset)

        try {
            val response =
                api.postOffloadingDetailSync(
                    VegaCoCoaOffloadingPostRequest(
                        contactNumber = receivingData.receiving.contactNumber,
                        driverName = receivingData.receiving.truckDriverName,
                        grnFlag = false,
                        grnNumber = "",
                        imageString = byteArray.let { it1 -> String(it1) },
                        imageUploadMsg = "",
                        key = getCurrentKey(),
                        lotDetails = prepareDeliveryList(receivingData),
                        message = "",
                        plant = getPlantDetails(),
                        success = false,
                        transportVendorCode = receivingData.receiving.transportVendorCode,
                        vehicleNumber = receivingData.receiving.vehicleNumber,
                        vehicleType = receivingData.receiving.vehicleType,
                        wayBillNo = receivingData.receiving.wayBillNo
                    )
                ).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    receivingData.receiving.tempGrnNumber = respData.grnNumber.toString()
                    //receivingData.receiving.weighBridgeId = respData.weighBridgeId.toString()
                    receivingData.receiving.transitLossDocNo = if (!respData.lotDetails.isNullOrEmpty()) respData.lotDetails[0].documentNum else ""
                    receivingData.receiving.isSynced = true
                    receivingData.receiving.isProgress = false
                    receivingData.receiving.syncStatusMsg = resp.message
                    receivingData.receiving.status = Status.RECEVING_COMPLETED
                    dao.saveMtnrReceiving(receivingData.receiving)
                    Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to receivingData.receiving.tempGrnNumber))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    receivingData.receiving.isSynced = false
                    receivingData.receiving.isProgress = false
                    receivingData.receiving.syncStatusMsg = msg
                    receivingData.receiving.status = Status.SYNC_ERROR
                    dao.saveMtnrReceiving(receivingData.receiving)
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                }

            } else {
                receivingData.receiving.isSynced = false
                receivingData.receiving.isProgress = false
                receivingData.receiving.syncStatusMsg = response.message()
                receivingData.receiving.status = Status.SYNC_ERROR
                dao.saveMtnrReceiving(receivingData.receiving)
                Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to error.message))
        }
    }

    private fun prepareDeliveryList(receiving: VegaCoCoaReceivingMtnrWithLots): List<VegaCoCoaOffloadingDeliveryDetail> {
        var batchList = mutableListOf<VegaCoCoaReceiveLots>()
        var bagList = arrayListOf<VegaCoCoaOffloadingBagMaterial>()

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

        val list = ArrayList<VegaCoCoaOffloadingDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in batchList) {
            val deliveryDetail = VegaCoCoaOffloadingDeliveryDetail()
            var bagItems = bagList.toMutableList()/*.filter { it.batchNumber == item.batch }*/
            deliveryDetail.batchNumber = item.batch
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.netWeight = item.editedWeight?.trim() ?: "0.0"
            deliveryDetail.recStorageLocation = receiving.receiving.storageLocationCode
            deliveryDetail.storageLocationCode = receiving.receiving.supplierCode
            deliveryDetail.storageLocation = receiving.receiving.supplierCode
            deliveryDetail.purchaseDocNum = receiving.receiving.purchaseDocNum
            deliveryDetail.purchaseDocDesc = receiving.receiving.purchaseDocDesc
            deliveryDetail.endLotFlag = receiving.receiving.endLot
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.posnr
            deliveryDetail.unitsOfMeasure = item.uom
            deliveryDetail.plant = getPlantDetails().plantId
            deliveryDetail.weighBridgeId = receiving.receiving.weighBridgeId
            deliveryDetail.weighBridgeType = receiving.receiving.weighBridgeType
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            bagItems.forEach { item1 ->
                grossWeight = item1.grossWeight.toDouble()/*grossWeight.plus(item1.grossWeight.toDouble())*/
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            }
            deliveryDetail.grossWeight = if (deliveryDetail.unitsOfMeasure.equals("MT"))
                convertKgToMT(grossWeight.toString().trim()) else
                grossWeight.toString().trim()//
            deliveryDetail.tareWeight =
                if (deliveryDetail.unitsOfMeasure.equals("MT"))
                    convertKgToMT(tareWeight.toString().trim()) else
                    tareWeight.toString().trim()//
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoCoaOffloadingBagMaterial>()
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

    fun resizeBase64Image(base64image: String): String {
        val encodeByte = Base64.decode(base64image.toByteArray(), Base64.DEFAULT)
        val options = BitmapFactory.Options()
        options.inPurgeable = true
        var image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size, options)
        if (image.height <= 400 && image.width <= 400) {
            return base64image
        }
        image = Bitmap.createScaledBitmap(image, 200, 200, false)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        System.gc()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

}




