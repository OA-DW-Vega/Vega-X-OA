package com.olam.warehouse.vegax.localsalesecuador.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesOrderModel
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesPallet
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesPostRequest
import com.olam.warehouse.vegax.localsalesecuador.data.domain.usecase.VegaEcuadorCocoaSalesDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/20/2020.
 */
class VegaEcuadorCocoaSalesViewModel(
    private val useCase: VegaEcuadorCocoaSalesDispatchUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {
    var salesOrder = VegaCoffeeSalesOrder()
    var lots: ArrayList<VegaCoffeeSalesLots> = ArrayList()
    val validateLot = MutableLiveData<VegaCoffeeSalesLots>()
    val validateWeighbridgeLot = MutableLiveData<VegaCocoaDispatchLots>()
    var currentScanLot: String = ""
    var currentTempId: String = ""
    var dispatchWh = VegaCocoaDispatchWB()
    val thirdPartyMaterial = MutableLiveData<List<VegaMaterial>>()
    var weighBridgelots: ArrayList<VegaCocoaDispatchLots> = ArrayList()
    val vendorDetails = MutableLiveData<VegaVendor>()

    private var dispatchSalesItemSource: LiveData<VegaCoffeeSalesOrderWithLots> =
        MutableLiveData()
    private val _dispatchSalesItem = MediatorLiveData<VegaCoffeeSalesOrderWithLots>()
    val dispatchSalesItem: LiveData<VegaCoffeeSalesOrderWithLots> get() = _dispatchSalesItem

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var productSource: LiveData<VegaMaterial> = mutableLiveDataOf()
    private val _product = MediatorLiveData<VegaMaterial>()
    val product: LiveData<VegaMaterial> get() = _product

    private var dispatchWeighBridge: LiveData<VegaCocoaDispatchWB> = MutableLiveData()
    private val _dispatchWB = MediatorLiveData<VegaCocoaDispatchWB>()
    val dispatchWB: LiveData<VegaCocoaDispatchWB> get() = _dispatchWB

    private var dispatchLotsSource: LiveData<List<VegaCocoaDispatchLots>> = MutableLiveData()
    private val _dispatchLots = MediatorLiveData<List<VegaCocoaDispatchLots>>()
    val dispatchLots: LiveData<List<VegaCocoaDispatchLots>> get() = _dispatchLots

    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> = MutableLiveData()
    private val _purchaseOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> get() = _purchaseOrder

    private var dispatchSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>>> =
        MutableLiveData()
    private val _dispatchSalesOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>>>()
    val dispatchSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>>> get() = _dispatchSalesOrder

    private var dispatchWBSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> =
        MutableLiveData()
    private val _dispatchWBSalesOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>>()
    val dispatchWBSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> get() = _dispatchWBSalesOrder

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> get() = _stockList

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

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

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesPallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesPallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesPallet>>>> get() = _pallet

    private var dispatchPendingSource: LiveData<List<VegaCoffeePendingSalesOrderWithLots>> = MutableLiveData()
    private val _dispatchPendingSales = MediatorLiveData<List<VegaCoffeePendingSalesOrderWithLots>>()
    val dispatchPendingSales: LiveData<List<VegaCoffeePendingSalesOrderWithLots>> get() = _dispatchPendingSales

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>> =
        MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>> get() = _deliveryPost

    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> get() = _trucks

    fun getPurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _dispatchSalesOrder.removeSource(dispatchSalesOrder) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchSalesOrder = useCase.getPurchaseOrder()
        }
        _dispatchSalesOrder.addSource(dispatchSalesOrder) {
            _dispatchSalesOrder.value = it
        }
    }

    fun getWBPurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _dispatchWBSalesOrder.removeSource(dispatchWBSalesOrder) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchWBSalesOrder = useCase.getWBPurchaseOrder()
        }
        _dispatchWBSalesOrder.addSource(dispatchWBSalesOrder) {
            _dispatchWBSalesOrder.value = it
        }
    }

    fun getDispatchSalesItem(soNumber: String, salesType: String, salesTempId: String) =
        viewModelScope.launch(dispatchers.main) {
            _dispatchSalesItem.removeSource(dispatchSalesItemSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                dispatchSalesItemSource = useCase.getDispatchSalesItem(soNumber, salesType, salesTempId)
            }
            _dispatchSalesItem.addSource(dispatchSalesItemSource) {
                _dispatchSalesItem.value = it
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
            /* if (salesOrder.salesTempId.isBlank()) {
                 val randomDouble = "TMP".plus(Random.nextLong().toString())
                 salesOrder.salesTempId = randomDouble
             }*/
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

    fun getBagItems(batchNumber: String, materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber, materialCode)
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

    fun validateWeighbridgeLot(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateWeighbridgeLot.postValue(useCase.validateWBLot(batchNumber, material))
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

    fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeSalesPostRequest) = viewModelScope.launch(dispatchers.main) {
        _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
        }
        _deliveryPost.addSource(deliveryPostSource) {
            _deliveryPost.value = it
        }
    }

    fun removeLotFromList(batchNumber: String, material: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeLotFromTruck(batchNumber, material)
        }
    }

    fun deleteTempData(salesTempId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteTempData(salesTempId)
        }
    }

    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(dispatchTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchTruckResource = useCase.getTrucks()
        }
        _trucks.addSource(dispatchTruckResource) {
            _trucks.value = it
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

    fun getWeighBrideData(whId: String) = viewModelScope.launch(dispatchers.main) {
        _dispatchWB.removeSource(dispatchWeighBridge) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchWeighBridge = useCase.getTruckDetails(whId)
        }
        _dispatchWB.addSource(dispatchWeighBridge) {
            _dispatchWB.value = it
        }
    }

    fun getLotsDataFromLocal(whId: String) = viewModelScope.launch(dispatchers.main) {
        _dispatchLots.removeSource(dispatchLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchLotsSource = useCase.getLotsDetails(whId)
        }
        _dispatchLots.addSource(dispatchLotsSource) {
            _dispatchLots.value = it
        }
    }

    fun getLotDetails(charge: String, material: String, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material, whId)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }

    fun deleteLotsData(whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteTruckAndLots(whId)
        }
    }

    fun getVendorInfo(vendor: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            vendorDetails.postValue(useCase.getVendorInfo(vendor))
        }
    }

    fun getProduct(code: String) = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts(code)
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun addLoTInDB(lot: VegaCocoaDispatchLots) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLot(dispatchWh.weighBridgeId, lot)
        }
    }


    fun getPurchaseOrder(receivingWerks: String) = viewModelScope.launch(dispatchers.main) {
        _purchaseOrder.removeSource(purchaseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSource = useCase.getWeighbridgePurchaseOrder(receivingWerks)
        }
        _purchaseOrder.addSource(purchaseSource) {
            _purchaseOrder.value = it
        }
    }

    fun getPreSamplingQualitydata(batchNo: String, materialId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }

    fun updateSuccessStatus(
        wbId: VegaCocoaDispatchWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaDispatchLots>
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateSuccessData(wbId, syncStatus, status, msg, lots)
        }
    }

    fun deleteLot(lots: java.util.ArrayList<VegaCoffeeSalesLots>) {

    }
}
