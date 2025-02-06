package com.olam.warehouse.vegax.thirdpartysalescoffee.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.api.VegaCoffeeThirdPartyApi
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.UpdateTPLotSequencePost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Created by Baskaran Kannan on 6/2/2023.
 */

@Suppress("UNCHECKED_CAST")
class VegaTPLotSequnceUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaCoffeeThirdPartyApi by inject()
//        val batchNumber = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        val batchNumber = inputData.getString(UIUtils.LOT_DETAIL) ?: ""
        var lotSequence = batchNumber.substring(batchNumber.length - 5)
        when (lotSequence.length) {
            1 -> lotSequence="0000".plus(lotSequence.toString())
            2 ->lotSequence= "000".plus(lotSequence.toString())
            3 -> lotSequence="00".plus(lotSequence.toString())
            4 ->lotSequence= "0".plus(lotSequence.toString())
            5 -> lotSequence.toString()
        }

        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        var year: String? = ""
        year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
            (currentyear+1).toString() else currentyear.toString()
        val prefix1 = PreferenceHelper.get(Constants.USER_NAME, "")
        var prefix3 = ""
        prefix3 = Constants.LOT_SEQUENCE
        val postData = UpdateTPLotSequencePost(
            plant = getPlantDetails(),
            prefix1 = prefix1,
            year = year,
            sequence = lotSequence,
            isLotSequence = "Y",
            isInSequence = "N",
            invoiceSequence = "",
            grnSequence = "",
            isGrnRefSequence = "N",
            poSequence = "",
            isPoRefSequence = "N",
            prefix3 = prefix3
        )

        try {
            val response =
                api.updateLotSequenceWorker(postData).execute()
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
