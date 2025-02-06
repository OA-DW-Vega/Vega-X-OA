package com.olam.warehouse.vegax.historytransactionsghanacocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.historytransactionsghanacocoa.data.api.VegaGhanaCocoaHistoryTransactionsApi
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTranxResponse
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaHistoryTranxMtnr
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtntHistoryTranxResponse

interface VegaHistoryTransactionsGhanaCocoaRepository {

    suspend fun getHistoryTranxMtnr(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr>>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getHistoryTranxGrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse>>>

    suspend fun getHistoryTranxMtnt(
        endDate: String,
        plant: String,
        startDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse>>>
    suspend fun getuomDetail(): LiveData<List<VegaUomDetails>>
}

class VegaHistoryTransactionsGhanaCocoaRepositoryImpl(
        private val apiCocoa: VegaGhanaCocoaHistoryTransactionsApi,
    private val dao: VegaEcuadorOffloadingDao
) : VegaHistoryTransactionsGhanaCocoaRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getHistoryTranxMtnt(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse> =
                apiCocoa.fetchHistoryTranxMtnt(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxMtnr(
        endDate: String,
        plant: String,
        startDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr> =
                apiCocoa.fetchHistoryTranxMtnr(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxGrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse> =
                apiCocoa.fetchHistoryTranxGrn(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getuomDetail() = dao.getuomDetail()

}
