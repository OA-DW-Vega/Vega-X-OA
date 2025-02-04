package com.olam.warehouse.odreceiving.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.common.data.domain.model.ODMasterUseCase
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.data.api.DOReceivingApi
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingPost
import com.olam.warehouse.odreceiving.data.domain.model.QrCodeMapping
import com.olam.warehouse.odreceiving.data.domain.model.QrCodes
import com.olam.warehouse.odreceiving.data.domain.model.ReplaceQrCodes
import com.olam.warehouse.odreceiving.data.domain.usecase.DOReceivingUseCase
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_OUTPUT_DATA
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.fromJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
@Suppress("UNCHECKED_CAST")
class DOReceivingPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val useCase: DOReceivingUseCase by inject()
        val useCaseMaster: ODMasterUseCase by inject()
        val api: DOReceivingApi by inject()
        val receivingJson = inputData.getString(RECEIVING_DATA) ?: ""
        val receiving = Gson().fromJson<DOReceivingWithLineItems>(receivingJson)
        var bagitems = useCaseMaster.getAllDOBagsOfflineInfo()
        var missedQrCodes = mutableListOf<QrCodes>()
        var replaceQrCodes = mutableListOf<ReplaceQrCodes>()
        missedQrCodes.clear()
        replaceQrCodes.clear()
        val plant = getPlantDetails()
        var mDoBags=bagitems.filter{ it.lotTransactionId.contains(receiving.lineItems.get(0).txnId.toString()) }
        var transactionIdList=mDoBags.map { it.transactionId}.distinct()
        transactionIdList.forEach { id ->
            var temptransid = mDoBags.filter { it.transactionId.equals(id) }
            var missedQrCode = mutableListOf<String>()
            var qrCodeMapping = mutableListOf<QrCodeMapping>()
            temptransid.forEach { doBag ->

                if (doBag.bagMissed != null && doBag.bagMissed) {
                    missedQrCode.add(doBag.bagQrCode.toString())
                }
                if (doBag.newQrCode != 0) {
                    qrCodeMapping.add(QrCodeMapping(
                        newQrCode = doBag.newQrCode.toString(),
                        oldQrCode = doBag.bagQrCode.toString()
                    ))
                }
            }
            if (qrCodeMapping.size != 0) {
                val replaceQrCode = ReplaceQrCodes(transactionId = id, qrCodeMapping = qrCodeMapping)
                replaceQrCodes.add(replaceQrCode)
            }
            if (missedQrCode.size != 0) {
                val QrCode = QrCodes(transactionId = id, qrCodes = missedQrCode)
                missedQrCodes.add(QrCode)
            }
        }
        try {
            /*val response =
                api.postReceivingItem(DOReceivingPostLineItem(getCurrentKey(), getPlantDetails(), receiving.lineItems))
                    .execute()
            */
            val response =
                    api.postReceivingItem(DOReceivingPost(
                            getCurrentKey(),
                            plant,
                            missedQrCodes,
                            replaceQrCodes,
                            listOf(receiving.receiving)
                    )).execute()

            if (response.isSuccessful) {
                val resp = response.body()
                val respData = resp?.data
                val isSuccess = resp?.success ?: true
                if (respData != null && isSuccess) {
                    receiving.receiving.wbId = respData.wbId.toString()
                    receiving.receiving.isSynced = true
                    receiving.receiving.status = Status.RECEVING_COMPLETED
                    useCase.deleteReceiving(receiving.receiving)
                    useCaseMaster.updateTempIdToWbid(
                        respData.wbId.toString(),
                        respData.grossWeight.toString(),
                        receiving.receiving.tmpWbId
                    )
                    Result.success(workDataOf(RECEIVING_OUTPUT_DATA to resp.data.wbId))
                } else {
                    val msg = resp?.message ?: resp?.errors
                    Result.failure(workDataOf(RECEIVING_OUTPUT_DATA to msg))
                }

            } else {
                Result.failure(workDataOf(RECEIVING_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(RECEIVING_OUTPUT_DATA to error.message))
        }
    }
}
