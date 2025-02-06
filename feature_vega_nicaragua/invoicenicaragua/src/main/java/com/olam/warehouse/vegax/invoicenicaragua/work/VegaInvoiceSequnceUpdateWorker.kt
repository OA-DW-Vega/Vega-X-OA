package com.olam.warehouse.vegax.invoicenicaragua.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.invoicenicaragua.data.api.VegaNicaraguaInvoiceApi
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaUpdateInvoiceSequencePost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Created by Baskaran Kannan on 10/28/2020.
 */

@Suppress("UNCHECKED_CAST")
class VegaInvoiceSequnceUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaInvoiceApi by inject()
        val batchNumber = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        val invoiceNo = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
//        val batchNumber = inputData.getString(UIUtils.GRN_DATA) ?: ""
        val lotSequence = ""/*batchNumber.substring(batchNumber.length - 5)*/
        val grnSequence = ""
        var invoiceSequence = ""
        var invoiceSequence1 = invoiceNo.substring(invoiceNo.length - 5)
        when (invoiceSequence1.length) {
            1 -> invoiceSequence1 = "0000".plus(invoiceSequence1.toString())
            2 -> invoiceSequence1 = "000".plus(invoiceSequence1.toString())
            3 -> invoiceSequence1 = "00".plus(invoiceSequence1.toString())
            4 -> invoiceSequence1 = "0".plus(invoiceSequence1.toString())
            5 -> invoiceSequence1 = invoiceSequence1.toString()
        }
        // if (invoiceSequence1.isNotEmpty()) invoiceSequence = (invoiceSequence1.toInt() - 1).toString()
        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        val year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
            (currentyear+1).toString() else currentyear.toString()

        val prefix1 = PreferenceHelper.get(Constants.USER_NAME, "")
        val postData = VegaNicaraguaUpdateInvoiceSequencePost(
            getPlantDetails(),
            prefix1,
            year,
            lotSequence,
            "N",
            "Y",
            invoiceSequence1,
            grnSequence,
            "N",
            Constants.INVOICE_SEQUENCE
        )

        try {
            val response =
                api.updateInvoiceSequenceWorker(postData).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    //saveInvoiceSequence()
                    Result.success()
                } else {
                    val msg = resp?.message ?: resp?.errors
                    // saveInvoiceSequence()
                    Result.failure()
                }
            } else {
                // saveInvoiceSequence()
                Result.failure()
            }

        } catch (error: Throwable) {
            Result.failure()
        }
    }
}
