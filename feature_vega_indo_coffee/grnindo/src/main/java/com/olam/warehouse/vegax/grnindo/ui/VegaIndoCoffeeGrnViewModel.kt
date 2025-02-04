package com.olam.warehouse.vegax.grnindo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.grnindo.data.domain.usecase.VegaIndoCoffeeGrnUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeGrnViewModel(
    private val useCase: VegaIndoCoffeeGrnUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> =
        MutableLiveData()
    private val _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> get() = _weighBridge

    private var weighBridgeLocalSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeLocal = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeLocal: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeLocal

    private var weighBridgeTransSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeTrans = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeTrans: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeTrans

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

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
        MutableLiveData()
    private val _poList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    private var poListOfflineSource: LiveData<List<VegaEcuadorPurchaseOrder>> =
        MutableLiveData()
    private val _poListOffline = MediatorLiveData<List<VegaEcuadorPurchaseOrder>>()
    val poListOffline: LiveData<List<VegaEcuadorPurchaseOrder>> get() = _poListOffline

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    fun getPreSamplingQualitydata(batchNo: String, materialId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }

    fun getPOList() = viewModelScope.launch(dispatchers.main) {
        _poList.removeSource(poListSource)
        withContext(dispatchers.io) {
            poListSource = useCase.getPOList()
        }
        _poList.addSource(poListSource) {
            _poList.value = it
        }
    }

    fun getPOListOffline() = viewModelScope.launch(dispatchers.main) {
        _poListOffline.removeSource(poListOfflineSource)
        withContext(dispatchers.io) {
            poListOfflineSource = useCase.getPOListOffline()
        }
        _poListOffline.addSource(poListOfflineSource) {
            _poListOffline.value = it
        }
    }

    fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {
        fetchWeighBridgeList()
        return weighBridge
    }

    private fun fetchWeighBridgeList() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.invoke()
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

    fun getWeighBridgeTransDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeTrans.removeSource(weighBridgeTransSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeTransSource = useCase.getWeighBridgeTransDetail()
        }
        _weighBridgeTrans.addSource(weighBridgeTransSource) {
            _weighBridgeTrans.value = it
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

    fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int,
        wbDetails: VegaGrnWeighBridgeId
    ) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.updateGrnSuccess(wbid, grnNo, batch, msg, status, wbDetails)
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

    fun deleteAllItem(wbTempId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteAllItem(wbTempId)
        }
    }
}

