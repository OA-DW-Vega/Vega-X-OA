package com.olam.warehouse.vegax.ppqindiacoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ppqindiacoffee.data.api.VegaIndiaCoffeePpqApi
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqPostResponse
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaPpqModel
import com.olam.warehouse.vegax.ppqindiacoffee.utils.prepareData
import java.util.*


interface VegaIndiaCoffeePpqRepository {
    suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeePpqInspectionLots>>>>
    suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeePpqInspectionLotDetails>>>
    suspend fun saveInspectionLotDetails(lotDetail: VegaIndiaCoffeePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeePpqPostResponse>>>
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
}

class VegaIndiaCoffeePpqRepositoryImpl(
    private val api: VegaIndiaCoffeePpqApi,
    private val dao: VegaInventoryDao,
    private val daoQ: VegaQualityDao
) : VegaIndiaCoffeePpqRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaPpqModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaPpqModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaPpqModel> =
                api.getIndiaCoffeeInventoryList(currentKey)
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

    override suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeePpqInspectionLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndiaCoffeePpqInspectionLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaIndiaCoffeePpqInspectionLots>> =
                api.getInspectionLots(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeePpqInspectionLotDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeePpqInspectionLotDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeePpqInspectionLotDetails> =
                api.getInspectionLotDetails(getCurrentKey(), lotId, PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun saveInspectionLotDetails(lotDetail: VegaIndiaCoffeePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeePpqPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeePpqPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeePpqPostResponse> =
                api.saveInspectionLotDetails(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""), lotDetail)
        }.build().asLiveData()
    }

    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

}
