package com.olam.warehouse.vegax.qualitycoffee.work

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.util.Base64Utils.decode
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.vegax.qualitycoffee.data.api.VegaCoffeeQualityApi
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaSavePrintTicket
import com.olam.warehouse.vegax.qualitycoffee.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http2.Huffman.decode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.lang.Byte.decode
import java.lang.Long.decode
import java.util.*

/**
 * Created by Baskaran Kannan on 10/28/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaPrintFileReceiptWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaCoffeeQualityApi by inject()
        val grnNo = inputData.getString(UIUtils.GRN_DATA) ?: ""
        val batchno = inputData.getString(BATCH_NO) ?: ""
        val isType = inputData.getString(PRINT_TYPE) ?:""
        val palletNo = inputData.getString(UIUtils.LOTS) ?: ""
        val material = inputData.getString(UIUtils.MATERIAL) ?: ""
        val curdate = DateUtils.getcurdateformat()
        val printReceiptKeys = PreferenceHelper.get(MTNR_PRINT_KEY, "")
        val printSampleKeys = PreferenceHelper.get(MTNR_PRINT_SAMPLE_KEY, "")

        var postprint = NicaraguaSavePrintTicket(
            Constants.MTNR,
            if (isType.equals("receipt")) TYPE_R else  if (isType.equals("ticket")) TYPE_T else TYPE_SAMPLE,
            if (isType.equals("receipt")) grnNo else palletNo,
            batchno,
            if (!isType.equals("sample_ticket"))printReceiptKeys else printSampleKeys,
            material,
            curdate
        )
//        val imageBytes = Base64.decode(printReceiptKeys, 0)
//        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//        val file = bitmapToFile(image, "file", applicationContext )?: File(printReceiptKeys)
//        println("=======file==========/storage/emulated/0/Android/data/com.olam.warehouse.vegax/files/Download/file==$file")
//        val request = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
//        val fileBody = MultipartBody.Part.createFormData("file", file.name, request)


      //  var recepit: MultipartBody.Part = MultipartBody.Part.createFormData("file", TYPE_R, requestBody)
        try {
            // val response = api.saveprintformatWorker(postprint).execute()
            val response = api.postprintformatdata(
                Constants.MTNR.toRequestBody(MultipartBody.FORM),
                if (isType.equals("receipt")) TYPE_R.toRequestBody(MultipartBody.FORM)
                else  if (isType.equals("ticket")) TYPE_T.toRequestBody(MultipartBody.FORM)
                else TYPE_SAMPLE.toRequestBody(MultipartBody.FORM),
                if (isType.equals("receipt")) grnNo.toRequestBody(MultipartBody.FORM) else
                    palletNo.toRequestBody(MultipartBody.FORM),
                batchno.toRequestBody(MultipartBody.FORM),
                material.toRequestBody(MultipartBody.FORM),
                curdate.toRequestBody(MultipartBody.FORM),
                getCurrentKey().split("_")[1].toRequestBody(MultipartBody.FORM),
                if (!isType.equals("sample_ticket")) printReceiptKeys.toRequestBody(MultipartBody.FORM)
            else printSampleKeys.toRequestBody(MultipartBody.FORM)
            ).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.success
                if (respData == true) {
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
