package com.olam.warehouse.vegax.qualityecuador.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorQualitySavedResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.qualityecuador.data.domain.usecase.VegaEcuadorQualityUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaEcuadorQualityViewModel(
    private val useCase: VegaEcuadorQualityUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> = MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> get() = _quality

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private val _qualitySaved = MediatorLiveData<Resource<GenericReqAndResp<VegaEcuadorQualitySavedResponse>>>()
    private var qualitySavedSource: LiveData<Resource<GenericReqAndResp<VegaEcuadorQualitySavedResponse>>> =
        MutableLiveData()
    val qualitySaved: LiveData<Resource<GenericReqAndResp<VegaEcuadorQualitySavedResponse>>> get() = _qualitySaved


    private var qualityOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _qualityOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    val qualityOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityOfflineList

    private var weighBridgeSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<List<VegaQualityWBDetails>>()
    val weighBridge: LiveData<List<VegaQualityWBDetails>> get() = _weighBridge

    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline

    private var wbWithParamSource: LiveData<List<VegaWeighBridgeWithQualityParams>> = MutableLiveData()
    private val _wbWithParams = MediatorLiveData<List<VegaWeighBridgeWithQualityParams>>()
    val wbWithParams: LiveData<List<VegaWeighBridgeWithQualityParams>> get() = _wbWithParams

    fun postQualityParams(qualityPost: VegaQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = useCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) = viewModelScope.launch(dispatchers.main) {
        _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityListSource = useCase.getQualityParams(materialId, isValueExist, wbId)
        }
        _qualitylist.addSource(qualityListSource) {
            _qualitylist.value = it
        }
    }

    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveQualityData(qualityParameter, batchNo)
        }
    }

    fun saveWBDB(weighBridge: VegaQualityWBDetails) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveWBDB(weighBridge)
        }
    }

    fun getQualityOfflineListCount(): LiveData<List<VegaQualityWBDetails>> {
        getQualityOfflineList()
        return qualityOfflineList
    }

    private fun getQualityOfflineList() = viewModelScope.launch(dispatchers.main) {
        _qualityOfflineList.removeSource(qualityOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityOfflineSource = useCase.getQualityOfflineList()
        }
        _qualityOfflineList.addSource(qualityOfflineSource) {
            _qualityOfflineList.value = it
        }
    }

    fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.getWeighBridgeDetail()
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
            weighBridgeOnlineSource = useCase.getWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getWBWithQualityPars(weighBridgeID: String) = viewModelScope.launch(dispatchers.main) {
        _wbWithParams.removeSource(wbWithParamSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbWithParamSource = useCase.getWBWithQuality(weighBridgeID)
        }
        _wbWithParams.addSource(wbWithParamSource) {
            _wbWithParams.value = it
        }
    }

    fun updateDeletedItem(weighBridgeId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(weighBridgeId)
        }
    }

    fun fetchSavedWeighBridgeDetail(plantId: String, materialCode: String, batchNumber: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualitySaved.removeSource(qualitySavedSource)
            withContext(dispatchers.io) {
                qualitySavedSource = useCase.getSavedQuality(plantId, materialCode, batchNumber)
            }
            _qualitySaved.addSource(qualitySavedSource) {
                _qualitySaved.value = it
            }
        }
}
