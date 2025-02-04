 package com.olam.warehouse.vegax.grnnicaragua.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveGrnSequence
import com.olam.warehouse.master.common.utils.saveInvoiceSequence
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.grnnicaragua.data.api.VegaNicaraguaGrnApi
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.grnnicaragua.utils.saveLotSequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

/**
 * Created by Baskaran Kannan on 10/28/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaLotSequnceUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaGrnApi by inject()
        val batchNumber = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        val invoiceNo = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
        val grnSequncce1 = PreferenceHelper.get(Constants.GRN_SEQUENCE, "")
        val grnType = inputData.getString(UIUtils.GRN_DATA) ?: ""
        var lotSequence = batchNumber.substring(batchNumber.length - 5)
        when (lotSequence.length) {
            1 -> lotSequence="0000".plus(lotSequence.toString())
            2 ->lotSequence= "000".plus(lotSequence.toString())
            3 -> lotSequence="00".plus(lotSequence.toString())
            4 ->lotSequence= "0".plus(lotSequence.toString())
            5 -> lotSequence.toString()
        }
        var grnSequncce = grnSequncce1.substring(grnSequncce1.length - 5)
        when (grnSequncce.length) {
            1 ->grnSequncce= "0000".plus(grnSequncce.toString())
            2 -> grnSequncce="000".plus(grnSequncce.toString())
            3 -> grnSequncce="00".plus(grnSequncce.toString())
            4 ->grnSequncce= "0".plus(grnSequncce.toString())
            5 -> grnSequncce.toString()
        }
        var invoiceSequence = ""
        var isInvoiceSequence = ""
        if ((grnType.contains("fixed", true) || grnType.equals("spot", true)) && invoiceNo.isNotEmpty()) {
            invoiceSequence = invoiceNo.substring(invoiceNo.length - 5)
            when (invoiceSequence.length) {
                1 -> invoiceSequence="0000".plus(invoiceSequence.toString())
                2 ->invoiceSequence= "000".plus(invoiceSequence.toString())
                3 ->invoiceSequence= "00".plus(invoiceSequence.toString())
                4 -> invoiceSequence="0".plus(invoiceSequence.toString())
                5 -> invoiceSequence.toString()
            }
            isInvoiceSequence = "Y"
        } else {
            invoiceSequence = ""
            isInvoiceSequence = "N"
        }

        val rightNow = Calendar.getInstance()
        val year = rightNow.get(Calendar.YEAR).toString()
        val prefix1 = PreferenceHelper.get(Constants.USER_NAME, "")
        val postData = VegaNicaraguaUpdateLotSequencePost(
            getPlantDetails(),
            prefix1,
            year,
            lotSequence,
            "Y",
            isInvoiceSequence,
            invoiceSequence,
            grnSequncce,
            "Y",
            "",
            "N"
        )

        try {
            val response =
                api.updateLotSequenceWorker(postData).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    saveLotSequence(batchNumber)
                    saveGrnSequence()
                    if ((grnType.contains("fixed", true) || grnType.equals(
                            "spot",
                            true
                        )) && invoiceNo.isNotEmpty()
                    ) saveInvoiceSequence()
                    Result.success()
                } else {
                    val msg = resp?.message ?: resp?.errors
                    saveLotSequence(batchNumber)
                    saveGrnSequence()
                    if ((grnType.contains("fixed", true) || grnType.equals(
                            "spot",
                            true
                        )) && invoiceNo.isNotEmpty()
                    ) saveInvoiceSequence()
                    Result.failure()
                }
            } else {
                saveLotSequence(batchNumber)
                saveGrnSequence()
                if ((grnType.contains("fixed", true) || grnType.equals(
                        "spot",
                        true
                    )) && invoiceNo.isNotEmpty()
                ) saveInvoiceSequence()
                Result.failure()
            }

        } catch (error: Throwable) {
            saveLotSequence(batchNumber)
            saveGrnSequence()
            if ((grnType.contains("fixed", true) || grnType.equals(
                    "spot",
                    true
                )) && invoiceNo.isNotEmpty()
            ) saveInvoiceSequence()
            Result.failure()
        }
    }
}
