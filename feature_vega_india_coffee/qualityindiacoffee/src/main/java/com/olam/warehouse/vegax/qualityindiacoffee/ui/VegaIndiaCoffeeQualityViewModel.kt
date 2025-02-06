package com.olam.warehouse.vegax.qualityindiacoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPost
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPostResponse
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.usecase.VegaIndiaCoffeeQualityUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaIndiaCoffeeQualityViewModel(
    private val getIndiaCoffeeQualityUseCase: VegaIndiaCoffeeQualityUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var weighBridgeSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>> =
        MutableLiveData()
    private var qualityGetSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private var qualityOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private var wbWithParamSource: LiveData<List<VegaWeighBridgeWithQualityParams>> = MutableLiveData()

    private val _weighBridge = MediatorLiveData<List<VegaQualityWBDetails>>()
    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>>()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    private val _qualityOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    private val _offlineParamList = MediatorLiveData<List<VegaQuality>>()
    private val _wbWithParams = MediatorLiveData<List<VegaWeighBridgeWithQualityParams>>()

    val weighBridge: LiveData<List<VegaQualityWBDetails>> get() = _weighBridge
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline
    val quality: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>> get() = _quality
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist
    val qualityOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityOfflineList
    val offlineParamList: LiveData<List<VegaQuality>> get() = _offlineParamList
    val wbWithParams: LiveData<List<VegaWeighBridgeWithQualityParams>> get() = _wbWithParams

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> get() = _stocks

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId

    private val _warehouse = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>()
    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> =
            MutableLiveData()
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> get() = _warehouse

    private var postQualityTruckDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>> =
        MutableLiveData()
    private val _postProcessQualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>>()
    val postProcessQualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>> get() = _postProcessQualityDetails


    fun getWeighBridgeData(): LiveData<List<VegaQualityWBDetails>> {
        getWeighBridgeDetail()
        return weighBridge
    }

    fun updateDB(wbId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            wbId?.let { getIndiaCoffeeQualityUseCase.updateDB(it) }
        }
    }

    private fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = getIndiaCoffeeQualityUseCase.getWeighBridgeDetail()
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
            weighBridgeOnlineSource = getIndiaCoffeeQualityUseCase.getWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityGetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityGetSource = getIndiaCoffeeQualityUseCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityGetSource) {
                _qualitylist.value = it
            }
        }

//    fun postWBWithQualityParams(wbid: String): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>> {
//        val wbWithParam = getWBWithQuality(wbid)
//        var wbDetails = VegaQualityWBDetails()
//        wbWithParam.value?.forEach {
//            it.qualityWBDetails.qualityDetails = it.quality
//            wbDetails = it.qualityWBDetails
//        }
//
//        postQualityParams(VegaIndiaCoffeeQualityPost(plant = getPlantDetails(), lotDetails = listOf(wbDetails)))
//        return quality
//    }

    fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> {
        getWBWithQualityPars(weighBridgeID)
        return wbWithParams
    }

    fun getWBWithQualityPars(weighBridgeID: String) = viewModelScope.launch(dispatchers.main) {
        _wbWithParams.removeSource(wbWithParamSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbWithParamSource = getIndiaCoffeeQualityUseCase.getWBWithQuality(weighBridgeID)
        }
        _wbWithParams.addSource(wbWithParamSource) {
            _wbWithParams.value = it
        }
    }

    fun postQualityParams(qualityPost: VegaIndiaCoffeeQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = getIndiaCoffeeQualityUseCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            getIndiaCoffeeQualityUseCase.saveQualityData(qualityParameter, batchNo)
        }
    }

    fun updateWBDB(wbId: String?, charg: String, message: String, status: Int) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                wbId?.let { getIndiaCoffeeQualityUseCase.updateWBDB(it, charg, message, status) }
            }
        }

    fun saveWBDB(weighBridge: VegaQualityWBDetails) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            getIndiaCoffeeQualityUseCase.saveWBDB(weighBridge)
        }
    }

    fun getQualityOfflineListCount(): LiveData<List<VegaQualityWBDetails>> {
        getQualityOfflineList()
        return qualityOfflineList
    }

    private fun getQualityOfflineList() = viewModelScope.launch(dispatchers.main) {
        _qualityOfflineList.removeSource(qualityOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityOfflineSource = getIndiaCoffeeQualityUseCase.getQualityOfflineList()
        }
        _qualityOfflineList.addSource(qualityOfflineSource) {
            _qualityOfflineList.value = it
        }
    }

    fun updateDeletedItem(wbId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            wbId?.let { getIndiaCoffeeQualityUseCase.updateDeletedItem(it) }
        }
    }

    fun updateWBMessage(msg: String?, weighBridgeId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            msg?.let {
                getIndiaCoffeeQualityUseCase.updateWBMessage(it, weighBridgeId)
            }
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = getIndiaCoffeeQualityUseCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }


    fun getPreSamplingQualitydata(batchNo: String, materialId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = getIndiaCoffeeQualityUseCase.getPreSamplingQualityList(batchNo, materialId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }

    fun getWeighBridgeIdDetail(wbid: String, isWeighScale: Boolean) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeId.removeSource(weighBridgeIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeIdSource = getIndiaCoffeeQualityUseCase.getWeighBridgeIdDetail(wbid, isWeighScale)
        }
        _weighBridgeId.addSource(weighBridgeIdSource) {
            _weighBridgeId.value = it
        }
    }

    fun fetchWarehouseWithMtns() = viewModelScope.launch(dispatchers.main) {
        _warehouse.removeSource(warehouseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSource = getIndiaCoffeeQualityUseCase.fetchWarehouseWithMtns()
        }
        _warehouse.addSource(warehouseSource) {
            _warehouse.value = it
        }
    }

    fun getPostProcessingQualityWBDetails() = viewModelScope.launch(dispatchers.main) {
        _postProcessQualityDetails.removeSource(postQualityTruckDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postQualityTruckDetailsSource = getIndiaCoffeeQualityUseCase.getPostProcessingQualityWBDetails()
        }
        _postProcessQualityDetails.addSource(postQualityTruckDetailsSource) {
            _postProcessQualityDetails.value = it
        }
    }

    fun fetchStocks(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = getIndiaCoffeeQualityUseCase.getStocks(material)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = getIndiaCoffeeQualityUseCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
        }
    }


}
