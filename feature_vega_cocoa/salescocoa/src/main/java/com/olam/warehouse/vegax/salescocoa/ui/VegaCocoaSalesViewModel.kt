package com.olam.warehouse.vegax.salescocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesDeliveryDetail
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesOrderModel
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPallet
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPostRequest
import com.olam.warehouse.vegax.salescocoa.data.domain.usecase.VegaCocoaSalesDispatchUseCase
import com.olam.warehouse.vegax.salescocoa.utils.SALES_TYPE_WEIGHBRIDGE
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class VegaCocoaSalesViewModel(
    private val useCase: VegaCocoaSalesDispatchUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {
    var dispatchWh = VegaCocoaSalesWB()

    val thirdPartyMaterial = MutableLiveData<List<VegaMaterial>>()

    private var dispatchWeighBridge: LiveData<VegaCocoaSalesWB> = MutableLiveData()
    private val _dispatchWB = MediatorLiveData<VegaCocoaSalesWB>()
    val dispatchWB: LiveData<VegaCocoaSalesWB> get() = _dispatchWB

    private var dispatchSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesOrderModel>>>> =
        MutableLiveData()
    private val _dispatchSalesOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesOrderModel>>>>()
    val dispatchSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesOrderModel>>>> get() = _dispatchSalesOrder

    private var dispatchPendingSource: LiveData<List<VegaCocoaSalesWB>> = MutableLiveData()
    private val _dispatchPendingWB = MediatorLiveData<List<VegaCocoaSalesWB>>()
    val dispatchPendingWB: LiveData<List<VegaCocoaSalesWB>> get() = _dispatchPendingWB

    val dispatchLots = MutableLiveData<List<VegaCocoaSalesLots>>()

    val validateLot = MutableLiveData<VegaCocoaSalesLots>()
    val pendingList = MutableLiveData<List<VegaCocoaSalesWB>>()


    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesWB>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesWB>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesWB>>>> get() = _trucks

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesPallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesPallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesPallet>>>> get() = _pallet

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>> get() = _qualityDetails


    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> = MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> get() = _deliveryPost

    private var deliveryPostSuccessSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>>> =
        MutableLiveData()
    private val _deliverySuccessPost =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>>>()
    val deliverySuccessPost: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>>> get() = _deliverySuccessPost

    var lots: ArrayList<VegaCocoaSalesLots> = ArrayList()

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>> get() = _stockList

    fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest) = viewModelScope.launch(dispatchers.main) {
        _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
        }
        _deliveryPost.addSource(deliveryPostSource) {
            _deliveryPost.value = it
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }

    fun postAnticipatedDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _deliverySuccessPost.removeSource(deliverySuccessPost) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryPostSuccessSource = useCase.postAnticipatedDeliveryDetail(vegaDeliveryPost)
            }
            _deliverySuccessPost.addSource(deliveryPostSuccessSource) {
                _deliverySuccessPost.value = it
            }
        }

    fun getBagItems(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
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


    fun getPalletInfo(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _pallet.removeSource(palletResource)
        withContext(dispatchers.io) {
            palletResource = useCase.getPalletDetails(batchNumber, material)
        }
        _pallet.addSource(palletResource) {
            _pallet.value = it
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


    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
        }
    }

    fun saveWeighBridgeAndLotDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            if (dispatchWh.weighBridgeId.isBlank()) {
                val randomDouble = "TMP".plus(Random.nextLong().toString())
                dispatchWh.weighBridgeId = randomDouble
                dispatchWh.wbTempId = randomDouble
            }
            if (dispatchWh.saleOrderId.isNotEmpty()) useCase.saveDispatchAndLots(dispatchWh, lots)
        }
    }

    fun removeLotFromList(batchNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeLotFromTruck(batchNumber)
        }
    }

    fun getWeighBrideData(whId: String) = viewModelScope.launch(dispatchers.main) {
        _dispatchWB.removeSource(dispatchWeighBridge) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchWeighBridge = if (SALES_TYPE_WEIGHBRIDGE == dispatchWh.salesType) {
                useCase.getTruckDetails(whId)
            } else {
                useCase.getTruckDetailsForNoWieghScale(whId, dispatchWh.saleOrderId, dispatchWh.salesType)
            }
        }
        _dispatchWB.addSource(dispatchWeighBridge) {
            _dispatchWB.value = it
        }
    }

    fun getPendingList(type: String) = viewModelScope.launch(dispatchers.main) {
        _dispatchPendingWB.removeSource(dispatchPendingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchPendingSource = useCase.getPendingListLot(type)
        }
        _dispatchPendingWB.addSource(dispatchPendingSource) {
            _dispatchPendingWB.value = it
        }
    }

    fun getLotsDataFromLocal(whId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            dispatchLots.postValue(
                if (SALES_TYPE_WEIGHBRIDGE == dispatchWh.salesType)
                    useCase.getLotsDetails(whId)
                else useCase.getLotsDetailsBySalesAndType(dispatchWh.saleOrderId, dispatchWh.salesType)
            )
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun pendingList(type: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            pendingList.postValue(useCase.getPendingLot(type))
        }
    }

    fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(bagMaterial)
        }
    }

    fun updateSuccessStatus(
        wbId: VegaCocoaSalesWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaSalesLots>
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateSuccessData(wbId, syncStatus, status, msg, lots)
        }
    }

    fun updateBagSuccessStatus(batchNumber: String, bag: List<VegaCocoaSweepingBagMaterial>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateBagSuccessData(batchNumber, bag)
        }
    }

    fun updateLotWeightInfo(lot: VegaCocoaSalesLots) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateLotEditWeight(lot)
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

    fun getStockList(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stockList.removeSource(stockListSource)
        withContext(dispatchers.io) {
            stockListSource = useCase.getStockList(materialList)
        }
        _stockList.addSource(stockListSource) {
            _stockList.value = it
        }
    }
}
