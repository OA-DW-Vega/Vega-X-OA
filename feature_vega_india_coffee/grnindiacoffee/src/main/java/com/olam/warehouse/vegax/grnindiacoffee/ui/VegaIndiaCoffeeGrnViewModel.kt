package com.olam.warehouse.vegax.grnindiacoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.VegaIndiaCoffeeGrnUseCase
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaCameroonQcPost
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGRNQuality
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGrnPost
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeQualityApprovePostResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaIndiaCoffeeGrnViewModel(
    private val useCaseIndiaCoffee: VegaIndiaCoffeeGrnUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> =
        MutableLiveData()
    private val _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> get() = _weighBridge

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var weighBridgeLocalSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeLocal = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeLocal: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeLocal

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
        MutableLiveData()
    private val _poList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    private var qualitySources: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>> =
        MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>> get() = _quality

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

    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()


    private var grnSource: LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> = MutableLiveData()
    private val _grn = MediatorLiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>()
    val grn: LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> get() = _grn

    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>> get() = _qualityDetails
    fun getQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>> {
        fetchQualityDetails(charge, material)
        return qualityDetails
    }

    fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {
        fetchWeighBridgeList()
        return weighBridge
    }

    private fun fetchWeighBridgeList() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCaseIndiaCoffee.invoke()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCaseIndiaCoffee.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getPOList() = viewModelScope.launch(dispatchers.main) {
        _poList.removeSource(poListSource)
        withContext(dispatchers.io) {
            poListSource = useCaseIndiaCoffee.getPOList()
        }
        _poList.addSource(poListSource) {
            _poList.value = it
        }
    }

    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCaseIndiaCoffee.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
        }
    }

    fun postQualityParams(qualityPost: VegaCameroonQcPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySources) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySources = useCaseIndiaCoffee.postQuality(qualityPost)
        }
        _quality.addSource(qualitySources) {
            _quality.value = it
        }
    }

    fun fetchStorageLocation(locationCode: String) = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource)
        withContext(dispatchers.io) {
            storageLocationSource = useCaseIndiaCoffee.getStorageLocation(locationCode)
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

    fun postGrn(grnPost: VegaIndiaCoffeeGrnPost) = viewModelScope.launch(dispatchers.main) {
        _grn.removeSource(grnSource)
        withContext(dispatchers.io) {
            grnSource = useCaseIndiaCoffee.postGrn(grnPost)
        }
        _grn.addSource(grnSource) {
            _grn.value = it
        }
    }

    fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeLocal.removeSource(weighBridgeLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeLocalSource = useCaseIndiaCoffee.getWeighBridgeDetail()
        }
        _weighBridgeLocal.addSource(weighBridgeLocalSource) {
            _weighBridgeLocal.value = it
        }
    }

    fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseIndiaCoffee.updateGRNPrice(wbDetails)
        }
    }

    fun updateDeletedItem(weighBridgeId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseIndiaCoffee.updateDeletedItem(weighBridgeId)
        }
    }

    fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCaseIndiaCoffee.updateGrnSuccess(wbid, grnNo, batch, msg, status)
            }
        }

    fun getOfflineWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOffline.removeSource(weighBridgeOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOfflineSource = useCaseIndiaCoffee.getOfflineWeighBridgeDetail()
        }
        _weighBridgeOffline.addSource(weighBridgeOfflineSource) {
            _weighBridgeOffline.value = it
        }
    }

    fun getOfflineWeighBridgeDetailCount() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOfflineCount.removeSource(weighBridgeOfflineCountSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOfflineCountSource = useCaseIndiaCoffee.getOfflineWeighBridgeDetailCount()
        }
        _weighBridgeOfflineCount.addSource(weighBridgeOfflineCountSource) {
            _weighBridgeOfflineCount.value = it
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

    fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseIndiaCoffee.updateGrnNoToQuality(wbid, grnNo, batchNo)
        }
    }

    private fun fetchQualityDetails(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualitySource)
            withContext(dispatchers.io) {
                qualitySource = useCaseIndiaCoffee.invokeQuality(charge, material)
            }
            _qualityDetails.addSource(qualitySource) {
                _qualityDetails.value = it
            }
        }
}
