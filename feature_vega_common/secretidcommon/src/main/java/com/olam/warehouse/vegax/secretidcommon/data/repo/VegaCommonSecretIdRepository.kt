package com.olam.warehouse.vegax.secretidcommon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaProcessingDao
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeRminDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.secretidcommon.data.api.VegaCommonSecretIdApi
import com.olam.warehouse.vegax.secretidcommon.data.domain.model.VegaSecretIdResponse
import java.util.ArrayList

interface VegaCommonSecretIdRepository {

    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getStockByPlant(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
  //  suspend fun saveLots(secretLotList: MutableList<VegaCoffeeRminLots>)
  suspend fun getValidLots(
      charge: String,
      material: String, whId: String
  ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getSecretId(
        batchNo: String,
        isSecretIdExists: Boolean,
        materialCode: String,
        plantFk:String
    ): LiveData<Resource<GenericReqAndResp<List<VegaSecretIdResponse>>>>


}

class VegaCommonSecretIdRepoImpl(
    private val api: VegaCommonSecretIdApi,
    private val dao: VegaProcessingDao,
    private val masterDao: MasterDao
) : VegaCommonSecretIdRepository {


    override suspend fun getStockByPlant(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getAllStockByPlant(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }


  //  override suspend fun saveLots(secretLotList: MutableList<VegaCoffeeRminLots>) = dao.saveL

    override suspend fun getValidLots(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getValidLotId(getCurrentKey(), charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getSecretId(
        batchNo: String,
        isSecretIdExists: Boolean,
        materialCode: String,
        plantFk:String
    ): LiveData<Resource<GenericReqAndResp<List<VegaSecretIdResponse>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaSecretIdResponse>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaSecretIdResponse>> =
                api.getSecretId(batchNo,isSecretIdExists,materialCode,plantFk)

        }.build().asLiveData()
    }


}
