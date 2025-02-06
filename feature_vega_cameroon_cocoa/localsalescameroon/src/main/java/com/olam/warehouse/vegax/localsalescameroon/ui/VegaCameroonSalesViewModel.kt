package com.olam.warehouse.vegax.localsalescameroon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vegacameroon.model.VegaCameroonMtntWithBagItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCameroonSalesWithBagItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesOrderModel
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesPallet
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesPostRequest
import com.olam.warehouse.vegax.localsalescameroon.data.domain.usecase.VegaCameroonSalesDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/20/2020.
 */
class VegaCameroonSalesViewModel(
    private val useCase: VegaCameroonSalesDispatchUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {
    var dispatchWh = VegaCocoaSalesWB()
    var salesOrder = VegaCoffeeSalesOrder()
    var lots: ArrayList<VegaCoffeeSalesLots> = ArrayList()
    var data: ArrayList<VegaCameroonSalesPostRequest> = ArrayList()
    val validateLot = MutableLiveData<VegaCoffeeSalesLots>()
    var currentScanLot: String = ""
    var currentTempId: String = ""

    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    private var dispatchSalesItemSource: LiveData<VegaCoffeeSalesOrderWithLots> =
        MutableLiveData()
    private val _dispatchSalesItem = MediatorLiveData<VegaCoffeeSalesOrderWithLots>()
    val dispatchSalesItem: LiveData<VegaCoffeeSalesOrderWithLots> get() = _dispatchSalesItem

    private var dispatchSummarySalesItemSource: LiveData<VegaCoffeeSalesOrderWithLots> =
        MutableLiveData()
    private val _dispatchSummarySalesItem = MediatorLiveData<VegaCoffeeSalesOrderWithLots>()
    val dispatchSummarySalesItem: LiveData<VegaCoffeeSalesOrderWithLots> get() = _dispatchSummarySalesItem

    private var dispatchSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaCameroonSalesOrderModel>>>> =
        MutableLiveData()
    private val _dispatchSalesOrder =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonSalesOrderModel>>>>()
    val dispatchSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaCameroonSalesOrderModel>>>> get() = _dispatchSalesOrder

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> =
        MutableLiveData()
    private val _stockList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> get() = _stockList

    private var stocksSourceLocal: LiveData<List<VegaCoffeeSalesLots>> = MutableLiveData()
    private val _stocksLocal = MediatorLiveData<List<VegaCoffeeSalesLots>>()
    val stocksLocal: LiveData<List<VegaCoffeeSalesLots>> get() = _stocksLocal

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> =
        MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> get() = _lotDetails

    private var bagSource: LiveData<List<VegaCoffeeSalesBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCoffeeSalesBagMaterial>>()
    val bagItems: LiveData<List<VegaCoffeeSalesBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonSalesPallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonSalesPallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCameroonSalesPallet>>>> get() = _pallet

    private var dispatchPendingSource: LiveData<List<VegaCoffeePendingSalesOrderWithLots>> =
        MutableLiveData()
    private val _dispatchPendingSales =
        MediatorLiveData<List<VegaCoffeePendingSalesOrderWithLots>>()
    val dispatchPendingSales: LiveData<List<VegaCoffeePendingSalesOrderWithLots>> get() = _dispatchPendingSales

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaCameroonSalesPostRequest>>> =
        MutableLiveData()
    private val _deliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonSalesPostRequest>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaCameroonSalesPostRequest>>> get() = _deliveryPost

    var storageLocationSource: LiveData<List<VegaStorageLocation>> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<List<VegaStorageLocation>>()
    val storeLocation: LiveData<List<VegaStorageLocation>> get() = _storageLocation

    private var salesBagWithMaterialSource: LiveData<VegaCameroonSalesWithBagItems> =
        MutableLiveData()
    private val _salesBagWithMaterial = MediatorLiveData<VegaCameroonSalesWithBagItems>()
    val salesBagWithMaterial: LiveData<VegaCameroonSalesWithBagItems> get() = _salesBagWithMaterial


    fun getSalesPalletBag(batchNo: String) = viewModelScope.launch(dispatchers.main) {
        _salesBagWithMaterial.removeSource(salesBagWithMaterialSource)
        withContext(dispatchers.io) {
            salesBagWithMaterialSource = useCase.getOfflineSalesPalletBags(batchNo)
        }
        _salesBagWithMaterial.addSource(salesBagWithMaterialSource) {
            _salesBagWithMaterial.value = it
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

    fun getDispatchSalesItem(soNumber: String, salesType: String, salesTempId: String) =
        viewModelScope.launch(dispatchers.main) {
            _dispatchSalesItem.removeSource(dispatchSalesItemSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                dispatchSalesItemSource =
                    useCase.getDispatchSalesItem(soNumber, salesType, salesTempId)
            }
            _dispatchSalesItem.addSource(dispatchSalesItemSource) {
                _dispatchSalesItem.value = it
            }
        }


    fun getDispatchSummarySalesItem(soNumber: String, salesType: String, salesTempId: String) =
        viewModelScope.launch(dispatchers.main) {
            _dispatchSummarySalesItem.removeSource(dispatchSummarySalesItemSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                dispatchSummarySalesItemSource =
                    useCase.getDispatchSummarySalesItem(soNumber, salesType, salesTempId)
            }
            _dispatchSummarySalesItem.addSource(dispatchSummarySalesItemSource) {
                _dispatchSummarySalesItem.value = it
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

    fun saveWeighBridgeAndLotDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {

            if (salesOrder.saleOrderId.isNotEmpty()) useCase.saveDispatchAndLots(salesOrder, lots)
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
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

    fun removeLotDetails(lot: VegaCoffeeSalesLots) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLotDetails(lot)
        }
    }

    fun getBagItems(batchNumber: String, materialCode: String, salesTempId: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber, materialCode, salesTempId)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
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

    fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(bagMaterial)
        }
    }

    fun getPendingList(type: String) = viewModelScope.launch(dispatchers.main) {
        _dispatchPendingSales.removeSource(dispatchPendingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchPendingSource = useCase.getPendingListLot(type)
        }
        _dispatchPendingSales.addSource(dispatchPendingSource) {
            _dispatchPendingSales.value = it
        }
    }

    fun updateLotWeightInfo(lot: ArrayList<VegaCoffeeSalesLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateLotEditWeight(lot)
        }
    }

    fun postDeliveryDetail(vegaDeliveryPost: VegaCameroonSalesPostRequest) = viewModelScope.launch(dispatchers.main) {
        _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
        }
        _deliveryPost.addSource(deliveryPostSource) {
            _deliveryPost.value = it
        }
    }

    fun deleteTempData(salesTempId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteTempData(salesTempId)
        }
    }


    fun getStorageLocations() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getStorageLocations()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

    private var gradeSource: LiveData<List<VegaQualitative>> = mutableLiveDataOf(emptyList())
    private val _grade = MediatorLiveData<List<VegaQualitative>>()
    val grade: LiveData<List<VegaQualitative>> get() = _grade

    private var certificationSource: LiveData<List<VegaQualitative>> = mutableLiveDataOf(emptyList())
    private val _certification = MediatorLiveData<List<VegaQualitative>>()
    val certification: LiveData<List<VegaQualitative>> get() = _certification

    private var materialQualityGradeSource: LiveData<List<VegaNicaraguaMaterialQualitGrades>> =
        mutableLiveDataOf(emptyList())
    private val _materialQualitygrade = MediatorLiveData<List<VegaNicaraguaMaterialQualitGrades>>()
    val materialQualityGrades: LiveData<List<VegaNicaraguaMaterialQualitGrades>> get() = _materialQualitygrade

    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

    fun getMaterialQualityGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _materialQualitygrade.removeSource(materialQualityGradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialQualityGradeSource = useCase.getMaterialQualityGrades(materialCode)
        }
        _materialQualitygrade.addSource(materialQualityGradeSource) {
            _materialQualitygrade.value = it
        }
    }

    fun getGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _grade.removeSource(gradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gradeSource = useCase.getGrades(materialCode)
        }
        _grade.addSource(gradeSource) {
            _grade.value = it
        }
    }

    fun getCertification(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _certification.removeSource(certificationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            certificationSource = useCase.getCertification(materialCode)
        }
        _certification.addSource(certificationSource) {
            _certification.value = it
        }
    }
    fun saveSalesLots(salesLots: VegaCoffeeSalesLots)= viewModelScope.launch {
        withContext(dispatchers.io){
            useCase.saveSalesLot(salesLots)
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
