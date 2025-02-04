package com.olam.warehouse.vegax.weighment.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeMtntPost
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeMtntResponse
import com.olam.warehouse.vegax.weighment.data.domain.usecase.VegaIndiaCoffeeMtntUseCase
import com.olam.warehouse.vegax.weighment.utils.getLineItemFromMtnt
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaIndiaCoffeeMtntViewModel(
    private val useCaseIndiaCoffee: VegaIndiaCoffeeMtntUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>> get() = _weighBridge

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var indiaCoffeeMtntSource: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeMtntResponse>>> =
        MutableLiveData()
    private val _mtnt = MediatorLiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeMtntResponse>>>()
    val indiaCoffeeMtnt: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeMtntResponse>>> get() = _mtnt

    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> = MutableLiveData()
    private val _purchaseOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> get() = _purchaseOrder

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCaseIndiaCoffee.getWeighBridgeDetail()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
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

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCaseIndiaCoffee.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCaseIndiaCoffee.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun postMtntData(
        indiaCoffeeMtntData: VegaIndiaCoffeeMtntPost
    ) = viewModelScope.launch(dispatchers.main) {
        _mtnt.removeSource(indiaCoffeeMtntSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            indiaCoffeeMtntSource = useCaseIndiaCoffee.postMtntData(indiaCoffeeMtntData)
        }
        _mtnt.addSource(indiaCoffeeMtntSource) {
            _mtnt.value = it
        }
    }

    fun saveMtnt(mtntData: VegaMtnt) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseIndiaCoffee.saveMtnt(mtntData)
        }
    }

    fun saveMtntLineItems(postData: MutableList<VegaMtnt>) = viewModelScope.launch(dispatchers.main) {
        val lineItems = getLineItemFromMtnt(postData)
        withContext(dispatchers.io) {
            useCaseIndiaCoffee.saveMtntLineItems(lineItems)
        }
    }

    fun getPurchaseOrder(plantId: String) = viewModelScope.launch(dispatchers.main) {
        _purchaseOrder.removeSource(purchaseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSource = useCaseIndiaCoffee.getPurchaseOrder(plantId)
        }
        _purchaseOrder.addSource(purchaseSource) {
            _purchaseOrder.value = it
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

}
