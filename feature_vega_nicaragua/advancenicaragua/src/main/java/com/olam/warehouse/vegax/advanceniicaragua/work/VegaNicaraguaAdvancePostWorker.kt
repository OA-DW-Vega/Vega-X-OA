package com.olam.warehouse.vegax.grnnicaragua.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaAdvanceDao
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.ADVANCE_DATA
import com.olam.warehouse.presentation.utils.UIUtils.ADVANCE_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.FORWARD_PO_OUTPUT_DATA
import com.olam.warehouse.vegax.advanceniicaragua.data.api.VegaNicaraguaAdvanceApi
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaNicaraguaAdvancePostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaAdvanceApi by inject()
        val dao: VegaNicaraguaAdvanceDao by inject()
        val wbid = inputData.getString(ADVANCE_DATA) ?: ""
        val postData = dao.getAdvanceDetailsByTempId(wbid)
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

        val post = VegaNicaraguaAdvancePostRequest()
        post.documentNumber = postData.promissoryNumber
        post.vendorNo=postData.vendorCode
        post.vendorName=postData.vendorName
        post.totalAmount=postData.requestedAdvanceAmount
        post.plant = getPlantDetails()
        post.key=currentKey
        post.advanceFlag=true

        try {
            val response =
                api.syncAdvanceData(post).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {

                    if((respData.cashJournalDocumentNumber.isNullOrEmpty()) || respData.accountingDocNumber.isNullOrEmpty()) {
                        dao.updateErrorAdvanceDetails(wbid,respData.errorMessage)
                        Result.failure(workDataOf(ADVANCE_OUTPUT_DATA to respData.errorMessage))
                    }else
                    {
                        postData.syncStatusMsg = respData.cashJournalMessage
                        postData.documentNumber = respData.cashJournalDocumentNumber
                        postData.accountingNumber = respData.accountingDocNumber
                        postData.erdat = DateUtils.getCurrentTimeInMills().toString()
                        postData.syncStatus = true
                        dao.saveAdvanceDetailsData(postData)
                        Result.success(workDataOf(ADVANCE_OUTPUT_DATA to resp?.data?.cashJournalDocumentNumber))
                    }
                } else {
                    val msg = resp?.message ?: resp?.errors
                    dao.updateErrorAdvanceDetails(wbid, msg.toString())
                    Result.failure(workDataOf(ADVANCE_OUTPUT_DATA to msg))
                }
            } else {

                dao.updateErrorAdvanceDetails(wbid, response.message())

                Result.failure(workDataOf(ADVANCE_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(ADVANCE_OUTPUT_DATA to error.message))
        }
    }
}




