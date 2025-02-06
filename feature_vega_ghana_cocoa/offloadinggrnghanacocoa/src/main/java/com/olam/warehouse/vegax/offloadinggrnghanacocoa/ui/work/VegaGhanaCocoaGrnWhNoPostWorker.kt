package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.login.ui.printformats.OFFLOADING_RECEIPT_PRINT_KEY
import com.olam.warehouse.login.ui.printformats.OFFLOADING_TICKET_PRINT_KEY
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.api.VegaGhanaCocoaOffloadingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VegaGhanaCocoaGrnWhNoPostWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    @SuppressLint("SuspiciousIndentation")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaGhanaCocoaOffloadingApi by inject()
        val dao: VegaEcuadorOffloadingDao by inject()
        val useCase: MasterUseCase by inject()

        val wbid = inputData.getString(UIUtils.RECEIVING_DATA) ?: ""
        val whNumber = inputData.getString(UIUtils.WAREHOUSE_NUMBER) ?: ""
        val receivingData = dao.getOffloadingWithLineItem(wbid)

        val plantId= getPlantDetails().plantId
        val wbType="PROCURE"

        try {
            val response = api.validateReceiptWhNumbers(getCurrentKey(),plantId,whNumber,wbType).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data

                if (respData != null) {
                    receivingData.receiving.remarks= respData.msgtype
                       if(respData.msgtype.equals("E")){
                         receivingData.receiving.invoiceFlag=false
                       }else if(respData.msgtype.equals("S")){
                           receivingData.receiving.invoiceFlag=true
                       }
                    useCase.saveReceiving(receivingData.receiving)
                    Result.success(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to respData.msgtype))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to msg))
                }
            } else {
                Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to response.message()))

            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(UIUtils.RECEIVING_OUTPUT_DATA to error.message))
        }
    }

}
