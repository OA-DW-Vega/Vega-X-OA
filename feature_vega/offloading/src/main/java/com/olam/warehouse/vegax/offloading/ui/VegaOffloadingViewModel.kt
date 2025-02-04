package com.olam.warehouse.vegax.offloading.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.offloading.data.domain.model.BatchNumResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.OffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingLotQuality
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingQualityPost
import com.olam.warehouse.vegax.offloading.data.domain.usecase.VegaOffloadingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaOffloadingViewModel(private val useCase: VegaOffloadingUseCase, private val dispatchers: AppDispatchers): BaseViewModel(){


    private var offloadingTruckResource: LiveData<Resource<List<VegaOffloadingTrucks>>> = MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<List<VegaOffloadingTrucks>>>()
    val trucks: LiveData<Resource<List<VegaOffloadingTrucks>>> get() = _trucks


    private var paramSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _paramList = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val paramList: LiveData<List<VegaQualityParamsWithQualitative>> get() = _paramList

    private var qualitySource: LiveData<Resource<GenericReqAndResp<OffloadingQualityPostResponse>>> = MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<OffloadingQualityPostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<OffloadingQualityPostResponse>>> get() = _quality

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var batchSource: LiveData<Resource<GenericReqAndResp<BatchNumResponse>>> = MutableLiveData()
    private val _batch = MediatorLiveData<Resource<GenericReqAndResp<BatchNumResponse>>>()
    val batch: LiveData<Resource<GenericReqAndResp<BatchNumResponse>>> get() = _batch

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaOffloadingLotQuality>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaOffloadingLotQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaOffloadingLotQuality>>>> get() = _qualityDetails

    private var suggestLocationSource: LiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>> =
        MutableLiveData()
    private val _suggestCustonLocation = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>>()
    val suggestCustonLocation: LiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>> get() = _suggestCustonLocation


    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(offloadingTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offloadingTruckResource = useCase.getTrucks()
        }
        _trucks.addSource(offloadingTruckResource) {
            _trucks.value = it
        }
    }


    fun getOffloadingParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _paramList.removeSource(paramSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                paramSource = useCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _paramList.addSource(paramSource) {
                _paramList.value = it
            }
        }

    fun postQualityParams(qualityPost: VegaOffloadingQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = useCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }


    fun updateDB(Wbid: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { useCase.updateDB(it) }
        }
    }

    fun saveQualityData(qualityParameter: VegaOffloadingParameter, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.saveQualityData(qualityParameter, batchNo)
            }
        }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getDeliveryBatchNumber(deliveryNo: String, posnr: String) = viewModelScope.launch(dispatchers.main) {
        _batch.removeSource(batchSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            batchSource = useCase.getDeliveryBatchNumber(deliveryNo, posnr)
        }
        _batch.addSource(batchSource) {
            _batch.value = it
        }
    }

    fun getQualityParams(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }

    fun getSuggestedLocation(kor: String, origin: String, materialCode: String) =
        viewModelScope.launch(dispatchers.main) {
            _suggestCustonLocation.removeSource(suggestLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                suggestLocationSource = useCase.getSuggestedLocation(kor, origin, materialCode)
            }
            _suggestCustonLocation.addSource(suggestLocationSource) {
                _suggestCustonLocation.value = it
            }
        }


}
