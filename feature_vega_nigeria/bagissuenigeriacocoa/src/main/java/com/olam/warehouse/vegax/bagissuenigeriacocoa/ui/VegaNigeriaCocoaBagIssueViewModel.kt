package com.olam.warehouse.vegax.bagissuenigeriacocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaPlanRoute
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssuePost
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagManagementResp
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaCurrentBagsIssued
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.usecase.VegaNigeriaCocoaBagIssueUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaNigeriaCocoaBagIssueViewModel (
    private val useCase: VegaNigeriaCocoaBagIssueUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var productSource: LiveData<List<VegaPackageMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaPackageMaterial>>()
    val product: LiveData<List<VegaPackageMaterial>> get() = _product


    private var allPlantRouteSource: LiveData<List<VegaPlanRoute>> = mutableLiveDataOf()
    private val _allPlantRoute = MediatorLiveData<List<VegaPlanRoute>>()
    val allPlantRoute: LiveData<List<VegaPlanRoute>> get() = _allPlantRoute

    private var materialSource: LiveData<List<VegaPackageMaterial>> = mutableLiveDataOf(emptyList())
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var currentBagIssueSource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>>> =
        MutableLiveData()
    private val _currentBagIssue = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>>>()
    val currentBagIssue: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>>> get() = _currentBagIssue


    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> get() = _stocks

    private var bagIssueSource: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>>> =
        MutableLiveData()
    private val _bagIssue = MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>>>()
    val bagIssue: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>>> get() = _bagIssue

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

    fun getSuppliers(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier(selectedPlantId)
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

   fun getVendors() = viewModelScope.launch(dispatchers.main) {
       _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
       withContext(dispatchers.io) {
           supplierSource = useCase.getVendors()
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

    fun postBagIssueData(bagIssuePost: VegaNigeriaCocoaBagIssuePost) = viewModelScope.launch(dispatchers.main) {
        _bagIssue.removeSource(bagIssueSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagIssueSource = useCase.postBagIssueData(bagIssuePost)
        }
        _bagIssue.addSource(bagIssueSource) {
            _bagIssue.value = it
        }
    }


    fun fetchStocks(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCase.getStocks(material)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }


}
