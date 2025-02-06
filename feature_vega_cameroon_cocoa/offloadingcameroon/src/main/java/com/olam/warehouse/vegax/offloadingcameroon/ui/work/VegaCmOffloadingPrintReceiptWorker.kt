package com.olam.warehouse.vegax.offloadingcameroon.ui.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.login.di.injectFeature
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.offloadingcameroon.data.api.VegaCameroonOffloadingApi
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.CameroonSavePrintTicket
import com.olam.warehouse.vegax.offloadingcameroon.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VegaCmOffloadingPrintReceiptWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api:VegaCameroonOffloadingApi by inject()

        val grnNo = inputData.getString(UIUtils.GRN_DATA) ?: ""
        val batchno = inputData.getString(BATCH_NO) ?: ""
        val isType = inputData.getString(PRINT_TYPE) ?:""
        val material = inputData.getString(UIUtils.MATERIAL) ?: ""
        val curdate = DateUtils.getcurdateformat()
        val printTicketKeys = PreferenceHelper.get(OFFLOADING_TICKET_PRINT_KEY, "")


        try {
            val response = api.postprintformatdata(
                Constants.OFFLOADING.toRequestBody(MultipartBody.FORM),
                if (isType.equals("receipt")) TYPE_R.toRequestBody(MultipartBody.FORM)
                else TYPE_T.toRequestBody(MultipartBody.FORM),
                grnNo.toRequestBody(MultipartBody.FORM),
                batchno.toRequestBody(MultipartBody.FORM),
                material.toRequestBody(MultipartBody.FORM),
                curdate.toRequestBody(MultipartBody.FORM),
                getCurrentKey().split("_")[1].toRequestBody(MultipartBody.FORM),
                printTicketKeys.toRequestBody(MultipartBody.FORM)
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
