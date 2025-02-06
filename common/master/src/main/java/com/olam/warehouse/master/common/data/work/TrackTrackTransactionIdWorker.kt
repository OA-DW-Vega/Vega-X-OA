package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.dao.TrackTraceDao
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.presentation.utils.TRANS_ID_OUTPUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TrackTrackTransactionIdWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO){
        val api: MasterApi by inject()
        val trackTraceDao: TrackTraceDao by inject()

        try {
            val response = api.getTranscationIdDetails(getCurrentKey(), null).execute()
            if (response.isSuccessful) {
                trackTraceDao.clearTTTransactionIdDetails()
                val data = response.body()
//                data?.data?.let {trackTraceDao.insertMultipleTransIdList(prepareTransactionListData(it))}
                data?.data?.let {
                    var transactionList = ArrayList<TrackTraceTransactionIdDetails>()
                    it.forEach {
                        if (it.vendorDetails.isNotEmpty()) {
                            var data = TrackTraceTransactionIdDetails()
                            var vendorDetails = it.vendorDetails.get(0)
                            data.dwTransactionId = it.dwTransactionId
                            data.vendorName = vendorDetails.vendorName
                            data.vendorCode = vendorDetails.vendorCode
                            data.country = vendorDetails.country
                            data.product = vendorDetails.product
                            data.totalProductionInMetricTon = vendorDetails.totalProductionInMetricTon
                            data.dateGeoLocationCaptured = vendorDetails.dateGeoLocationCaptured
                            data.compliantFlag = vendorDetails.compliantFlag
                            transactionList.add(data)
                        }
                    }
                    trackTraceDao.insertMultipleTransIdList(transactionList)
                }
                Result.success()
            } else if (response.body() == null) {
                Result.failure(workDataOf(TRANS_ID_OUTPUT_DATA to response.raw().message))
            } else {
                Result.retry()
            }
            //Result.success()
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.failure(workDataOf(TRANS_ID_OUTPUT_DATA to error.message))
        }
    }

    private fun prepareTransactionListData(transactionIdDetails: List<TrackTraceModelTransactionIdDetails>): List<TrackTraceTransactionIdDetails> {
        var transactionList = ArrayList<TrackTraceTransactionIdDetails>()
        transactionIdDetails.forEach {
            if (it.vendorDetails.isNotEmpty()) {
                var data = TrackTraceTransactionIdDetails()
                var vendorDetails = it.vendorDetails.get(0)
                data.dwTransactionId = it.dwTransactionId
                data.vendorName = vendorDetails.vendorName
                data.vendorCode = vendorDetails.vendorCode
                data.country = vendorDetails.country
                data.product = vendorDetails.product
                data.totalProductionInMetricTon = vendorDetails.totalProductionInMetricTon
                data.dateGeoLocationCaptured = vendorDetails.dateGeoLocationCaptured
                data.compliantFlag = vendorDetails.compliantFlag
                transactionList.add(data)
            }
        }

        return transactionList
    }
}
