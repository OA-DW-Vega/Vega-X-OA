package com.olam.warehouse.vegax.gateentrycameroon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonPost
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonResponse
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.usecase.VegaGateEntryCameroonUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
class VegaGateEntryCameroonViewModel(
    private val useCase: VegaGateEntryCameroonUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {


    private var gateEntryTruckResource1: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> = MutableLiveData()
    private val _waitingTrucks1 = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>()
    val waitingTrucks1: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> get() = _waitingTrucks1


    private var gateEntrySource: LiveData<Resource<GenericReqAndResp<VegaGateEntryCameroonResponse>>> =
        MutableLiveData()
    private val _gateEntry = MediatorLiveData<Resource<GenericReqAndResp<VegaGateEntryCameroonResponse>>>()
    val gateEntry: LiveData<Resource<GenericReqAndResp<VegaGateEntryCameroonResponse>>> get() = _gateEntry

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

    private var multiPlantSource: LiveData<List<Plant>> = MutableLiveData()
    private val _multiPlant = MediatorLiveData<List<Plant>>()
    val multiPlant: LiveData<List<Plant>> get() = _multiPlant

    private var farmerListSource: LiveData<List<VegaTrackTraceFarmerData>> = MutableLiveData()
    private val _farmerList = MediatorLiveData<List<VegaTrackTraceFarmerData>>()
    val farmerList: LiveData<List<VegaTrackTraceFarmerData>> get() = _farmerList

    fun getWaitingTruckList(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _waitingTrucks1.removeSource(gateEntryTruckResource1) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateEntryTruckResource1 = useCase.getWaitingTrucks1(selectedPlantId)
        }
        _waitingTrucks1.addSource(gateEntryTruckResource1) {
            _waitingTrucks1.value = it
        }
    }

    fun postGateEntryData(gateEntryPost: VegaGateEntryCameroonPost) = viewModelScope.launch(dispatchers.main) {
        _gateEntry.removeSource(gateEntrySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateEntrySource = useCase.postGateEntryData(gateEntryPost)
        }
        _gateEntry.addSource(gateEntrySource) {
            _gateEntry.value = it
        }
    }

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getSupplierZone(bcApprover: String) = viewModelScope.launch(dispatchers.main) {
        _supplierZone.removeSource(supplierZoneSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierZoneSource = useCase.getSupplierZone(bcApprover)
        }
        _supplierZone.addSource(supplierZoneSource) {
            _supplierZone.value = it
        }
    }

    fun fetchWarehouseWithMtns() = viewModelScope.launch(dispatchers.main) {
        _warehouse.removeSource(warehouseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSource = useCase.fetchWarehouseWithMtns()
        }
        _warehouse.addSource(warehouseSource) {
            _warehouse.value = it
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

    fun getFarmerList(){
        viewModelScope.launch(dispatchers.main) {
            _farmerList.removeSource(farmerListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                farmerListSource = useCase.getFarmerList()
            }
            _farmerList.addSource(farmerListSource) {
                _farmerList.value = it
            }
        }
    }


}
