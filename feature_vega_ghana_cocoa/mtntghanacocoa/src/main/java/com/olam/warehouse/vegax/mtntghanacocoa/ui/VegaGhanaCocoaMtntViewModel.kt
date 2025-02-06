package com.olam.warehouse.vegax.mtntghanacocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.domain.model.TruckManagementSeasonResponse
import com.olam.warehouse.presentation.data.domain.model.TruckManagementVehicleResponse
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.*
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.usecase.VegaGhanaCocoaDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaGhanaCocoaMtntViewModel(private val useCaseCocoa: VegaGhanaCocoaDispatchUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    var dispatchWh = VegaCocoaDispatchWB()
    var lotList = ArrayList<VegaGhanaCocoaDispatchLots>()
    var materialModelList = ArrayList<VegaGhanaPurchaseOrderMaterialModel>()
    val bagWithMaterial = MutableLiveData<List<VegaCocoaFgrnGradesMatrialWeights>>()
    //private var weighScaleWithLotMaterial: LiveData<VegaGhanaCocoaMtntWithLots> = MutableLiveData()

    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    private var dispatchWeighBridge: LiveData<VegaCocoaDispatchWB> = MutableLiveData()
    private val _dispatchWB = MediatorLiveData<VegaCocoaDispatchWB>()
    val dispatchWB: LiveData<VegaCocoaDispatchWB> get() = _dispatchWB

    private var dispatchLotsSource: LiveData<List<VegaGhanaCocoaDispatchLots>> = MutableLiveData()
    private val _dispatchLots = MediatorLiveData<List<VegaGhanaCocoaDispatchLots>>()
    val dispatchLots: LiveData<List<VegaGhanaCocoaDispatchLots>> get() = _dispatchLots

    private var weighBridgeWithLotSource: LiveData<VegaGhanaCocoaMtntWithLots> = MutableLiveData()
    private val _weighBridgeWithLot = MediatorLiveData<VegaGhanaCocoaMtntWithLots>()
    val weighBridgeWithLotsSource: LiveData<VegaGhanaCocoaMtntWithLots> get() = _weighBridgeWithLot

    private var weighBridgeWithLotSourceNew: LiveData<List<VegaGhanaCocoaMtntWithLots>> = MutableLiveData()
    private val _weighBridgeWithLotNew = MediatorLiveData<List<VegaGhanaCocoaMtntWithLots>>()
    val weighBridgeWithLotsSourceNew: LiveData<List<VegaGhanaCocoaMtntWithLots>> get() = _weighBridgeWithLotNew

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> get() = _stocks

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>> = MutableLiveData()
    private val _lots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>>()
    val stockLots: LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>> get() = _lots

    private var stocksSourceOffline: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _stocksOffline = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val stocksOffline: LiveData<List<VegaEcuadorDispatchStocks>> get() = _stocksOffline

    val validateLot = MutableLiveData<VegaGhanaCocoaDispatchLots>()
    val weighBridgeWithLots = MutableLiveData<VegaGhanaCocoaMtntWithLots>()
    val weighScaleWithLotMaterial = MutableLiveData<VegaGhanaCocoaMtntWithLots>()

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> get() = _trucks

    private var truckDetailSource: LiveData<Resource<TruckManagementVehicleResponse>> =
        MutableLiveData()
    private val _truckDetails = MediatorLiveData<Resource<TruckManagementVehicleResponse>>()
    val truckDetails: LiveData<Resource<TruckManagementVehicleResponse>> get() = _truckDetails

    private var seasonDetailSource: LiveData<Resource<TruckManagementSeasonResponse>> =
        MutableLiveData()
    private val _seasonDetails = MediatorLiveData<Resource<TruckManagementSeasonResponse>>()
    val seasonDetails: LiveData<Resource<TruckManagementSeasonResponse>> get() = _seasonDetails

    private var seasonDetailOfflineSource: LiveData<List<VehicleDetails>> =
        MutableLiveData()
    private val _seasonDetailsOffline = MediatorLiveData<List<VehicleDetails>>()
    val seasonDetailsOffline: LiveData<List<VehicleDetails>> get() = _seasonDetailsOffline

    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>>>> =
        MutableLiveData()
    private val _purchaseOrder =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>>>> get() = _purchaseOrder

    private var purchaseSourceOffline: LiveData<List<VegaCocoaPurchaseOrders>> = MutableLiveData()
    private val _purchaseOrderOffline = MediatorLiveData<List<VegaCocoaPurchaseOrders>>()
    val purchaseOrderOffline: LiveData<List<VegaCocoaPurchaseOrders>> get() = _purchaseOrderOffline

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>> get() = _qualityDetails

    private var productSource: LiveData<VegaMaterial> = mutableLiveDataOf()
    private val _product = MediatorLiveData<VegaMaterial>()
    val product: LiveData<VegaMaterial> get() = _product


    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

   private var materialStorgeValue: LiveData<List<VegaStorageLocationDetail>> = mutableLiveDataOf(emptyList())
    private val _materialSourceValue = MediatorLiveData<List<VegaStorageLocationDetail>>()
    val materialStorgeSourceValue: LiveData<List<VegaStorageLocationDetail>> get() = _materialSourceValue

    private var storageLocationListSource: LiveData<List<VegaStorageLocationDetail>> = MutableLiveData()
    private val _storageLocationList = MediatorLiveData<List<VegaStorageLocationDetail>>()
    val storageLocationList: LiveData<List<VegaStorageLocationDetail>> get() = _storageLocationList

    private var allPlantRouteSource: LiveData<List<VegaPlanRoute>> = mutableLiveDataOf()
    private val _allPlantRoute = MediatorLiveData<List<VegaPlanRoute>>()
    val allPlantRoute: LiveData<List<VegaPlanRoute>> get() = _allPlantRoute

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var deliverySource: LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> = MutableLiveData()
    private val _delivery = MediatorLiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>>()
    val delivery: LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> get() = _delivery

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> = MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> get() = _deliveryPost


    private var weighScaleDeliveryPostSource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>> =
        MutableLiveData()
    private val _weighScaleDeliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>>()
    val weighScaleDeliveryPost: LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>> get() = _weighScaleDeliveryPost

    private var wsPostSource: LiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>> =
        MutableLiveData()
    private val _wsdeliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>>()
    val wsdeliveryPost: LiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>> get() = _wsdeliveryPost


    var lots: ArrayList<VegaGhanaCocoaDispatchLots> = ArrayList()
    var enableProceed = MutableLiveData<Boolean>()

    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntWeighScalePallet>>>> get() = _pallet

    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location

    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()

    private var uomDetailSource: LiveData<List<VegaUomDetails>> = MutableLiveData()
    private val _uomDetail = MediatorLiveData<List<VegaUomDetails>>()
    val uomDetail: LiveData<List<VegaUomDetails>> get() = _uomDetail

    val truck: LiveData<List<VegaCocoaDispatchWB>> get() = _truck
    private var truckSource: LiveData<List<VegaCocoaDispatchWB>> = MutableLiveData()
    private val _truck = MediatorLiveData<List<VegaCocoaDispatchWB>>()

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material

    private var validateNumberSource: LiveData<Resource<GenericReqAndResp<ValidateNumber>>> =
        MutableLiveData()
    private val _validateNumber = MediatorLiveData<Resource<GenericReqAndResp<ValidateNumber>>>()
    val validateNumber: LiveData<Resource<GenericReqAndResp<ValidateNumber>>> get() = _validateNumber

    fun validateReceiptNumber(plantId:String,whNumber:String, wbType:String) = viewModelScope.launch(dispatchers.main){
        _validateNumber.removeSource(validateNumberSource)
        withContext(dispatchers.io) {
            validateNumberSource = useCaseCocoa.validateNumbers(plantId, whNumber, wbType)
        }
        _validateNumber.addSource(validateNumberSource) {
            _validateNumber.value = it
        }
    }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCaseCocoa.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCaseCocoa.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getDelivery(delivery: String, deliveryItem: String) = viewModelScope.launch(dispatchers.main) {
        _delivery.removeSource(deliverySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliverySource = useCaseCocoa.getDelivery(delivery, deliveryItem)
        }
        _delivery.addSource(deliverySource) {
            _delivery.value = it
        }
    }

    fun saveLotDetails(list: VegaGhanaCocoaDispatchLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.saveLot(list)
            }
        }

    fun saveLot(list: VegaGhanaCocoaDispatchLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.saveLotData(list)
            }
        }

    fun postDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntDeliveryPost) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryPostSource = useCaseCocoa.postDeliveryDetail(vegaDeliveryPost)
            }
            _deliveryPost.addSource(deliveryPostSource) {
                _deliveryPost.value = it
            }
        }


    fun postWeighScaleDeliveryDetails(vegaDeliveryPost: VegaGhanaMtntMergedDeliveryPost) =
        viewModelScope.launch(dispatchers.main) {
            _weighScaleDeliveryPost.removeSource(weighScaleDeliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                //weighScaleDeliveryPostSource = useCase.postWeighScaleDeliveryDetail(vegaDeliveryPost)
            }
            _weighScaleDeliveryPost.addSource(weighScaleDeliveryPostSource) {
                _weighScaleDeliveryPost.value = it
            }
        }

    fun postWeighScaleDeliveryDetails(
        vegaDeliveryPost: VegaGhanaMtntDeliveryPost,
        wayBillNumber: String
    ) =
        viewModelScope.launch(dispatchers.main) {
            _weighScaleDeliveryPost.removeSource(weighScaleDeliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                weighScaleDeliveryPostSource =
                    useCaseCocoa.postWeighScaleDeliveryDetail(vegaDeliveryPost, wayBillNumber)
            }
            _weighScaleDeliveryPost.addSource(weighScaleDeliveryPostSource) {
                _weighScaleDeliveryPost.value = it
            }
        }

    fun postWeighScaleDeliveryDetails123(vegaSesameDeliveryPost: VegaGhanaMtntMergedDeliveryPost) {
       viewModelScope.launch (dispatchers.main){
          _wsdeliveryPost.removeSource(wsPostSource)
           withContext(dispatchers.io){
               wsPostSource = useCaseCocoa.postWs(vegaSesameDeliveryPost)
           }
           _wsdeliveryPost.addSource(wsPostSource){
               _wsdeliveryPost.value = it
           }
       }
    }

    fun getWeighBridgeWithLotAndMaterial(wbID: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeWithLot.removeSource(weighBridgeWithLotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeWithLotSource = useCaseCocoa.getMtntWithLotsAndMaterial(wbID)
        }
        _weighBridgeWithLot.addSource(weighBridgeWithLotSource) {
            _weighBridgeWithLot.value = it
        }
    }

    fun getWeighBridgeStockCount() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeWithLotNew.removeSource(weighBridgeWithLotSourceNew) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeWithLotSourceNew = useCaseCocoa.getWeighBridgeStockCount()
        }
        _weighBridgeWithLotNew.addSource(weighBridgeWithLotSourceNew) {
            _weighBridgeWithLotNew.value = it
        }
    }

    fun getWeighScaleWithLotAndMaterial(whID: String, stoId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighScaleWithLotMaterial.postValue(useCaseCocoa.getWeighScaleInfo(whID, stoId))
        }
    }

/*
    fun getWeighScaleWithLotAndMaterial() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighScaleWithLotMaterial.postValue(useCase.getWeighScaleInfo())
        }
    }

*/

    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(dispatchTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchTruckResource = useCaseCocoa.getTrucks()
        }
        _trucks.addSource(dispatchTruckResource) {
            _trucks.value = it
        }
    }

    fun getTruckDetails(seasonId:String) = viewModelScope.launch(dispatchers.main) {
        _truckDetails.removeSource(truckDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            truckDetailSource = useCaseCocoa.getTruckMangeDetails(seasonId)
        }
        _truckDetails.addSource(truckDetailSource) {
            _truckDetails.value = it
        }
    }

    fun getSeasonDetails() = viewModelScope.launch(dispatchers.main) {
        _seasonDetails.removeSource(seasonDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            seasonDetailSource = useCaseCocoa.getSeasonDetails()
        }
        _seasonDetails.addSource(seasonDetailSource) {
            _seasonDetails.value = it
        }
    }

    fun getSeasonDetailsOffline() = viewModelScope.launch(dispatchers.main) {
        _seasonDetailsOffline.removeSource(seasonDetailOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            seasonDetailOfflineSource = useCaseCocoa.getSeasonDetailsOffline()
        }
        _seasonDetailsOffline.addSource(seasonDetailOfflineSource) {
            _seasonDetailsOffline.value = it
        }
    }


    fun getPurchaseOrder(receivingWerks: String) = viewModelScope.launch(dispatchers.main) {
        _purchaseOrder.removeSource(purchaseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSource = useCaseCocoa.getPurchaseOrder(receivingWerks)
        }
        _purchaseOrder.addSource(purchaseSource) {
            _purchaseOrder.value = it
        }
    }

    fun getLotDetails(charge: String, material: List<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCaseCocoa.getQualityParams(charge, material, whId)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }

    fun getProduct(code: String) = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCaseCocoa.getProducts(code)
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }


    fun getAllProduct() = viewModelScope.launch(dispatchers.main) {
        _allProduct.removeSource(allProductSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allProductSource = useCaseCocoa.getAllProducts()
        }
        _allProduct.addSource(allProductSource) {
            _allProduct.value = it
        }
    }

    fun getPlantRouteDetails() = viewModelScope.launch(dispatchers.main) {
        _allPlantRoute.removeSource(allPlantRouteSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allPlantRouteSource = useCaseCocoa.getAllPlantRoute()
        }
        _allPlantRoute.addSource(allPlantRouteSource) {
            _allPlantRoute.value = it
        }
    }


    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCaseCocoa.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
        }
    }

    fun getUomDetails() = viewModelScope.launch(dispatchers.main) {
        _uomDetail.removeSource(uomDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            uomDetailSource = useCaseCocoa.getuomDetail()
        }
        _uomDetail.addSource(uomDetailSource) {
            _uomDetail.value = it
        }
    }

    fun getOfflineTruckDetails() = viewModelScope.launch(dispatchers.main) {
        _truck.removeSource(truckSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            truckSource = useCaseCocoa.getTruckDetails()
        }
        _truck.addSource(truckSource) {
            _truck.value = it
        }
    }

    fun saveWeighBridgeAndLotDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.saveDispatchAndLots(dispatchWh, lotList)
        }
    }

    fun saveMaterialDetails(list: List<VegaGhanaPurchaseOrderMaterialModel>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.insertMaterialDetails(list)
        }
    }

    fun updateEndLoading() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.updateEndTime(dispatchWh.endTime, dispatchWh.turnAroundTime, dispatchWh.weighBridgeId)
        }
    }

    fun updateStartLoading() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.updateStartLoad(dispatchWh.startTime, dispatchWh.weighBridgeId)
        }
    }

    fun deleteTruckAndLotsData(whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteTruckAndLots(whId)
        }
    }

    fun deleteMaterialData(whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteMaterialData(whId)
        }
    }

    fun deleteGhanaLotList() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteGhanaLotList()
        }
    }

    fun deleteMaterialData() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteMaterialData()
        }
    }

    fun addLoTInDB(lot: VegaGhanaCocoaDispatchLots) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.insertLot(dispatchWh.weighBridgeId, lot)
        }
    }

    fun addLoTInDB(lot: List<VegaGhanaCocoaDispatchLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.insertLotList(dispatchWh.weighBridgeId, lot)
        }
    }

    fun saveWeighBridgeDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.insertTruckInfo(dispatchWh)
        }
    }

    fun removeLotFromList(batchNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.removeLotFromTruck(batchNumber)
        }
    }
    fun removeGhanaLotFromList(batchNumber: String,wbid:String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.removeGhanaLotFromList(batchNumber,wbid)
        }
    }
    fun removeLotList() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.removeLotFromList()
        }
    }

    fun removeGhanaLotList(wbid: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.removeGhanaLotList(wbid)
        }
    }

    fun updateRemarks(remark: String, isStart: Boolean, whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.updateRemark(remark, isStart, whId)
        }
    }

    fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.updateDeliveryItem(deliveryItem, deliveryStatus, whId)
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCaseCocoa.validateLot(batchNumber))
        }
    }

    fun getStockList(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _lots.removeSource(lotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotSource = useCaseCocoa.getStocks(material)
        }
        _lots.addSource(lotSource) {
            _lots.value = it
        }
    }

    fun getDispatchLotList(wbId: String) = viewModelScope.launch(dispatchers.main) {
        _dispatchLots.removeSource(dispatchLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchLotsSource = useCaseCocoa.getDispatchLotList(wbId)
        }
        _dispatchLots.addSource(dispatchLotsSource) {
            _dispatchLots.value = it
        }
    }

    fun fetchStocksOffline() = viewModelScope.launch(dispatchers.main) {
        _stocksOffline.removeSource(stocksSourceOffline)
        withContext(dispatchers.io) {
            stocksSourceOffline = useCaseCocoa.getStockListOffline()
        }
        _stocksOffline.addSource(stocksSourceOffline) {
            _stocksOffline.value = it
        }
    }

    fun updateStockDetails(weight: String, batchNo: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.updateStockDetails(weight, batchNo)
            }
        }

    fun updateMtntPurchaseOrderDetails(weight: String, po: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.updateMtntPurchaseOrderDetails(weight, po)
            }
        }

    fun getMtntWithLots(wbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighBridgeWithLots.postValue(useCaseCocoa.getMtntWithLots(wbId))
        }
    }

    fun getMtntOfflineGradeWithBags(fgrnIdWithMatrial: String, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                bagWithMaterial.postValue(useCaseCocoa.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNo))
            }
        }

    fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteBagDetails(id)
        }
    }

    fun deleteBagDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteBagDetails()
        }
    }

    fun deleteBag(batchNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteBag(batchNumber)
        }
    }

    fun getBagItems(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCaseCocoa.getBagItems(batchNumber, material)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun getBagItems(batchNumber: String, material: String, wbid: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCaseCocoa.getBagItems(batchNumber, material, wbid)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun getBagItems() = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCaseCocoa.getAllBagItems()
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun getPalletInfo(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _pallet.removeSource(palletResource)
        withContext(dispatchers.io) {
            palletResource = useCaseCocoa.getPalletDetails(batchNumber, material)
        }
        _pallet.addSource(palletResource) {
            _pallet.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCaseCocoa.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun updateSyncStatus(model: VegaGhanaCocoaMtntWithLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.updateAllSyncStatus(model)
            }
        }

    fun updateGhanaSyncStatus(model: VegaGhanaCocoaMtntWithLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.updateSyncStatus(model)
            }
        }

    fun getSendingWHLocations() = viewModelScope.launch(dispatchers.main) {
        _location.removeSource(locationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            locationSource = useCaseCocoa.getLocations()
        }
        _location.addSource(locationSource) {
            _location.value = it
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCaseCocoa.getThirdPartyMaterials())
        }
    }

    fun getProcessTypeList(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCaseCocoa.getProcessTypeList(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun fetchStocks(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCaseCocoa.getStocks(material)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun getPurchaseOrderOffline() = viewModelScope.launch(dispatchers.main) {
        _purchaseOrderOffline.removeSource(purchaseSourceOffline) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSourceOffline = useCaseCocoa.getPurchaseOrderOffline()
        }
        _purchaseOrderOffline.addSource(purchaseSourceOffline) {
            _purchaseOrderOffline.value = it
        }
    }

    fun updateSyncedMtntDeletedItem(status: Int) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.updateSyncedMtntDeletedItem(status)
        }
    }

    fun getStorageLocationDetail() = viewModelScope.launch(dispatchers.main) {
        _storageLocationList.removeSource(storageLocationListSource)
        withContext(dispatchers.io) {
            storageLocationListSource = useCaseCocoa.getStorageLocationDetail()
        }
        _storageLocationList.addSource(storageLocationListSource) {
            _storageLocationList.value = it
        }
    }

}
