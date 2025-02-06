package com.olam.warehouse.vegax.qualityapprovecameroon.ui.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.login.ui.printformats.OFFLOADING_RECEIPT_PRINT_KEY
import com.olam.warehouse.login.ui.printformats.OFFLOADING_TICKET_PRINT_KEY
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.BATCH_NO
import com.olam.warehouse.presentation.utils.UIUtils.PRINT_TYPE
import com.olam.warehouse.presentation.utils.UIUtils.TYPE_R
import com.olam.warehouse.presentation.utils.UIUtils.TYPE_T
import com.olam.warehouse.vegax.qualityapprovecameroon.data.api.VegaQualityApproveCameroonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VegaCMQualityApprovalPrintReceiptWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api:VegaQualityApproveCameroonApi by inject()

        val grnNo = inputData.getString(UIUtils.GRN_DATA) ?: ""
        val batchno = inputData.getString(BATCH_NO) ?: ""
        val isType = inputData.getString(PRINT_TYPE) ?:""
        val material = inputData.getString(UIUtils.MATERIAL) ?: ""
        val curdate = DateUtils.getcurdateformat()
        val printTicketKeys = PreferenceHelper.get(OFFLOADING_TICKET_PRINT_KEY, "")
        val printReceiptKeys = PreferenceHelper.get(OFFLOADING_RECEIPT_PRINT_KEY, "")


        try {
            val response = api.postprintformatdata(
                Constants.QUALITY_APPROVAL.toRequestBody(MultipartBody.FORM),
                if (isType.equals(TYPE_R)) TYPE_R.toRequestBody(MultipartBody.FORM)
                else TYPE_T.toRequestBody(MultipartBody.FORM),
                grnNo.toRequestBody(MultipartBody.FORM),
                batchno.toRequestBody(MultipartBody.FORM),
                material.toRequestBody(MultipartBody.FORM),
                curdate.toRequestBody(MultipartBody.FORM),
                getCurrentKey().split("_")[1].toRequestBody(MultipartBody.FORM),
                (if(isType.equals(TYPE_R))printReceiptKeys else printTicketKeys).toRequestBody(
                    MultipartBody.FORM
                )
            ).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    Result.success()
                } else {
                    val msg = resp?.message ?: resp?.errors

                    Result.failure()
                }
            } else {

                Result.failure()
            }

        } catch (error: Throwable) {
            Result.failure()
        }
    }

}

