package com.olam.warehouse.vegax.grnnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaNigeriaCocoaOffloadingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.*
import com.olam.warehouse.vegax.grnnigeria.data.domain.usecase.VegaNigeriaGrnUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaNigeriaGrnViewModel(
    private val useCase: VegaNigeriaGrnUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> =
        MutableLiveData()
    private val _weighBridge =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> get() = _weighBridge

    private var storageLocationListSource: LiveData<List<VegaStorageLocationDetail>> =
        MutableLiveData()
    private val _storageLocationList = MediatorLiveData<List<VegaStorageLocationDetail>>()
    val storageLocationList: LiveData<List<VegaStorageLocationDetail>> get() = _storageLocationList

    private var weighBridgeLocalSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeLocal = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeLocal: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeLocal

    private var weighBridgeOfflineSource: LiveData<List<VegaGrnWeighBridgeId>> = MutableLiveData()
    private val _weighBridgeOffline = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeOffline: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeOffline

    private var weighBridgeOfflineCountSource: LiveData<List<VegaGrnWeighBridgeId>> =
        MutableLiveData()
    private val _weighBridgeOfflineCount = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeOfflineCount: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeOfflineCount

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var storageLocationSource: LiveData<VegaStorageLocation> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<VegaStorageLocation>()
    val storageLocation: LiveData<VegaStorageLocation> get() = _storageLocation

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var grnSource: LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> =
        MutableLiveData()
    private val _grn = MediatorLiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>()
    val grn: LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> get() = _grn

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>> get() = _qualityDetails

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> =
        MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive

    private var currentBagIssueSource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>> =
        MutableLiveData()
    private val _currentBagIssue = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>>()
    val currentBagIssue: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>> get() = _currentBagIssue

    private var qualitySources: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>>> =
        MutableLiveData()
    private val _quality =
        MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>>> get() = _quality

    private var qualitySourceDB: LiveData<List<VegaQualityParameter>> =
        MutableLiveData()
    private val _qualityDetailsDB =
        MediatorLiveData<List<VegaQualityParameter>>()
    val qualityDetailsDB: LiveData<List<VegaQualityParameter>> get() = _qualityDetailsDB

    fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {
        fetchWeighBridgeList()
        return weighBridge
    }

    fun getQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>> {
        fetchQualityDetails(charge, material)
        return qualityDetails
    }

    fun postNigeriaCocoa(
        receivingData: VegaNigeriaCocoaOffloadingPost
    ) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCase.postNigeriaCocoa(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String) = viewModelScope.launch(dispatchers.main) {
        _currentBagIssue.removeSource(currentBagIssueSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            currentBagIssueSource = useCase.getCurrentBagsIssued(materialCode,supplierCode,storageLocation)
        }
        _currentBagIssue.addSource(currentBagIssueSource) {
            _currentBagIssue.value = it
        }
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

    private fun fetchQualityDetails(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualitySource)
            withContext(dispatchers.io) {
                qualitySource = useCase.fetchQualityDetails(charge, material)
            }
            _qualityDetails.addSource(qualitySource) {
                _qualityDetails.value = it
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

    fun getProcessTypeList(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCase.getProcessTypeList(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun postQualityParams(qualityPost: VegaCameroonQcPost) =
        viewModelScope.launch(dispatchers.main) {
            _quality.removeSource(qualitySources) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualitySources = useCase.postQuality(qualityPost)
            }
            _quality.addSource(qualitySources) {
                _quality.value = it
            }
        }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCase.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getAllProduct() = viewModelScope.launch(dispatchers.main) {
        _allProduct.removeSource(allProductSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allProductSource = useCase.getAllProducts()
        }
        _allProduct.addSource(allProductSource) {
            _allProduct.value = it
        }
    }

    fun getQualityDetailsDB(material: String, entryObligatory: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetailsDB.removeSource(qualitySourceDB) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualitySourceDB = useCase.invokeQualityDB(material, entryObligatory)
            }
            _qualityDetailsDB.addSource(qualitySourceDB) {
                _qualityDetailsDB.value = it
            }
        }

    fun getSuppliers(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier(selectedPlantId)
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
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

    fun postGrn(grnPost: VegaNigeriaGrnPost) = viewModelScope.launch(dispatchers.main) {
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

    fun getStorageLocationDetail() = viewModelScope.launch(dispatchers.main) {
        _storageLocationList.removeSource(storageLocationListSource)
        withContext(dispatchers.io) {
            storageLocationListSource = useCase.getStorageLocationDetail()
        }
        _storageLocationList.addSource(storageLocationListSource) {
            _storageLocationList.value = it
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
