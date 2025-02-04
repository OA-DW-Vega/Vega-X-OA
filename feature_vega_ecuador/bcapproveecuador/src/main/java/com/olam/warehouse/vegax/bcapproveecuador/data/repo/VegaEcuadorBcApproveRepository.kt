package com.olam.warehouse.vegax.bcapproveecuador.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.dao.VegaCoCoaOffloadDao
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorDispatchDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApprovePost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.bcapproveecuador.data.api.VegaEcuadorBcApproveOffloadingApi
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail

interface VegaEcuadorBcApproveRepository {
    suspend fun getWeighBridgeDetailOnline(plantId:String): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>>
    suspend fun postApproval(VegaEcuadorBcApprovePost: VegaEcuadorBcApprovePost): LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>>
    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>>
    }

class VegaEcuadorBcApproveRepositoryImpl(
    private val api: VegaEcuadorBcApproveOffloadingApi,
    private val dao: VegaEcuadorDispatchDao,
    private val masterDao: MasterDao
) : VegaEcuadorBcApproveRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

        override suspend fun getWeighBridgeDetailOnline(plantId:String): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey,plantId)
        }.build().asLiveData()
    }
    override suspend fun postApproval(VegaEcuadorBcApprovePost: VegaEcuadorBcApprovePost): LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorBcApproveResponse> =
                api.postApproval(VegaEcuadorBcApprovePost)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorApproveWbDetail>> =
                api.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }
  }

