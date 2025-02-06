package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaGateEntryApprovalPostResponse
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaGateEntryApprovalNigeriaPost
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaGateEntryApprovalNigeriaResponse
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.usecase.VegaGateEntryApprovalNigeriaUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
class VegaGateEntryApprovalNigeriaViewModel(
    private val useCase: VegaGateEntryApprovalNigeriaUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

/*    private var gateEntryTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> = MutableLiveData()
    private val _waitingTrucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>()
    val waitingTrucks: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> get() = _waitingTrucks*/

    private var gateEntryTruckResource1: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> =
        MutableLiveData()
    private val _waitingTrucks1 =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>()
    val waitingTrucks1: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> get() = _waitingTrucks1

    private var gateEntryTruckSource: LiveData<Resource<GenericReqAndResp<VegaGateEntryApprovalPostResponse>>> =
        MutableLiveData()
    private val _gateEntryTruck =
        MediatorLiveData<Resource<GenericReqAndResp<VegaGateEntryApprovalPostResponse>>>()
    val gateEntryTruck: LiveData<Resource<GenericReqAndResp<VegaGateEntryApprovalPostResponse>>> get() = _gateEntryTruck

    private var storageLocationSource: LiveData<VegaCustomStLocation> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<VegaCustomStLocation>()
    val storageLocation: LiveData<VegaCustomStLocation> get() = _storageLocation

    private var gateEntrySource: LiveData<Resource<GenericReqAndResp<VegaGateEntryApprovalNigeriaResponse>>> =
        MutableLiveData()
    private val _gateEntry =
        MediatorLiveData<Resource<GenericReqAndResp<VegaGateEntryApprovalNigeriaResponse>>>()
    val gateEntry: LiveData<Resource<GenericReqAndResp<VegaGateEntryApprovalNigeriaResponse>>> get() = _gateEntry

    private var imageResourceWs: LiveData<Resource<GenericReqAndResp<VegaDMSImageResponse>>> =
        MutableLiveData()
    private val _imageWs =
        MediatorLiveData<Resource<GenericReqAndResp<VegaDMSImageResponse>>>()
    val imageWs: LiveData<Resource<GenericReqAndResp<VegaDMSImageResponse>>> get() = _imageWs

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

    fun getWaitingTruckList(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _waitingTrucks1.removeSource(gateEntryTruckResource1) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateEntryTruckResource1 = useCase.getWaitingTrucks1(selectedPlantId)
        }
        _waitingTrucks1.addSource(gateEntryTruckResource1) {
            _waitingTrucks1.value = it
        }
    }

    fun fetchWbDetails(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _gateEntryTruck.removeSource(gateEntryTruckSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateEntryTruckSource = useCase.fetchWbDetails(selectedPlantId)
        }
        _gateEntryTruck.addSource(gateEntryTruckSource) {
            _gateEntryTruck.value = it
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

    fun postGateEntryData(gateEntryPost: VegaGateEntryApprovalNigeriaPost) =
        viewModelScope.launch(dispatchers.main) {
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

    fun getDMSUploadedImages(wbId: String, werks: String, year: String) =
        viewModelScope.launch(dispatchers.main) {
            _imageWs.removeSource(imageResourceWs)
            withContext(dispatchers.io) {
                imageResourceWs = useCase.getDMSUploadedImages(wbId, werks, year)
            }
            _imageWs.addSource(imageResourceWs) {
                _imageWs.value = it
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

/*    fun getMultiPlantList() = viewModelScope.launch(dispatchers.main) {
        _multiPlant.removeSource(multiPlantSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            multiPlantSource = useCase.getMultiPlantList()
        }
        _multiPlant.addSource(multiPlantSource) {
            _multiPlant.value = it
        }
    }*/

}
