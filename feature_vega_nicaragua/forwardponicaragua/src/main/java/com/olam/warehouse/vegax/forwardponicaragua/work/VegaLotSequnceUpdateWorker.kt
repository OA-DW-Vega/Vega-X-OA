package com.olam.warehouse.vegax.forwardponicaragua.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.savePOSequence
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.forwardponicaragua.data.api.VegaNicaraguaForwardPOApi
import com.olam.warehouse.vegax.forwardponicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Created by Baskaran Kannan on 10/28/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaLotSequnceUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaForwardPOApi by inject()

        val poSequncce1 = PreferenceHelper.get(Constants.PO_SEQUENCE, "")

        var poSequncce = poSequncce1.substring(poSequncce1.length - 5)
        when (poSequncce.toString().length) {
            1 -> "0000".plus(poSequncce.toString())
            2 -> "000".plus(poSequncce.toString())
            3 -> "00".plus(poSequncce.toString())
            4 -> "0".plus(poSequncce.toString())
            5 -> poSequncce.toString()
        }
        val rightNow = Calendar.getInstance()
        val year = rightNow.get(Calendar.YEAR).toString()
        val prefix1 = PreferenceHelper.get(Constants.USER_NAME, "")
        val prefix3 = Constants.PO_SEQUENCE
        val postData = VegaNicaraguaUpdateLotSequencePost(
            plant = getPlantDetails(),
            prefix1 = prefix1,
            year = year,
            sequence = "",
            isLotSequence = "N",
            isInSequence = "N",
            invoiceSequence = "",
            grnSequence = "",
            isGrnRefSequence = "N",
            poSequence = poSequncce,
            isPoRefSequence = "Y",
            prefix3 = prefix3
        )

        try {
            val response =
                api.updateLotSequenceWorker(postData).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {

                    savePOSequence()
                    Result.success()
                } else {
                    val msg = resp?.message ?: resp?.errors
                    savePOSequence()
                    Result.failure()
                }
            } else {
                savePOSequence()

                Result.failure()
            }

        } catch (error: Throwable) {
            Result.failure()
        }
    }
}
