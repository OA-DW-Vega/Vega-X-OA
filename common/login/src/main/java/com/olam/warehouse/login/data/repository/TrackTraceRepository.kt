package com.olam.warehouse.login.data.repository

import androidx.lifecycle.LiveData
import com.olam.warehouse.login.data.api.TrackTraceApi
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.common.dao.TrackTraceDao
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource

interface TrackTraceRepository {
    suspend fun getSourceLotDetails(key: String, sourceLotId: String):LiveData<Resource<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>>

    suspend fun getAllSourceLotIdList():LiveData<List<TrackTraceSourceLotDetails>>

    suspend fun   getAllTransIdList():LiveData<List<TrackTraceTransactionIdDetails>>

    suspend fun getOfflineSourceLotDetails(sourceLotId: String):LiveData<TrackTraceSourceLotDetails>
    suspend fun getSuppliers(purChaseType: String?): LiveData<List<VegaVendor>>

    suspend fun getFarmerList(): LiveData<List<VegaTrackTraceFarmerData>>

    suspend fun getFarmerEudrDetails(farmerData: String): LiveData<VegaTrackTraceFarmerData>

    suspend fun getOnlineTransactionIdDetails(transactionId: String):LiveData<Resource<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>>

    suspend fun getOfflineTransactionIdDetails(transactionId: String):LiveData<TrackTraceTransactionIdDetails>



}

class TrackTraceRepositoryImpl(private val trackTraceApi: TrackTraceApi, private val dao: TrackTraceDao):TrackTraceRepository {
    override suspend fun getSourceLotDetails(
        key: String,
        sourceLotId: String
    ): LiveData<Resource<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<TrackTraceSourceLotDetails>>{
                return trackTraceApi.getSourceLotDetails(getCurrentKey(), sourceLotId)
            }
        }.build().asLiveData()
    }

    override suspend fun getAllSourceLotIdList(): LiveData<List<TrackTraceSourceLotDetails>> {
        return dao.getAllSourceLotIdList()
    }

    override suspend fun getAllTransIdList(): LiveData<List<TrackTraceTransactionIdDetails>> {
        return dao.getAllTransIdList()
    }

    override suspend fun getOfflineSourceLotDetails(sourceLotId: String): LiveData<TrackTraceSourceLotDetails> = dao.getOfflineSourceLotDetails(sourceLotId)

    override suspend fun getSuppliers(purChaseType: String?) = if (purChaseType?.isNotEmpty() == true) dao.getSuppliers(purChaseType.toString()) else dao.getSuppliers()
    override suspend fun getFarmerList() = dao.getFarmerList()

    override suspend fun getFarmerEudrDetails(farmerId: String) = dao.getFarmerEudrDetails(farmerId)

    override suspend fun getOnlineTransactionIdDetails(transactionId: String): LiveData<Resource<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>{
                return trackTraceApi.getTranscationIdDetails(getCurrentKey(), transactionId)
            }
        }.build().asLiveData()
    }

    override suspend fun getOfflineTransactionIdDetails(transactionId: String): LiveData<TrackTraceTransactionIdDetails> {
        return dao.getOfflineTransactionIdDetails(transactionId)
    }

}
