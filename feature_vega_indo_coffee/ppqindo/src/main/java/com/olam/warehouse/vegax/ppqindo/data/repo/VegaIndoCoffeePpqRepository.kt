package com.olam.warehouse.vegax.ppqindo.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ppqindo.data.api.VegaIndoCoffeePpqApi
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqPostResponse

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
interface VegaIndoCoffeePpqRepository {
    suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>>>
    suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails>>>
    suspend fun saveInspectionLotDetails(lotDetail: VegaIndoCoffeePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqPostResponse>>>
}

class VegaIndoCoffeePpqRepositoryImpl(
    private val api: VegaIndoCoffeePpqApi
) : VegaIndoCoffeePpqRepository {

    override suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>> =
                api.getInspectionLots(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails> =
                api.getInspectionLotDetails(getCurrentKey(), lotId, PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun saveInspectionLotDetails(lotDetail: VegaIndoCoffeePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndoCoffeePpqPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndoCoffeePpqPostResponse> =
                api.saveInspectionLotDetails(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""), lotDetail)
        }.build().asLiveData()
    }

}
