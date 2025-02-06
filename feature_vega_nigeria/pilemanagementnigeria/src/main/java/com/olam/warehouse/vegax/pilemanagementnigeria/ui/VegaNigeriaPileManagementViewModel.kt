package com.olam.warehouse.vegax.pilemanagementnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.VegNigeriaPileManagementUseCase
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPilePostRequest
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPileSelectionModel
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPileSequence
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPileSuccessResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaNigeriaPileManagementViewModel(
    private val useCase: VegNigeriaPileManagementUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var lotlist = VegaCocoaDispatchLots()
    var lots: ArrayList<VegaCocoaDispatchLots> = ArrayList()
    var model = VegaCoffeeThirdPartyRequestModel()
    var currentScanLot: String = ""
    var salesOrder = VegaCoffeeSalesOrder()
    val validateLot = MutableLiveData<VegaCocoaDispatchLots>()
    var lotsList = ArrayList<VegaCocoaDispatchLots>()


    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _stockList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _stockList

    private var createPileSource: LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSequence>>> =
        MutableLiveData()
    private val _createPile =
        MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaPileSequence>>>()
    val createPile: LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSequence>>> get() = _createPile

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails

    private var postPileSource: LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>> =
        MutableLiveData()
    private val _postPile =
        MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>>()
    val postPile: LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>> get() = _postPile

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()


    fun getPackingMaterial() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCase.getPackingMaterial()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getPostPile(postPileRequest: VegaNigeriaPilePostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _postPile.removeSource(postPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                postPileSource = useCase.getPostPile(postPileRequest)
            }
            _postPile.addSource(postPileSource) {
                _postPile.value = it
            }
        }


    fun getCreatePile() = viewModelScope.launch(dispatchers.main) {
        _createPile.removeSource(createPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            createPileSource = useCase.getCreatePile()
        }
        _createPile.addSource(createPileSource) {
            _createPile.value = it
        }
    }

    fun addLoTInDB(lot: List<VegaCocoaDispatchLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLotList(lot)
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun getLotDetails(charge: String, material: List<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material, whId)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }

    fun getShiftRemarksItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCase.getShiftRemarkItems(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }


    private var stockPileSource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaPileSelectionModel>>>> =
        MutableLiveData()
    private val _stockPile =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaPileSelectionModel>>>>()
    val stockPile: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaPileSelectionModel>>>> get() = _stockPile

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getStockPiles(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stockPile.removeSource(stockPileSource)
        withContext(dispatchers.io) {
            stockPileSource = useCase.getStockPile(materialList)
        }
        _stockPile.addSource(stockPileSource) {
            _stockPile.value = it
        }
    }


    fun getStockList(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stockList.removeSource(stockListSource)
        withContext(dispatchers.io) {
            stockListSource = useCase.getStockList(materialList)
        }
        _stockList.addSource(stockListSource) {
            _stockList.value = it
        }
    }

    fun saveWeighBridgeAndLotDetails(list: MutableList<VegaCocoaDispatchLots>) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveLotInDispatch(list)
            }
        }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSuppliers()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
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

    fun removeLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLot(batchNumber)
        }
    }

    fun saveThirdPartyInfo() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertThirdPartyModel(model)
            if (lots.isNotEmpty())
                useCase.insertLotList(lots)
        }
    }

}
