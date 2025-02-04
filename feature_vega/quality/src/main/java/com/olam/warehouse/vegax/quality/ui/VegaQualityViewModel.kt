package com.olam.warehouse.vegax.quality.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.quality.data.domain.model.VegaQualityPost
import com.olam.warehouse.vegax.quality.data.domain.model.VegaQualityPostResponse
import com.olam.warehouse.vegax.quality.data.domain.usecase.VegaQualityUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaQualityViewModel(private val getQualityUseCase: VegaQualityUseCase, private val dispatchers: AppDispatchers) : BaseViewModel(){

    private var weighBridgeSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> = MutableLiveData()
    private var qualityGetSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private var qualityOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private var wbWithParamSource: LiveData<List<VegaWeighBridgeWithQualityParams>> = MutableLiveData()

    private val _weighBridge = MediatorLiveData<List<VegaQualityWBDetails>>()
    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>>()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    private val _qualityOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    private val _offlineParamList = MediatorLiveData<List<VegaQuality>>()
    private val _wbWithParams = MediatorLiveData<List<VegaWeighBridgeWithQualityParams>>()

    val weighBridge: LiveData<List<VegaQualityWBDetails>> get() = _weighBridge
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline
    val quality: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> get() = _quality
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist
    val qualityOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityOfflineList
    val offlineParamList: LiveData<List<VegaQuality>> get() = _offlineParamList
    val wbWithParams: LiveData<List<VegaWeighBridgeWithQualityParams>> get() = _wbWithParams

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation


    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality


    private var postQualityTruckDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>> =
        MutableLiveData()
    private val _postProcessQualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>>()
    val postProcessQualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>> get() = _postProcessQualityDetails


    fun getWeighBridgeData(): LiveData<List<VegaQualityWBDetails>> {
        getWeighBridgeDetail()
        return weighBridge
    }

    private fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = getQualityUseCase.getWeighBridgeDetail()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }


    fun getWeighBridgeDataOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        getWeighBridgeDetailOnline()
        return weighBridgeOnline
    }

    private fun getWeighBridgeDetailOnline() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOnlineSource = getQualityUseCase.getWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityGetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityGetSource = getQualityUseCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityGetSource) {
                _qualitylist.value = it
            }
        }

//    fun postWBWithQualityParams(wbid: String): LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> {
//        val wbWithParam = getWBWithQuality(wbid)
//        var wbDetails = VegaQualityWBDetails()
//        wbWithParam.value?.forEach {
//            it.qualityWBDetails.qualityDetails = it.quality
//            wbDetails = it.qualityWBDetails
//        }
//
//        postQualityParams(VegaQualityPost(plant = getPlantDetails(), lotDetails = listOf(wbDetails)))
//        return quality
//    }

    fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> {
        getWBWithQualityPars(weighBridgeID)
        return wbWithParams
    }

    fun getWBWithQualityPars(weighBridgeID: String) = viewModelScope.launch(dispatchers.main) {
        _wbWithParams.removeSource(wbWithParamSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbWithParamSource = getQualityUseCase.getWBWithQuality(weighBridgeID)
        }
        _wbWithParams.addSource(wbWithParamSource) {
            _wbWithParams.value = it
        }
    }

    fun postQualityParams(qualityPost:  VegaQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = getQualityUseCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            getQualityUseCase.saveQualityData(qualityParameter, batchNo)
        }
    }

    fun updateWBDB(Wbid: String?, charg: String, message: String, status: Int) =
        viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { getQualityUseCase.updateWBDB(it, charg, message, status) }
        }
        }

    fun saveWBDB(weighBridge: VegaQualityWBDetails) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            getQualityUseCase.saveWBDB(weighBridge)
        }
    }

    fun getQualityOfflineListCount(): LiveData<List<VegaQualityWBDetails>> {
        getQualityOfflineList()
        return qualityOfflineList
    }

    private fun getQualityOfflineList() = viewModelScope.launch(dispatchers.main) {
        _qualityOfflineList.removeSource(qualityOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityOfflineSource = getQualityUseCase.getQualityOfflineList()
        }
        _qualityOfflineList.addSource(qualityOfflineSource) {
            _qualityOfflineList.value = it
        }
    }

    fun updateDeletedItem(Wbid: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { getQualityUseCase.updateDeletedItem(it) }
        }
    }

    fun updateWBMessage(msg: String?, weighBridgeId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            msg?.let {
                getQualityUseCase.updateWBMessage(it, weighBridgeId)
            }
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = getQualityUseCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }


    fun getPreSamplingQualitydata(batchNo: String, materialId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = getQualityUseCase.getPreSamplingQualityList(batchNo, materialId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }


    fun getPostProcessingQualityWBDetails() = viewModelScope.launch(dispatchers.main) {
        _postProcessQualityDetails.removeSource(postQualityTruckDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postQualityTruckDetailsSource = getQualityUseCase.getPostProcessingQualityWBDetails()
        }
        _postProcessQualityDetails.addSource(postQualityTruckDetailsSource) {
            _postProcessQualityDetails.value = it
        }
    }

}
