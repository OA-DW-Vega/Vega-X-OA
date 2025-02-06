package com.olam.warehouse.vegax.qualitynigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.qualitynigeria.data.domain.usecase.VegaNigeriaQualityUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaNigeriaQualityViewModel(
    private val useCase: VegaNigeriaQualityUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> =
        MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> get() = _quality

    private var qualityNigeriaSource: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> =
        MutableLiveData()
    private val _qualityNigeria =
        MediatorLiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>>()
    val qualityNigeria: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> get() = _qualityNigeria

    private var qcweighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _qcweighBridge =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val qcweighBridge: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _qcweighBridge

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> =
        MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

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

    private var portSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _portItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val portItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _portItems

    private var vegaMaterialResource: LiveData<List<VegaMaterial>> =
        MutableLiveData()
    private val _vegaMaterials =
        MediatorLiveData<List<VegaMaterial>>()
    val vegaMaterials: LiveData<List<VegaMaterial>> get() = _vegaMaterials

    fun getVegaMaterials() {
        viewModelScope.launch(dispatchers.main) {
            _vegaMaterials.removeSource(vegaMaterialResource)
            withContext(dispatchers.io) {
                vegaMaterialResource = useCase.getVegaMaterials()
            }
            _vegaMaterials.addSource(vegaMaterialResource) {
                _vegaMaterials.value = it
            }
        }
    }

    fun postQualityParams(qualityPost: VegaQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = useCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun postQualityParamsNigeria(qualityPost: VegaQualityNigeriaPost) =
        viewModelScope.launch(dispatchers.main) {
            _qualityNigeria.removeSource(qualityNigeriaSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityNigeriaSource = useCase.postNigeriaQuality(qualityPost)
            }
            _qualityNigeria.addSource(qualityNigeriaSource) {
                _qualityNigeria.value = it
            }
        }

    fun getQCWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        fetchQcWeighBridgeList(selectedPlantId)
        return qcweighBridge
    }

    private fun fetchQcWeighBridgeList(selectedPlantId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qcweighBridge.removeSource(qcweighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qcweighBridgeSource = useCase.fetchQcWeighBridgeList(selectedPlantId)
            }
            _qcweighBridge.addSource(qcweighBridgeSource) {
                _qcweighBridge.value = it
            }
        }

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityListSource = useCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityListSource) {
                _qualitylist.value = it
            }
        }

    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
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

    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
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

    fun getPortPlantsId(role:String) = viewModelScope.launch(dispatchers.main) {
        _portItems.removeSource(portSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            portSource = useCase.getPortPlantsId(role)
        }
        _portItems.addSource(portSource) {
            _portItems.value = it
        }
    }
}
