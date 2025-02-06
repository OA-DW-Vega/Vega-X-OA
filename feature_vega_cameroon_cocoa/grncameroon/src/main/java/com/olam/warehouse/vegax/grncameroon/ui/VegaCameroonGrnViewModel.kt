package com.olam.warehouse.vegax.grncameroon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.grncameroon.data.domain.usecase.VegaCameroonGrnUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaCameroonGrnViewModel(
    private val useCase: VegaCameroonGrnUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> =
        MutableLiveData()
    private val _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> get() = _weighBridge

    private var weighBridgeLocalSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeLocal = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeLocal: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeLocal

    private var weighBridgeOfflineSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeOffline = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeOffline: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeOffline

    private var weighBridgeOfflineCountSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeOfflineCount = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeOfflineCount: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeOfflineCount

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var storageLocationSource: LiveData<VegaStorageLocation> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<VegaStorageLocation>()
    val storageLocation: LiveData<VegaStorageLocation> get() = _storageLocation

    private var grnSource: LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> = MutableLiveData()
    private val _grn = MediatorLiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>()
    val grn: LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> get() = _grn

    fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {
        fetchWeighBridgeList()
        return weighBridge
    }

    private fun fetchWeighBridgeList() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.fetchWeighBridgeList()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
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

    fun fetchStorageLocation(locationCode: String) = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getStorageLocation(locationCode)
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

    fun postGrn(grnPost: VegaEcuadorGrnPost) = viewModelScope.launch(dispatchers.main) {
        _grn.removeSource(grnSource)
        withContext(dispatchers.io) {
            grnSource = useCase.postGrn(grnPost)
        }
        _grn.addSource(grnSource) {
            _grn.value = it
        }
    }

    fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeLocal.removeSource(weighBridgeLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeLocalSource = useCase.getWeighBridgeDetail()
        }
        _weighBridgeLocal.addSource(weighBridgeLocalSource) {
            _weighBridgeLocal.value = it
        }
    }

    fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateGRNPrice(wbDetails)
        }
    }

    fun updateDeletedItem(weighBridgeId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(weighBridgeId)
        }
    }

    fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.updateGrnSuccess(wbid, grnNo, batch, msg, status)
            }
        }

    fun getOfflineWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOffline.removeSource(weighBridgeOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOfflineSource = useCase.getOfflineWeighBridgeDetail()
        }
        _weighBridgeOffline.addSource(weighBridgeOfflineSource) {
            _weighBridgeOffline.value = it
        }
    }

    fun getOfflineWeighBridgeDetailCount() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOfflineCount.removeSource(weighBridgeOfflineCountSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOfflineCountSource = useCase.getOfflineWeighBridgeDetailCount()
        }
        _weighBridgeOfflineCount.addSource(weighBridgeOfflineCountSource) {
            _weighBridgeOfflineCount.value = it
        }
    }

    fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateGrnNoToQuality(wbid, grnNo, batchNo)
        }
    }
}
