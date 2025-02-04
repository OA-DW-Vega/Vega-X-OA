package com.olam.warehouse.vegax.grnnicaragua.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaForwardPODao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPOPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.FORWARD_PO_DATA
import com.olam.warehouse.presentation.utils.UIUtils.FORWARD_PO_OUTPUT_DATA
import com.olam.warehouse.vegax.forwardponicaragua.data.api.VegaNicaraguaForwardPOApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaNicaraguaForwardPoPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaForwardPOApi by inject()
        val dao: VegaNicaraguaForwardPODao by inject()
        val wbid = inputData.getString(FORWARD_PO_DATA) ?: ""
        val receivingData = dao.getForwardPODetails(wbid)
        val priceDetails = dao.getForwardPOPriceDetailsByTempId(wbid)

        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val post = VegaNicaraguaForwardPoPost()
        post.cascara = receivingData.cascara
        post.certificate = receivingData.certificate
        post.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        post.currency = receivingData.currency
        post.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        post.exchangeRate = receivingData.exchangeRate
        post.grade = receivingData.grade
        post.humedad = receivingData.humedad
        post.key = currentKey
        post.materialCode = receivingData.materialCode
        post.materialName = receivingData.materialName
        post.netPrice = receivingData.netPrice
        post.netWeight = receivingData.netWeight
        post.plant = getPlantDetails()
        post.priceDetails = prepareForwardPoPriceDetails(ArrayList(priceDetails))
        post.pricePerUnit = receivingData.pricePerUnit
        post.qualityGradeDesc = receivingData.qualityGradeDesc
        post.rendimientoBruto = receivingData.rendimientoBruto
        post.unitsOfMeasure = receivingData.unitsOfMeasure
        post.vendorCode = receivingData.vendorCode
        post.vendorName = receivingData.vendorName
        post.vendorAddress = receivingData.vendorAddress
        post.receiptNetPrice = receivingData.receiptNetPrice
        post.deliveryDate = receivingData.deliveryDate
        post.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")


        try {
            val response =
                api.syncForwardPOData(post).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    receivingData.syncStatusMsg = respData.message
                    receivingData.poNumber = respData.poNumber
                    receivingData.syncStatus = true
                    dao.saveForwardPOData(receivingData)
                    dao.saveForwardPOPriceDetails(priceDetails)
                    Result.success(workDataOf(FORWARD_PO_OUTPUT_DATA to resp?.data?.poNumber))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    dao.updateErrorForwardData(wbid, msg.toString())
                    Result.failure(workDataOf(FORWARD_PO_OUTPUT_DATA to msg))
                }
            } else {

                dao.updateErrorForwardData(wbid, response.message())

                Result.failure(workDataOf(FORWARD_PO_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(FORWARD_PO_OUTPUT_DATA to error.message))
        }
    }
}

private fun prepareForwardPoPriceDetails(forwardPoPriceDetails: ArrayList<VegaNicaraguaForwardPOPriceDetails>): java.util.ArrayList<VegaNicaraguaGrnPriceDetails> {
    val priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

    forwardPoPriceDetails.filter{ it.differential!!.isNotEmpty()
    }.forEach {
        val lineItem = VegaNicaraguaGrnPriceDetails()
        lineItem.fieldName = it.fieldName
        lineItem.companyCode = it.companyCode
        lineItem.division = it.division
        lineItem.plant = it.plant
        lineItem.purchasingOrg = it.purchasingOrg
        lineItem.purchasingGroup = it.purchasingGroup
        lineItem.description = it.description
        lineItem.priceDate = it.priceDate
        lineItem.price = it.price
        lineItem.differential = it.differential
        lineItem.currency = it.currency
        lineItem.baseUnit = it.baseUnit
        lineItem.createdOn = it.createdOn
        lineItem.percentage = it.percentage
        priceDetails.add(lineItem)
    }
    return priceDetails
}


