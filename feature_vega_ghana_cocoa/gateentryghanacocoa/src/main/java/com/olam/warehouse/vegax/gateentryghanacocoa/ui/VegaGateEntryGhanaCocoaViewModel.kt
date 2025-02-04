package com.olam.warehouse.vegax.gateentryghanacocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaWBMultiPlants
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.domain.model.TruckManagementSeasonResponse
import com.olam.warehouse.presentation.data.domain.model.TruckManagementVehicleResponse
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model.VegaGateEntryGhanaCocoaPost
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model.VegaGateEntryGhanaCocoaResponse
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.usecase.VegaGateEntryGhanaCocoaUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaGateEntryGhanaCocoaViewModel(private val cocoaUseCase: VegaGateEntryGhanaCocoaUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var gateEntryTruckResource: LiveData<Resource<List<VegaGateEntry>>> = MutableLiveData()
    private val _waitingTrucks = MediatorLiveData<Resource<List<VegaGateEntry>>>()
    val waitingTrucks: LiveData<Resource<List<VegaGateEntry>>> get() = _waitingTrucks

    private var gateEntrySourceCocoa: LiveData<Resource<GenericReqAndResp<VegaGateEntryGhanaCocoaResponse>>> = MutableLiveData()
    private val _gateEntry = MediatorLiveData<Resource<GenericReqAndResp<VegaGateEntryGhanaCocoaResponse>>>()
    val gateEntryCocoa: LiveData<Resource<GenericReqAndResp<VegaGateEntryGhanaCocoaResponse>>> get() = _gateEntry

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var supplierZoneSource: LiveData<List<VegaBcZoneMapping>> = MutableLiveData()
    private val _supplierZone = MediatorLiveData<List<VegaBcZoneMapping>>()
    val suppplierZone: LiveData<List<VegaBcZoneMapping>> get() = _supplierZone

    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> = MutableLiveData()
    private val _warehouse = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>()
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> get() = _warehouse

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation


    private var truckDetailSource: LiveData<Resource<TruckManagementVehicleResponse>> =
        MutableLiveData()
    private val _truckDetails = MediatorLiveData<Resource<TruckManagementVehicleResponse>>()
    val truckDetails: LiveData<Resource<TruckManagementVehicleResponse>> get() = _truckDetails

    private var seasonDetailSource: LiveData<Resource<TruckManagementSeasonResponse>> =
        MutableLiveData()
    private val _seasonDetails = MediatorLiveData<Resource<TruckManagementSeasonResponse>>()
    val seasonDetails: LiveData<Resource<TruckManagementSeasonResponse>> get() = _seasonDetails

    private var seasonDetailOfflineSource: LiveData<List<VehicleDetails>> =
        MutableLiveData()
    private val _seasonDetailsOffline = MediatorLiveData<List<VehicleDetails>>()
    val seasonDetailsOffline: LiveData<List<VehicleDetails>> get() = _seasonDetailsOffline

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId

    private var weightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> =
        MutableLiveData()
    private var _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _weighBridge

    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> get() = _trucks

    private var wbMultiPlantsesource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaWBMultiPlants>>>> =
        MutableLiveData()
    private val _wbMultiPlant =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaWBMultiPlants>>>>()
    val wbMultiPlant: LiveData<Resource<GenericReqAndResp<List<VegaCocoaWBMultiPlants>>>> get() = _wbMultiPlant

    private var sdWaybillSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceiving>>> =
        MutableLiveData()
    private var _sdWaybil = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeReceiving>>>()
    val sdWaybil: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceiving>>> get() = _sdWaybil


    fun getWaitingTruckList() = viewModelScope.launch(dispatchers.main) {
        _waitingTrucks.removeSource(gateEntryTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateEntryTruckResource = cocoaUseCase.getWaitingTrucks()
        }
        _waitingTrucks.addSource(gateEntryTruckResource) {
            _waitingTrucks.value = it
        }
    }

    fun postGateEntryData(gateEntryCocoaPost: VegaGateEntryGhanaCocoaPost) = viewModelScope.launch(dispatchers.main) {
        _gateEntry.removeSource(gateEntrySourceCocoa) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateEntrySourceCocoa = cocoaUseCase.postGateEntryData(gateEntryCocoaPost)
        }
        _gateEntry.addSource(gateEntrySourceCocoa) {
            _gateEntry.value = it
        }
    }

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = cocoaUseCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = cocoaUseCase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getSupplierZone(bcApprover: String) = viewModelScope.launch(dispatchers.main) {
        _supplierZone.removeSource(supplierZoneSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierZoneSource = cocoaUseCase.getSupplierZone(bcApprover)
        }
        _supplierZone.addSource(supplierZoneSource) {
            _supplierZone.value = it
        }
    }

    fun fetchWarehouseWithMtns() = viewModelScope.launch(dispatchers.main) {
        _warehouse.removeSource(warehouseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSource = cocoaUseCase.fetchWarehouseWithMtns()
        }
        _warehouse.addSource(warehouseSource) {
            _warehouse.value = it
        }
    }

    fun getProcessTypeList(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = cocoaUseCase.getProcessTypeList(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = cocoaUseCase.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getWeighBridgeIdDetail(wbid: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeId.removeSource(weighBridgeIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeIdSource = cocoaUseCase.getWeighBridgeIdDetail(wbid)
        }
        _weighBridgeId.addSource(weighBridgeIdSource) {
            _weighBridgeId.value = it
        }
    }

     fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weightBridgeSource = cocoaUseCase.getWeighBridgeDetailOnline()
        }
        _weighBridge.addSource(weightBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(dispatchTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchTruckResource = cocoaUseCase.getTrucks()
        }
        _trucks.addSource(dispatchTruckResource) {
            _trucks.value = it
        }
    }

    fun getfetchWBListforMultiPlants(isMTNT: Boolean,startDate: String,endDate: String,plantList: List<String>) = viewModelScope.launch(dispatchers.main) {
        _wbMultiPlant.removeSource(wbMultiPlantsesource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbMultiPlantsesource = cocoaUseCase.getfetchWBListforMultiPlants(isMTNT,startDate,endDate,plantList)
        }
        _wbMultiPlant.addSource(wbMultiPlantsesource) {
            _wbMultiPlant.value = it
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = cocoaUseCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }


    fun getTruckDetails(seasonId:String) = viewModelScope.launch(dispatchers.main) {
        _truckDetails.removeSource(truckDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            truckDetailSource = cocoaUseCase.getTruckMangeDetails(seasonId)
        }
        _truckDetails.addSource(truckDetailSource) {
            _truckDetails.value = it
        }
    }

    fun getSeasonDetails() = viewModelScope.launch(dispatchers.main) {
        _seasonDetails.removeSource(seasonDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            seasonDetailSource = cocoaUseCase.getSeasonDetails()
        }
        _seasonDetails.addSource(seasonDetailSource) {
            _seasonDetails.value = it
        }
    }

    fun getSeasonDetailsOffline() = viewModelScope.launch(dispatchers.main) {
        _seasonDetailsOffline.removeSource(seasonDetailOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            seasonDetailOfflineSource = cocoaUseCase.getSeasonDetailsOffline()
        }
        _seasonDetailsOffline.addSource(seasonDetailOfflineSource) {
            _seasonDetailsOffline.value = it
        }
    }

    fun getSDWaybillNumber(wbid: String) = viewModelScope.launch(dispatchers.main) {
        _sdWaybil.removeSource(sdWaybillSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            sdWaybillSource = cocoaUseCase.getSDWaybillNumber(wbid)
        }
        _sdWaybil.addSource(sdWaybillSource) {
            _sdWaybil.value = it
        }
    }

}
