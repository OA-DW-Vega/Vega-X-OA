package com.olam.warehouse.vegax.bagissueindiacoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.*
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.usecase.VegaIndiaCoffeeBagIssueUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaIndiaCoffeeBagIssueViewModel(
    private val useCase: VegaIndiaCoffeeBagIssueUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var productSource: LiveData<List<VegaPackageMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaPackageMaterial>>()
    val product: LiveData<List<VegaPackageMaterial>> get() = _product

    private var materialSource: LiveData<List<VegaPackageMaterial>> = mutableLiveDataOf(emptyList())
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    val material:LiveData<List<VegaPackageMaterial>> get() = _material

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var currentBagIssueSource: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>> =
        MutableLiveData()
    private val _currentBagIssue = MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>>()
    val currentBagIssue: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>> get() = _currentBagIssue


    private var bagIssueSource: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>>> =
        MutableLiveData()
    private val _bagIssue = MediatorLiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>>>()
    val bagIssue: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>>> get() = _bagIssue

/*    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }*/

    fun getBagMaterial() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCase.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
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

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
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

    fun postBagIssueData(bagIssuePost: VegaIndiaCoffeeBagIssuePost) = viewModelScope.launch(dispatchers.main) {
        _bagIssue.removeSource(bagIssueSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagIssueSource = useCase.postBagIssueData(bagIssuePost)
        }
        _bagIssue.addSource(bagIssueSource) {
            _bagIssue.value = it
        }
    }
}
