package com.olam.warehouse.odquality.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.dorigin.entity.DOMaterial
import com.olam.warehouse.master.dorigin.entity.DOQuality
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.dorigin.model.DOQualityParamsWithQualitative
import com.olam.warehouse.master.dorigin.model.DOWeighBridgeWithQualityParams
import com.olam.warehouse.odquality.data.domain.model.DOQualityPost
import com.olam.warehouse.odquality.data.domain.model.DOQualityPostResponse
import com.olam.warehouse.odquality.data.domain.model.DOQualitySavedResponse
import com.olam.warehouse.odquality.data.domain.model.DOQualityWeighBridgeBagDetail
import com.olam.warehouse.odquality.data.domain.usecase.DOQualityUseCase
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 12/27/2019.
 */
class DOQualityViewModel(private val getQualityUseCase: DOQualityUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var weighBridgeOnlineSource: LiveData<Resource<List<DOQualityWBDetails>>> =
        MutableLiveData()
    private var qualitySource: LiveData<Resource<GenericReqAndResp<DOQualityPostResponse>>> = MutableLiveData()
    private var qualitySavedSource: LiveData<Resource<GenericReqAndResp<DOQualitySavedResponse>>> = MutableLiveData()
    private var qualityGetSource: LiveData<List<DOQualityParamsWithQualitative>> = MutableLiveData()

    private val _weighBridgeOnline = MediatorLiveData<Resource<List<DOQualityWBDetails>>>()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<DOQualityPostResponse>>>()
    private val _qualitySaved = MediatorLiveData<Resource<GenericReqAndResp<DOQualitySavedResponse>>>()
    private val _qualitylist = MediatorLiveData<List<DOQualityParamsWithQualitative>>()

    val weighBridgeOnline: LiveData<Resource<List<DOQualityWBDetails>>> get() = _weighBridgeOnline
    val quality: LiveData<Resource<GenericReqAndResp<DOQualityPostResponse>>> get() = _quality
    val qualitySaved: LiveData<Resource<GenericReqAndResp<DOQualitySavedResponse>>> get() = _qualitySaved
    val qualitylist: LiveData<List<DOQualityParamsWithQualitative>> get() = _qualitylist

    private var wbWithParamSource: LiveData<List<DOWeighBridgeWithQualityParams>> = MutableLiveData()
    private val _wbWithParams = MediatorLiveData<List<DOWeighBridgeWithQualityParams>>()
    val wbWithParams: LiveData<List<DOWeighBridgeWithQualityParams>> get() = _wbWithParams

    private var qualityOfflineSource: LiveData<List<DOQualityWBDetails>> = MutableLiveData()
    private val _qualityOfflineList = MediatorLiveData<List<DOQualityWBDetails>>()
    val qualityOfflineList: LiveData<List<DOQualityWBDetails>> get() = _qualityOfflineList

    private var weighBridgeSource: LiveData<List<DOQualityWBDetails>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<List<DOQualityWBDetails>>()
    val weighBridge: LiveData<List<DOQualityWBDetails>> get() = _weighBridge

    private var qualityWeighBridgeSource: LiveData<Resource<GenericReqAndResp<DOQualityWeighBridgeBagDetail>>> = MutableLiveData()
    private val _qualityWeighBridge = MediatorLiveData<Resource<GenericReqAndResp<DOQualityWeighBridgeBagDetail>>>()
    val qualityWeighBridge: LiveData<Resource<GenericReqAndResp<DOQualityWeighBridgeBagDetail>>> get() = _qualityWeighBridge

    private val _sapMaterialUsingMaterialCode = MediatorLiveData<DOMaterial>()
    val sapMaterialUsingMaterialCode: LiveData<DOMaterial> get() = _sapMaterialUsingMaterialCode
    private var sapMaterialSourceUsingMaterialCode: LiveData<DOMaterial> = MutableLiveData()

    fun getWeighBridgeDataOnline(): LiveData<Resource<List<DOQualityWBDetails>>> {
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

    fun fetchSavedWeighBridgeDetail(plantId: String, materialCode: String, batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        _qualitySaved.removeSource(qualitySavedSource)
        withContext(dispatchers.io) {
            qualitySavedSource = getQualityUseCase.getSavedQuality(plantId, materialCode, batchNumber)
        }
        _qualitySaved.addSource(qualitySavedSource) {
            _qualitySaved.value = it
        }
    }

    fun getQualityWeighBridgeBagDetail(plantId: String) = viewModelScope.launch(dispatchers.main) {
        _qualityWeighBridge.removeSource(qualityWeighBridgeSource)
        withContext(dispatchers.io) {
            qualityWeighBridgeSource = getQualityUseCase.getQualityWeighBridgeBagDetail("", plantId)
        }
        _qualityWeighBridge.addSource(qualityWeighBridgeSource) {
            _qualityWeighBridge.value = it
        }
    }

    fun postQualityParams(qualityPost: DOQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = getQualityUseCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun updateWBDB(Wbid: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { getQualityUseCase.updateWBDB(it) }
        }
    }

    fun getWBWithQuality(weighBridgeID: String): LiveData<List<DOWeighBridgeWithQualityParams>> {
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

    fun saveQualityData(qualityParameter: DOQuality, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            getQualityUseCase.saveQualityData(qualityParameter, batchNo)
        }
    }

    fun getQualityOfflineListCount(): LiveData<List<DOQualityWBDetails>> {
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

    fun getWeighBridgeData(): LiveData<List<DOQualityWBDetails>> {
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

    fun updateDeletedItem(Wbid: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { getQualityUseCase.updateDeletedItem(it) }
        }
    }

    fun updateWBMessage(msg: String?, weighBridgeId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            msg?.let { getQualityUseCase.updateWBMessage(it, weighBridgeId) }
        }
    }

    private val _sapMaterial = MediatorLiveData<List<DOMaterial>>()
    val sapMaterial: LiveData<List<DOMaterial>> get() = _sapMaterial
    private var sapMaterialSource: LiveData<List<DOMaterial>> = MutableLiveData()

    fun getSAPMaterials() = viewModelScope.launch(dispatchers.main) {
        _sapMaterial.removeSource(sapMaterialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            sapMaterialSource = getQualityUseCase.getSAPMaterials()
        }
        _sapMaterial.addSource(sapMaterialSource) {
            _sapMaterial.value = it
        }
    }

    fun getSAPMaterialUsingMaterialCode(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _sapMaterialUsingMaterialCode.removeSource(sapMaterialSourceUsingMaterialCode) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            sapMaterialSourceUsingMaterialCode = getQualityUseCase.getSAPMaterialsUsingMaterialCode(materialCode)
        }
        _sapMaterialUsingMaterialCode.addSource(sapMaterialSourceUsingMaterialCode) {
            _sapMaterialUsingMaterialCode.value = it
        }
    }
}
