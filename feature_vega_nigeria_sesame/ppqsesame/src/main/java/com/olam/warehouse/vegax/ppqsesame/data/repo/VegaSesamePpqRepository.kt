package com.olam.warehouse.vegax.ppqsesame.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ppqsesame.data.api.VegaSesamePpqApi
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaPpqModel
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLots
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqPostResponse
import com.olam.warehouse.vegax.ppqsesame.utils.prepareData
import java.util.*

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaSesamePpqRepository {
    suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaSesamePpqInspectionLots>>>>
    suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaSesamePpqInspectionLotDetails>>>
    suspend fun saveInspectionLotDetails(lotDetail: VegaSesamePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaSesamePpqPostResponse>>>
    suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaPpqModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getStoreLocations(): LiveData<List<VegaStorageLocation>>

}

class VegaSesamePpqRepositoryImpl(
    private val api: VegaSesamePpqApi,
    private val dao: VegaInventoryDao,
    private val daoQ: VegaQualityDao,
    private val daoNic: VegaNicaraguaGrnDao
) : VegaSesamePpqRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaPpqModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaPpqModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaPpqModel> =
                api.getSesameInventoryList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun getCustomLocations() = daoQ.getCustomLocations()

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(daoQ.getQualityParameterWithData(materialId, wbId)) else
            daoQ.getQualityParameter(materialId)
    }

    override suspend fun getProducts() = dao.getProducts()

    override suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaSesamePpqInspectionLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaSesamePpqInspectionLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaSesamePpqInspectionLots>> =
                api.getInspectionLots(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaSesamePpqInspectionLotDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaSesamePpqInspectionLotDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaSesamePpqInspectionLotDetails> =
                api.getInspectionLotDetails(getCurrentKey(), lotId, PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun saveInspectionLotDetails(lotDetail: VegaSesamePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaSesamePpqPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaSesamePpqPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaSesamePpqPostResponse> =
                api.saveInspectionLotDetails(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""), lotDetail)
        }.build().asLiveData()
    }

    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

    override suspend fun getStoreLocations() = daoNic.getStorageLocations(getPlantDetails().plantId)

}
