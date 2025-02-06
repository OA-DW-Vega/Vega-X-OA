package com.olam.warehouse.vegax.mtntcocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vega.model.VegaCocoaNoWeighmentWithLots
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaDeliveryPost
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.*
import com.olam.warehouse.vegax.mtntcocoa.data.domain.usecase.VegaCocoaDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCocoaMtntViewModel(private val useCase: VegaCocoaDispatchUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    var dispatchWh = VegaCocoaDispatchWB()
    var dispatchNoWeighmentWh = VegaCocoaNoWeighmentModel()
    val weighScaleWithLot = MutableLiveData<VegaCocoaNoWeighmentWithLots>()
    var noWeighmentlots: ArrayList<VegaCocoaNoWeighmentLot> = ArrayList()
    val thirdPartyMaterial = MutableLiveData<List<VegaMaterial>>()
    var lotList = ArrayList<VegaCocoaDispatchLots>()
    var materialModelList = ArrayList<VegaCoffeePurchaseOrderMaterialModel>()

    private var dispatchWeighBridge: LiveData<VegaCocoaDispatchWB> = MutableLiveData()
    private val _dispatchWB = MediatorLiveData<VegaCocoaDispatchWB>()
    val dispatchWB: LiveData<VegaCocoaDispatchWB> get() = _dispatchWB

    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

    private var noWeighmentSource: LiveData<List<VegaCocoaNoWeighmentWithLots>> = MutableLiveData()
    private val _dispatchnoWeighment = MediatorLiveData<List<VegaCocoaNoWeighmentWithLots>>()
    val dispatchnoWeighment: LiveData<List<VegaCocoaNoWeighmentWithLots>> get() = _dispatchnoWeighment


    private var pendingSource: LiveData<List<VegaCocoaNoWeighmentModel>> = MutableLiveData()
    private val _pending = MediatorLiveData<List<VegaCocoaNoWeighmentModel>>()
    val dispatchPending: LiveData<List<VegaCocoaNoWeighmentModel>> get() = _pending

    private var pendingWithLotSource: LiveData<List<VegaCocoaNoWeighmentWithLots>> = MutableLiveData()
    private val _pendingWithLot = MediatorLiveData<List<VegaCocoaNoWeighmentWithLots>>()
    val dispatchPendingWithLot: LiveData<List<VegaCocoaNoWeighmentWithLots>> get() = _pendingWithLot

    private var dispatchLotsSource: LiveData<List<VegaCocoaDispatchLots>> = MutableLiveData()
    private val _dispatchLots = MediatorLiveData<List<VegaCocoaDispatchLots>>()
    val dispatchLots: LiveData<List<VegaCocoaDispatchLots>> get() = _dispatchLots

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    val validateLot = MutableLiveData<VegaCocoaDispatchLots>()
    val offlineStockInfo = MutableLiveData<List<VegaEcuadorDispatchStocks>>()
    val vendorDetails = MutableLiveData<VegaVendor>()

    private var virtualDeliveryPostSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>> =
        MutableLiveData()
    private val _virtualDeliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>>()
    val virtualDeliveryPost: LiveData<Resource<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>> get() = _virtualDeliveryPost



    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> get() = _trucks

    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> = MutableLiveData()
    private val _purchaseOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> get() = _purchaseOrder

    private var purchaseOfflineSource: LiveData<List<VegaCocoaPurchaseOrders>> = MutableLiveData()
    private val _purchaseOfflineOrder = MediatorLiveData<List<VegaCocoaPurchaseOrders>>()
    val purchaseOfflineOrder: LiveData<List<VegaCocoaPurchaseOrders>> get() = _purchaseOfflineOrder

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails


    private var qualityNoWeighmentParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>> =
        MutableLiveData()
    private val _qualityNoWeighmentDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>>()
    val qualityNoWeighmentDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>> get() = _qualityNoWeighmentDetails

    private var productSource: LiveData<VegaMaterial> = mutableLiveDataOf()
    private val _product = MediatorLiveData<VegaMaterial>()
    val product: LiveData<VegaMaterial> get() = _product

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> = MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> get() = _deliveryPost

    var lots: ArrayList<VegaCocoaDispatchLots> = ArrayList()
    var enableProceed = MutableLiveData<Boolean>()

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _lots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockLots: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lots

    private var lotOfflineSource: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _lotsOffline = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val offlineStockLots: LiveData<List<VegaEcuadorDispatchStocks>> get() = _lotsOffline


    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems


    val weighBridgeWithLots = MutableLiveData<VegaCocoaMtntWithLots>()
    val weighScaleWithLotMaterial = MutableLiveData<VegaCocoaMtntWithLots>()

    private var weighBridgeWithLotSource: LiveData<VegaCocoaMtntWithLots> = MutableLiveData()
    private val _weighBridgeWithLot = MediatorLiveData<VegaCocoaMtntWithLots>()
    val weighBridgeWithLotsSource: LiveData<VegaCocoaMtntWithLots> get() = _weighBridgeWithLot

    private var weighScaleDeliveryPostSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>>>> =
        MutableLiveData()
    private val _weighScaleDeliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>>>>()
    val weighScaleDeliveryPost: LiveData<Resource<GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>>>> get() = _weighScaleDeliveryPost


    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>>>> get() = _pallet

    private var palletResourceWs: LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>> =
        MutableLiveData()
    private val _palletWs = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>>()
    val palletWs: LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>> get() = _palletWs

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    val packingMaterial: LiveData<List<VegaPackageMaterial>> get() = _material

    fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaDeliveryPost) = viewModelScope.launch(dispatchers.main) {
        _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
        }
        _deliveryPost.addSource(deliveryPostSource) {
            _deliveryPost.value = it
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

    fun getPurchaseOrder(receivingWerks: String) = viewModelScope.launch(dispatchers.main) {
        _purchaseOrder.removeSource(purchaseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSource = useCase.getPurchaseOrder(receivingWerks)
        }
        _purchaseOrder.addSource(purchaseSource) {
            _purchaseOrder.value = it
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

    fun getNoWeighmentLotDetails(charge: String, material: String, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityNoWeighmentDetails.removeSource(qualityNoWeighmentParamSource)
            withContext(dispatchers.io) {
                qualityNoWeighmentParamSource = useCase.getQualityNoWeighmentParams(charge, material, whId)
            }
            _qualityNoWeighmentDetails.addSource(qualityNoWeighmentParamSource) {
                _qualityNoWeighmentDetails.value = it
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
            useCase.saveDispatchAndLots(dispatchWh, lots)
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

    fun updateVirtualSuccessStatus(
        wbId: VegaCocoaNoWeighmentModel,
        lots: List<VegaCocoaNoWeighmentLot>
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateVirtualSuccessData(wbId, true, 4, "", lots)
        }
    }

    fun updateEndLoading() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateEndTime(dispatchWh.endTime, dispatchWh.turnAroundTime, dispatchWh.weighBridgeId)
        }
    }

    fun deleteTruckAndLotsData(whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteTruckAndLots(whId)
        }
    }

    fun addLoTInDB(lot: VegaCocoaDispatchLots) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLot(dispatchWh.weighBridgeId, lot)
        }
    }
    fun addLoTInDBWs(lot: List<VegaCocoaDispatchLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLotList(dispatchWh.weighBridgeId, lot)
        }
    }

    fun addLoTInDB(lot: VegaCocoaNoWeighmentLot) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLot(dispatchNoWeighmentWh.weighBridgeId, lot)
        }
    }

    fun addLoTInDB(lot: List<VegaCocoaNoWeighmentLot>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLot(dispatchWh.weighBridgeId, lot)
        }
    }

    fun removeLotFromList(batchNumber: String, material: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeLotFromTruck(batchNumber, material)
        }
    }
    fun removeLotFromListWs(batchNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeLotFromTruckWs(batchNumber)
        }
    }

    fun removeNoWeighmentFromList(batchNumber: String, material: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeNoweighmentLot(batchNumber, material)
        }
    }

    fun deleteWeighModel(weighBridgeId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeNoweighment(weighBridgeId)
        }
    }

    fun updateRemarks(remark: String, isStart: Boolean, whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateRemark(remark, isStart, whId)
        }
    }

    fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateDeliveryItem(deliveryItem, deliveryStatus, whId)
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

    fun validateLot(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber, material))
        }
    }

    fun getVendorInfo(vendor: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            vendorDetails.postValue(useCase.getVendorInfo(vendor))
        }
    }

    fun getStockList(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _lots.removeSource(lotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotSource = useCase.getStocks(material)
        }
        _lots.addSource(lotSource) {
            _lots.value = it
        }
    }

    fun getNoWeighmentStockList(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        viewModelScope.launch(dispatchers.main) {
            _qualityNoWeighmentDetails.removeSource(qualityNoWeighmentParamSource)
            withContext(dispatchers.io) {
                qualityNoWeighmentParamSource = useCase.getNoWeighmentStocks(material)
            }
            _qualityNoWeighmentDetails.addSource(qualityNoWeighmentParamSource) {
                _qualityNoWeighmentDetails.value = it
            }
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


    fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun saveBagDetails(bagMaterial: ArrayList<VegaCocoaSweepingBagMaterial>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id)
        }
    }

    fun deleteBagDetails(batchNumber: String, mtnNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(batchNumber, mtnNumber)
        }
    }

    fun getBagItems(isWeighscale:Boolean,batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(isWeighscale,batchNumber, material)

        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }
    fun getBagItems() = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getAllBagItems()
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

    fun getAllProduct() = viewModelScope.launch(dispatchers.main) {
        _allProduct.removeSource(allProductSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allProductSource = useCase.getAllProducts()
        }
        _allProduct.addSource(allProductSource) {
            _allProduct.value = it
        }
    }

    fun saveNoWeighmentDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertTruckInfo(dispatchNoWeighmentWh)
        }
    }

    fun getWeighScaleWithLotAndMaterial(whID: String, stoId: String, purchase: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                weighScaleWithLot.postValue(useCase.getWeighScaleInfo(whID, stoId, purchase))
            }
        }

    fun getWeighScaleWithLot(whID: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighScaleWithLot.postValue(useCase.getWeighScaleInfo(whID))
        }
    }

    fun getTransactionList() = viewModelScope.launch(dispatchers.main) {
        _dispatchnoWeighment.removeSource(noWeighmentSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            noWeighmentSource = useCase.getTransaction()
        }
        _dispatchnoWeighment.addSource(noWeighmentSource) {
            _dispatchnoWeighment.value = it
        }
    }


    fun getPendingList() = viewModelScope.launch(dispatchers.main) {
        _pending.removeSource(pendingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            pendingSource = useCase.getPendingList()
        }
        _pending.addSource(pendingSource) {
            _pending.value = it
        }
    }


    fun getPendingListWithLot() = viewModelScope.launch(dispatchers.main) {
        _pendingWithLot.removeSource(pendingWithLotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            pendingWithLotSource = useCase.getPendingListWithLot()
        }
        _pendingWithLot.addSource(pendingWithLotSource) {
            _pendingWithLot.value = it
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }

    fun postVirtualDeliveryDetails(vegaDeliveryPost: VegaCocoaVirtualPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _virtualDeliveryPost.removeSource(virtualDeliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                virtualDeliveryPostSource = useCase.virtualDeliveryDetail(vegaDeliveryPost)
            }
            _virtualDeliveryPost.addSource(virtualDeliveryPostSource) {
                _virtualDeliveryPost.value = it
            }
        }


    fun getPackingMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCase.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getOfflinePoList() = viewModelScope.launch(dispatchers.main) {
        _purchaseOfflineOrder.removeSource(purchaseOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseOfflineSource = useCase.getOfflinePoList()
        }
        _purchaseOfflineOrder.addSource(purchaseOfflineSource) {
            _purchaseOfflineOrder.value = it
        }
    }

    fun getOfflineStockList(material: String) = viewModelScope.launch(dispatchers.main) {
        _lots.removeSource(lotOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotOfflineSource = useCase.getOfflineStockList(material)
        }
        _lotsOffline.addSource(lotOfflineSource) {
            _lotsOffline.value = it
        }
    }

    fun getOfflineStockDetails(batchNo: String, material: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            offlineStockInfo.postValue(useCase.getOfflineStockInfo(batchNo, material))
        }
    }
    fun saveWeighBridgeDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertTruckInfoWeighscale(dispatchWh)
        }
    }
    fun deleteMaterialData(whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteMaterialData(whId)
        }
    }
    fun getWeighScaleWithLotAndMaterial(whID: String, stoId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighScaleWithLotMaterial.postValue(useCase.getWeighScaleInfo(whID, stoId))
        }
    }
    fun saveMaterialDetails(list: List<VegaCoffeePurchaseOrderMaterialModel>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertMaterialDetails(list)
        }
    }
    fun deleteMaterialDetials() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteMaterialDetials()
        }
    }
    fun deleteMaterialCodeData(mcode: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteMaterialCodeData(mcode)
        }
    }

    fun getMtntWithLots(wbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighBridgeWithLots.postValue(useCase.getMtntWithLots(wbId))
        }
    }
    fun validateLotWs(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLotWs(batchNumber))
        }
    }
    fun updateStartLoading() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateStartLoad(dispatchWh.startTime, dispatchWh.weighBridgeId)
        }
    }

    fun getPalletInfoWs(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _palletWs.removeSource(palletResourceWs)
        withContext(dispatchers.io) {
            palletResourceWs = useCase.getPalletDetailsWs(batchNumber, material)
        }
        _palletWs.addSource(palletResourceWs) {
            _palletWs.value = it
        }
    }

    fun getWeighBridgeWithLotAndMaterial(wbID: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeWithLot.removeSource(weighBridgeWithLotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeWithLotSource = useCase.getMtntWithLotsAndMaterial(wbID)
        }
        _weighBridgeWithLot.addSource(weighBridgeWithLotSource) {
            _weighBridgeWithLot.value = it
        }
    }
    fun updateSyncStatus(model: VegaCocoaMtntWithLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateAllSyncStatus(model)
            }
        }
    fun postWeighScaleDeliveryDetails(vegaDeliveryPost: VegaCocoaWeighscaleDeliveryPost) =
        viewModelScope.launch(dispatchers.main) {
            _weighScaleDeliveryPost.removeSource(weighScaleDeliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                weighScaleDeliveryPostSource = useCase.postWeighScaleDeliveryDetail(vegaDeliveryPost)
            }
            _weighScaleDeliveryPost.addSource(weighScaleDeliveryPostSource) {
                _weighScaleDeliveryPost.value = it
            }
        }

}
