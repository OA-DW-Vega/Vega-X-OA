package com.olam.warehouse.vegax.offloadingindiacoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.IndiaCoffeeBatchNumResponse
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.IndiaCoffeeOffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.VegaIndiaCoffeeOffloadingLotQuality
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.VegaIndiaCoffeeOffloadingQualityPost
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.usecase.VegaIndiaCoffeeOffloadingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaOffloadingViewModel(
    private val useCaseIndiaCoffee: VegaIndiaCoffeeOffloadingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {


    private var offloadingTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> = MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> get() = _trucks


    private var paramSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _paramList = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val paramList: LiveData<List<VegaQualityParamsWithQualitative>> get() = _paramList

    private var qualitySource: LiveData<Resource<GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>>> =
        MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>>> get() = _quality

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var batchSource: LiveData<Resource<GenericReqAndResp<IndiaCoffeeBatchNumResponse>>> = MutableLiveData()
    private val _batch = MediatorLiveData<Resource<GenericReqAndResp<IndiaCoffeeBatchNumResponse>>>()
    val batch: LiveData<Resource<GenericReqAndResp<IndiaCoffeeBatchNumResponse>>> get() = _batch

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _lots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockLots: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lots

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>>>> get() = _qualityDetails

    private var suggestLocationSource: LiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>> =
        MutableLiveData()
    private val _suggestCustonLocation = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>>()
    val suggestCustonLocation: LiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>> get() = _suggestCustonLocation

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCaseIndiaCoffee.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getStockList(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _lots.removeSource(lotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotSource = useCaseIndiaCoffee.getStocks(material)
        }
        _lots.addSource(lotSource) {
            _lots.value = it
        }
    }

    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(offloadingTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offloadingTruckResource = useCaseIndiaCoffee.getTrucks()
        }
        _trucks.addSource(offloadingTruckResource) {
            _trucks.value = it
        }
    }


    fun getOffloadingParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _paramList.removeSource(paramSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                paramSource = useCaseIndiaCoffee.getQualityParams(materialId, isValueExist, wbId)
            }
            _paramList.addSource(paramSource) {
                _paramList.value = it
            }
        }

    fun postQualityParams(qualityPost: VegaIndiaCoffeeOffloadingQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = useCaseIndiaCoffee.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }


    fun updateDB(Wbid: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { useCaseIndiaCoffee.updateDB(it) }
        }
    }

    fun saveQualityData(qualityParameter: VegaOffloadingParameter, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCaseIndiaCoffee.saveQualityData(qualityParameter, batchNo)
            }
        }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCaseIndiaCoffee.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getDeliveryBatchNumber(deliveryNo: String, posnr: String) = viewModelScope.launch(dispatchers.main) {
        _batch.removeSource(batchSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            batchSource = useCaseIndiaCoffee.getDeliveryBatchNumber(deliveryNo, posnr)
        }
        _batch.addSource(batchSource) {
            _batch.value = it
        }
    }

    fun getQualityParams(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCaseIndiaCoffee.getQualityParams(charge, material)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }

    fun getSuggestedLocation(kor: String, origin: String, materialCode: String) =
        viewModelScope.launch(dispatchers.main) {
            _suggestCustonLocation.removeSource(suggestLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                suggestLocationSource = useCaseIndiaCoffee.getSuggestedLocation(kor, origin, materialCode)
            }
            _suggestCustonLocation.addSource(suggestLocationSource) {
                _suggestCustonLocation.value = it
            }
        }


}
