package com.olam.warehouse.vegax.dispatchnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.*
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.usecase.VegaNigeriaCocoaDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaNigeriaCocoaMtntViewModel(private val useCase: VegaNigeriaCocoaDispatchUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {
    var dispatchWh = VegaCocoaDispatchWB()
    var lotList = ArrayList<VegaCocoaDispatchLots>()
    var materialModelList = ArrayList<VegaCoffeePurchaseOrderMaterialModel>()
    val bagWithMaterial = MutableLiveData<List<VegaCocoaFgrnGradesMatrialWeights>>()
    //private var weighScaleWithLotMaterial: LiveData<VegaCocoaMtntWithLots> = MutableLiveData()

    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    private var gateEntryTruckResource1: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> =
        MutableLiveData()
    private val _waitingTrucks1 =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>()
    val waitingTrucks1: LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> get() = _waitingTrucks1

    private var dispatchWeighBridge: LiveData<VegaCocoaDispatchWB> = MutableLiveData()
    private val _dispatchWB = MediatorLiveData<VegaCocoaDispatchWB>()
    val dispatchWB: LiveData<VegaCocoaDispatchWB> get() = _dispatchWB

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var dispatchLotsSource: LiveData<List<VegaCocoaDispatchLots>> = MutableLiveData()
    private val _dispatchLots = MediatorLiveData<List<VegaCocoaDispatchLots>>()
    val dispatchLots: LiveData<List<VegaCocoaDispatchLots>> get() = _dispatchLots

    private var weighBridgeWithLotSource: LiveData<VegaCocoaMtntWithLots> = MutableLiveData()
    private val _weighBridgeWithLot = MediatorLiveData<VegaCocoaMtntWithLots>()
    val weighBridgeWithLotsSource: LiveData<VegaCocoaMtntWithLots> get() = _weighBridgeWithLot

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _lots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockLots: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lots

    val validateLot = MutableLiveData<VegaCocoaDispatchLots>()
    val weighBridgeWithLots = MutableLiveData<VegaCocoaMtntWithLots>()
    val weighScaleWithLotMaterial = MutableLiveData<VegaCocoaMtntWithLots>()

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> get() = _trucks

    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>>> =
        MutableLiveData()
    private val _purchaseOrder =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>>> get() = _purchaseOrder

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails

    private var qualitySourceNew: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>> =
        MutableLiveData()
    private val _qualityDetailsNew =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>>()
    val qualityDetailsNew: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>> get() = _qualityDetailsNew

    private var productSource: LiveData<VegaMaterial> = mutableLiveDataOf()
    private val _product = MediatorLiveData<VegaMaterial>()
    val product: LiveData<VegaMaterial> get() = _product


    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var deliverySource: LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> =
        MutableLiveData()
    private val _delivery = MediatorLiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>>()
    val delivery: LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> get() = _delivery

    private var qualityNigeriaSource: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> =
        MutableLiveData()
    private val _qualityNigeria =
        MediatorLiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>>()
    val qualityNigeria: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> get() = _qualityNigeria

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> =
        MutableLiveData()
    private val _deliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> get() = _deliveryPost

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> =
        MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var weighScaleDeliveryPostSource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntDeliveryDetail>>>> =
        MutableLiveData()
    private val _weighScaleDeliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntDeliveryDetail>>>>()
    val weighScaleDeliveryPost: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntDeliveryDetail>>>> get() = _weighScaleDeliveryPost

    private var wsPostSource: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>> =
        MutableLiveData()
    private val _wsdeliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>>()
    val wsdeliveryPost: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>> get() = _wsdeliveryPost

    private var wsPostBinMergeSource: LiveData<Resource<GenericReqAndResp<MergedData>>> =
        MutableLiveData()
    private val _wsdeliveryBinMergePost =
        MediatorLiveData<Resource<GenericReqAndResp<MergedData>>>()
    val wsdeliveryBinMergePost: LiveData<Resource<GenericReqAndResp<MergedData>>> get() = _wsdeliveryBinMergePost

    private var wsWeightedAvgSource: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>> =
        MutableLiveData()
    private val _wsWeightedAvgPost =
        MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>>()
    val wsWeightedAvgPost: LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>> get() = _wsWeightedAvgPost

    var lots: ArrayList<VegaCocoaDispatchLots> = ArrayList()
    var enableProceed = MutableLiveData<Boolean>()

    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>>> get() = _pallet

    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location

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

    fun getDelivery(delivery: String, deliveryItem: String) =
        viewModelScope.launch(dispatchers.main) {
            _delivery.removeSource(deliverySource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliverySource = useCase.getDelivery(delivery, deliveryItem)
            }
            _delivery.addSource(deliverySource) {
                _delivery.value = it
            }
        }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCase.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getWaitingTruckList(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _waitingTrucks1.removeSource(gateEntryTruckResource1) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateEntryTruckResource1 = useCase.getWaitingTrucks1(selectedPlantId)
        }
        _waitingTrucks1.addSource(gateEntryTruckResource1) {
            _waitingTrucks1.value = it
        }
    }

    fun postQualityParamsNigeria(qualityPost: VegaQualityNigeriaPost) =
        viewModelScope.launch(dispatchers.main) {
            _qualityNigeria.removeSource(qualityNigeriaSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityNigeriaSource = useCase.postNigeriaQuality(qualityPost)
            }
            _qualityNigeria.addSource(qualityNigeriaSource) {
                _qualityNigeria.value = it
            }
        }

    fun saveLotDetails(list: VegaCocoaDispatchLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveLot(list)
            }
        }

    fun getQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>> {
        fetchQualityDetails(charge, material)
        return qualityDetailsNew
    }

    private fun fetchQualityDetails(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetailsNew.removeSource(qualitySourceNew)
            withContext(dispatchers.io) {
                qualitySourceNew = useCase.fetchQualityDetails(charge, material)
            }
            _qualityDetailsNew.addSource(qualitySourceNew) {
                _qualityDetailsNew.value = it
            }
        }


    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityListSource = useCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityListSource) {
                _qualitylist.value = it
            }
        }

    fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntDeliveryPost) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
            }
            _deliveryPost.addSource(deliveryPostSource) {
                _deliveryPost.value = it
            }
        }


    fun postWeighScaleDeliveryDetails(vegaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost) =
        viewModelScope.launch(dispatchers.main) {
            _weighScaleDeliveryPost.removeSource(weighScaleDeliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                //weighScaleDeliveryPostSource = useCase.postWeighScaleDeliveryDetail(vegaDeliveryPost)
            }
            _weighScaleDeliveryPost.addSource(weighScaleDeliveryPostSource) {
                _weighScaleDeliveryPost.value = it
            }
        }

    fun postWeighScaleDeliveryBinMerge(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntBinMerge) {
        viewModelScope.launch(dispatchers.main) {
            _wsdeliveryBinMergePost.removeSource(wsPostBinMergeSource)
            withContext(dispatchers.io) {
                wsPostBinMergeSource = useCase.postWsBinMerge(vegaCocoaDeliveryPost)
            }
            _wsdeliveryBinMergePost.addSource(wsPostBinMergeSource) {
                _wsdeliveryBinMergePost.value = it
            }
        }
    }

    fun postWeightedAverage(vegaWeightedAvgPost: VegaNigeriaCocoaWeightedAveragePost) {
        viewModelScope.launch(dispatchers.main) {
            _wsWeightedAvgPost.removeSource(wsWeightedAvgSource)
            withContext(dispatchers.io) {
                wsWeightedAvgSource = useCase.postWeightedAverage(vegaWeightedAvgPost)
            }
            _wsWeightedAvgPost.addSource(wsWeightedAvgSource) {
                _wsWeightedAvgPost.value = it
            }
        }
    }

    fun postWeighScaleDeliveryDetails123(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost) {
        viewModelScope.launch(dispatchers.main) {
            _wsdeliveryPost.removeSource(wsPostSource)
            withContext(dispatchers.io) {
                wsPostSource = useCase.postWs(vegaCocoaDeliveryPost)
            }
            _wsdeliveryPost.addSource(wsPostSource) {
                _wsdeliveryPost.value = it
            }
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

    fun getWeighScaleWithLotAndMaterial(whID: String, stoId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighScaleWithLotMaterial.postValue(useCase.getWeighScaleInfo(whID, stoId))
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

    fun getProduct(code: String) = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts(code)
        }
        _product.addSource(productSource) {
            _product.value = it
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
            useCase.saveDispatchAndLots(dispatchWh, lotList)
        }
    }

    fun saveMaterialDetails(list: List<VegaCoffeePurchaseOrderMaterialModel>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertMaterialDetails(list)
        }
    }

    fun updateEndLoading() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateEndTime(dispatchWh.endTime, dispatchWh.turnAroundTime, dispatchWh.weighBridgeId)
        }
    }

    fun updateStartLoading() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateStartLoad(dispatchWh.startTime, dispatchWh.weighBridgeId)
        }
    }

    fun deleteTruckAndLotsData(whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteTruckAndLots(whId)
        }
    }

    fun deleteMaterialData(whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteMaterialData(whId)
        }
    }

    fun deleteMaterialData() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteMaterialData()
        }
    }

    fun addLoTInDB(lot: VegaCocoaDispatchLots) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLot(dispatchWh.weighBridgeId, lot)
        }
    }

    fun addLoTInDB(lot: List<VegaCocoaDispatchLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLotList(dispatchWh.weighBridgeId, lot)
        }
    }

    fun saveWeighBridgeDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertTruckInfo(dispatchWh)
        }
    }

    fun removeLotFromList(batchNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeLotFromTruck(batchNumber)
        }
    }
    fun removeLotList() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeLotFromList()
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

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
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

    fun getMtntWithLots(wbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighBridgeWithLots.postValue(useCase.getMtntWithLots(wbId))
        }
    }

    fun getMtntOfflineGradeWithBags(fgrnIdWithMatrial: String, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                bagWithMaterial.postValue(useCase.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNo))
            }
        }

    fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id)
        }
    }

    fun deleteBagDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails()
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

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun updateSyncStatus(model: VegaCocoaMtntWithLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateAllSyncStatus(model)
            }
        }
    fun getSendingWHLocations() = viewModelScope.launch(dispatchers.main) {
        _location.removeSource(locationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            locationSource = useCase.getLocations()
        }
        _location.addSource(locationSource) {
            _location.value = it
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }

    fun getProcessTypeList(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCase.getProcessTypeList(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }



}
