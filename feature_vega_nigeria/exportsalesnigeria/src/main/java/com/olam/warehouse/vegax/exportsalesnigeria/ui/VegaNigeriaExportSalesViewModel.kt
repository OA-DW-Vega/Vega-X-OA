package com.olam.warehouse.vegax.exportsalesnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesOrder
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesOrderModel
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesWeighScalePallet
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaInventoryModel
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.usecase.VegaNigeriaExportSalesUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaNigeriaExportSalesViewModel(
    private val useCase: VegaNigeriaExportSalesUsecase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    var lotList: MutableList<VegaCoffeeExportSalesLots> = mutableListOf()
    var containerList: ArrayList<ContainerWithLots> = ArrayList()
    var currentOT: String = ""
    var currentContainer: String = ""
    var currentNewContainer: String = ""
    var currentOldContainer: String = ""
    var currentOTDetails = VegaCoffeeExportSalesOrder()
    val validateLot = MutableLiveData<VegaCoffeeExportSalesLots>()
    val validateContainer = MutableLiveData<VegaCoffeeExportSalesContainer>()
    var currentScanLot: String = ""

    private var dispatchSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaExportSalesOrderModel>>>> =
        MutableLiveData()
    private val _dispatchSalesOrder =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaExportSalesOrderModel>>>>()
    val dispatchSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaExportSalesOrderModel>>>> get() = _dispatchSalesOrder

    private var otContainerSource: LiveData<VegaCoffeeExportOTWithContainer> = MutableLiveData()
    private val _otContainer = MediatorLiveData<VegaCoffeeExportOTWithContainer>()
    val otContainer: LiveData<VegaCoffeeExportOTWithContainer> get() = _otContainer

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> =
        MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> get() = _stockList

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> =
        MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> get() = _lotDetails

    private var containerLotsSource: LiveData<ContainerWithLots> = MutableLiveData()
    private val _containerLots = MediatorLiveData<ContainerWithLots>()
    val containerLots: LiveData<ContainerWithLots> get() = _containerLots

    private var addedLotsSource: LiveData<List<VegaCoffeeExportSalesLots>> = MutableLiveData()
    private val _addedLots = MediatorLiveData<List<VegaCoffeeExportSalesLots>>()
    val addedLots: LiveData<List<VegaCoffeeExportSalesLots>> get() = _addedLots

    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems

    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaNigeriaInventoryModel>>> =
        MutableLiveData()
    private val _inventory = MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaInventoryModel>>>()
    val inventory: LiveData<Resource<GenericReqAndResp<VegaNigeriaInventoryModel>>> get() = _inventory

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaExportSalesWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaExportSalesWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaExportSalesWeighScalePallet>>>> get() = _pallet


    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaNigeriaExportSalesPostRequest>>> =
        MutableLiveData()
    private val _deliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaExportSalesPostRequest>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaNigeriaExportSalesPostRequest>>> get() = _deliveryPost

    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getPurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _dispatchSalesOrder.removeSource(dispatchSalesOrder) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchSalesOrder = useCase.getPurchaseOrder()
        }
        _dispatchSalesOrder.addSource(dispatchSalesOrder) {
            _dispatchSalesOrder.value = it
        }
    }

    fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
            println("Roshna => vm. saveBagDetails")

        }
    }


    fun getBagItems(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber, material)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id)
        }
    }

    fun getContainerInventory(status:String) = viewModelScope.launch(dispatchers.main) {
        _inventory.removeSource(inventorySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            inventorySource = useCase.getContainerInventory(status)
        }
        _inventory.addSource(inventorySource) {
            _inventory.value = it
        }
    }

    fun getPalletInfo(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _pallet.removeSource(palletResource)
        withContext(dispatchers.io) {
            palletResource = useCase.getPalletDetails(batchNumber, material)
        }
        _pallet.addSource(palletResource) {
            _pallet.value = it
        }
    }

    fun getOTWithContainer(otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _otContainer.removeSource(otContainerSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            otContainerSource = useCase.getOTWithContainer(otNumber)
        }
        _otContainer.addSource(otContainerSource) {
            _otContainer.value = it
        }
    }

    fun saveContainer(container: VegaCoffeeExportSalesContainer) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveContainer(container)
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

    fun getLotDetails(charge: String, materialList: List<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotDetails.removeSource(lotSource)
            withContext(dispatchers.io) {
                lotSource = useCase.getQualityParams(charge, materialList, whId)
            }
            _lotDetails.addSource(lotSource) {
                _lotDetails.value = it
            }
        }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun validateContainer(containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateContainer.postValue(useCase.validateContainer(containerNumber))
        }
    }

    fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) =
        viewModelScope.launch(dispatchers.io) {
            withContext(dispatchers.io) {
                useCase.saveLotDetails(vegaCoffeeSalesLots)
            }
        }

    fun updateLotWeightInfo(lot: ArrayList<VegaCoffeeExportSalesLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateLotEditWeight(lot)
        }
    }

    fun getContainerWithLots(containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        _containerLots.removeSource(containerLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            containerLotsSource = useCase.getContainerWithLots(containerNumber)
        }
        _containerLots.addSource(containerLotsSource) {
            _containerLots.value = it
        }
    }

    fun removeLotDetails(lot: VegaCoffeeExportSalesLots) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLotDetails(lot)
        }
    }


    fun getAddedLotList() = viewModelScope.launch(dispatchers.main) {
        _addedLots.removeSource(addedLotsSource)
        withContext(dispatchers.io) {
            addedLotsSource = useCase.getAddedLotList()
        }
        _addedLots.addSource(addedLotsSource) {
            _addedLots.value = it
        }
    }

    fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaExportSalesPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
            }
            _deliveryPost.addSource(deliveryPostSource) {
                _deliveryPost.value = it
            }
        }

    fun removeConatinerWitfLots(containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeConatinerWitfLots(containerNumber)
        }
    }


    fun updateContainerStatus(status:String,containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateContainerStatus(status, containerNumber)
        }
    }

    fun updateContainerId(oldContainerId: String, newContainerId: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.updateContainerId(oldContainerId, newContainerId)
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

    fun getAllProduct() = viewModelScope.launch(dispatchers.main) {
        _allProduct.removeSource(allProductSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allProductSource = useCase.getAllProducts()
        }
        _allProduct.addSource(allProductSource) {
            _allProduct.value = it
        }
    }

}
