package com.olam.warehouse.vegax.splitlot.work

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.vegax.splitlot.data.api.VegaCommonSplitLotApi
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaSplitLotSavePrintTicket
import com.olam.warehouse.vegax.splitlot.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * Created by Baskaran Kannan on 10/5/2022.
 */
@Suppress("UNCHECKED_CAST")
class VegaSplitLotPrintTicketWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaCommonSplitLotApi by inject()
        val printReceiptKeys = PreferenceHelper.get(TICKET_KEY, "")
        val batchno = inputData.getString(BATCH_NO) ?: ""
        val grnNo = inputData.getString(GRN_NO) ?: ""
        val material = inputData.getString(UIUtils.MATERIAL) ?: ""
        val isTicketType = inputData.getString(PRINT_TYPE) ?: ""
        val curdate = DateUtils.getcurdateformat()
        /*  val imageBytes = Base64.decode(printReceiptKeys, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val file = bitmapToFile(image, "file_Ticket", applicationContext) ?: File(printReceiptKeys)
        val request = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val fileBody = MultipartBody.Part.createFormData("file", file.name, request)*/
        try {
            val response = api.postprintformatdata(
                Constants.MTNR.toRequestBody(MultipartBody.FORM),
                if (isTicketType.equals(TYPE_T)) TYPE_T.toRequestBody(MultipartBody.FORM)
                else if (isTicketType.equals(TYPE_R)) TYPE_R.toRequestBody(MultipartBody.FORM)
                else TYPE_SAMPLE.toRequestBody(MultipartBody.FORM),
                grnNo.toRequestBody(MultipartBody.FORM),
                batchno.toRequestBody(MultipartBody.FORM),
                material.toRequestBody(MultipartBody.FORM),
                curdate.toRequestBody(MultipartBody.FORM),
                getCurrentKey().split("_")[1].toRequestBody(MultipartBody.FORM),
                printReceiptKeys.toRequestBody(MultipartBody.FORM)
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
