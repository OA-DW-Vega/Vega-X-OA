package com.olam.warehouse.vegax.mtntnicaragua.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.modal.OfflineInventory
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.master.veganicaragua.model.VegaNicDispatchLotsWithBags
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryPost
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicPurchaseOrderModel
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicTicketListModel
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.usecase.VegaNicaraguaMtntUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 11/11/2020.
 */
class VegaNicaraguaMtntViewModel(
    private val useCase: VegaNicaraguaMtntUsecase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var mtnt = VegaNicaraguaMtnt()
    var revert = VegaNicDispatchLots()
    var lotList = ArrayList<VegaNicDispatchLotsWithBags>()

    val lotDetailsOffline = MutableLiveData<List<VegaNicaraguaGRNInventoryDetails>>()

    val productList = MutableLiveData<List<VegaMaterial>>()
    val purchaseOrderOffline = MutableLiveData<List<VegaCocoaPurchaseOrders>>()

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<VegaMaterial> = mutableLiveDataOf()
    private val _product = MediatorLiveData<VegaMaterial>()
    val product: LiveData<VegaMaterial> get() = _product

    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaNicPurchaseOrderModel>>>> =
        MutableLiveData()
    private val _purchaseOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicPurchaseOrderModel>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaNicPurchaseOrderModel>>>> get() = _purchaseOrder

    //    val weighBridgeLotsWithBags = MutableLiveData<List<VegaNicDispatchLotsWithBags>>()
    val validateLot = MutableLiveData<VegaNicDispatchLots>()

    private var weighBridgeLotsWithBagsSource: LiveData<VegaMtntWithLotsWithBags> = mutableLiveDataOf()
    private val _weighBridgeLotsWithBags = MediatorLiveData<VegaMtntWithLotsWithBags>()
    val weighBridgeLotsWithBags: LiveData<VegaMtntWithLotsWithBags> get() = _weighBridgeLotsWithBags

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaNicDispatchLots>>>> =
        MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicDispatchLots>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaNicDispatchLots>>>> get() = _lotDetails

    private var bagSource: LiveData<List<VegaNicaraguaWeighmentBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaNicaraguaWeighmentBagMaterial>>()
    val bagItems: LiveData<List<VegaNicaraguaWeighmentBagMaterial>> get() = _bagItems

    private var stockLotsSource: LiveData<Resource<GenericReqAndResp<OfflineInventory>>> = MutableLiveData()
    private val _lots = MediatorLiveData<Resource<GenericReqAndResp<OfflineInventory>>>()
    val stockLots: LiveData<Resource<GenericReqAndResp<OfflineInventory>>> get() = _lots

    private var stockLotsSapSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _lotsSap = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockLotsSap: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lotsSap

    private var stockLotsSourceOffline: LiveData<List<VegaNicaraguaGRNInventoryDetails>> = MutableLiveData()
    private val _lotsOffline = MediatorLiveData<List<VegaNicaraguaGRNInventoryDetails>>()
    val stockLotsOffline: LiveData<List<VegaNicaraguaGRNInventoryDetails>> get() = _lotsOffline

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

    private var weighScaleDeliveryPostSource: LiveData<Resource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>> =
        MutableLiveData()
    private val _weighScaleDeliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>>()
    val weighScaleDeliveryPost: LiveData<Resource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>> get() = _weighScaleDeliveryPost

    private var listWBLotsWithBagsSource: LiveData<List<VegaMtntWithLotsWithBags>> =
        mutableLiveDataOf()
    private val _listWBLotsWithBags = MediatorLiveData<List<VegaMtntWithLotsWithBags>>()
    val listWBLotsWithBags: LiveData<List<VegaMtntWithLotsWithBags>> get() = _listWBLotsWithBags

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    //added for mtnr ticket - reprint
    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaNicTicketListModel>>>> =
        MutableLiveData()
    private val _weighBridgeOnline =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicTicketListModel>>>>()
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaNicTicketListModel>>>> get() = _weighBridgeOnline

    /* private var lotQualitySource: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>> = MutableLiveData()
     private val _lotQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>>()
     val lotQuality: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>> get() = _lotQuality

     fun getLotQuality(material: String) = viewModelScope.launch(dispatchers.main) {
         _lotQuality.removeSource(lotQualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
         withContext(dispatchers.io) {
             lotQualitySource = useCase.getLotQuality(material)
         }
         _lotQuality.addSource(lotQualitySource) {
             _lotQuality.value = it
         }
     }*/

    private var grnPrintDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaMtnt>>>> =
        MutableLiveData()
    private val _grnPrintDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicaraguaMtnt>>>>()
    val grnPrintDetails: LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaMtnt>>>> get() = _grnPrintDetails

    fun getOfflineLotDetails(lotId: String, materialCode: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            lotDetailsOffline.postValue(useCase.getOfflineLotDetails(lotId, materialCode))
        }
    }

    fun getGrnPrintDetails() = viewModelScope.launch(dispatchers.main) {
        _grnPrintDetails.removeSource(grnPrintDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnPrintDetailsSource = useCase.getGrnPrintDetails()
        }
        _grnPrintDetails.addSource(grnPrintDetailsSource) {
            _grnPrintDetails.value = it
        }
    }

    fun getListOfMtntWithLots() = viewModelScope.launch(dispatchers.main) {
        _listWBLotsWithBags.removeSource(listWBLotsWithBagsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            listWBLotsWithBagsSource = useCase.getListOfMtntWithLots()
        }
        _listWBLotsWithBags.addSource(listWBLotsWithBagsSource) {
            _listWBLotsWithBags.value = it
        }
    }

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

    fun getStockListOffline(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _lotsOffline.removeSource(stockLotsSourceOffline) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            stockLotsSourceOffline = useCase.getStockListOffline(material)
        }
        _lotsOffline.addSource(stockLotsSourceOffline) {
            _lotsOffline.value = it
        }
    }

    fun getStockList(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _lots.removeSource(stockLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            stockLotsSource = useCase.getStocks(material)
        }
        _lots.addSource(stockLotsSource) {
            _lots.value = it
        }
    }

    fun getStockListSap(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _lotsSap.removeSource(stockLotsSapSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            stockLotsSapSource = useCase.getStocksSap(material)
        }
        _lotsSap.addSource(stockLotsSapSource) {
            _lotsSap.value = it
        }
    }

    fun getBagItems(batchNumber: String, tempId: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber, tempId)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun getLotDetails(charge: String, material: List<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotDetails.removeSource(lotSource)
            withContext(dispatchers.io) {
                lotSource = useCase.getLotDetails(charge, material, whId)
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

    /*fun getMtntWithLots(tmpId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            weighBridgeLotsWithBags.postValue(useCase.getMtntWithLots(tmpId))
        }
    }*/

    fun getMtntWithLots(tmpId: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeLotsWithBags.removeSource(weighBridgeLotsWithBagsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeLotsWithBagsSource = useCase.getMtntWithLots(tmpId)
        }
        _weighBridgeLotsWithBags.addSource(weighBridgeLotsWithBagsSource) {
            _weighBridgeLotsWithBags.value = it
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

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
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

    /*fun getProduct() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
//            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }*/

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            productList.postValue(useCase.getProducts())
        }
    }

    fun getPurchaseOrderOffline() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            purchaseOrderOffline.postValue(useCase.getPurchaseOrderOffline())
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

    fun saveWeighBridgeDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertTruckInfo(mtnt)
        }
    }

    fun saveLotDetails(lot: VegaNicDispatchLots) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveLotDetails(lot)
        }
    }

    fun saveLotList(lotList: List<VegaNicDispatchLots>) = viewModelScope.launch(dispatchers.io) {
        withContext(dispatchers.io) {
            useCase.saveLotList(lotList)
        }
    }

    fun removeLotFromList(batchNumber: String, tempId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeLotFromList(batchNumber, tempId)
        }
    }

    fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(material)
        }
    }

    fun deleteBagDetails(bagItem: VegaNicaraguaWeighmentBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(bagItem.id)
        }
    }

    fun updateLotDetails() = viewModelScope.launch(dispatchers.io) {
        withContext(dispatchers.io) {
            val lots = lotList.map { it.lots }
            useCase.saveLotList(lots)
        }
    }

    fun postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost: VegaNicMtntDeliveryPost) =
        viewModelScope.launch(dispatchers.main) {
            _weighScaleDeliveryPost.removeSource(weighScaleDeliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                weighScaleDeliveryPostSource = useCase.postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost)
            }
            _weighScaleDeliveryPost.addSource(weighScaleDeliveryPostSource) {
                _weighScaleDeliveryPost.value = it
            }
        }

    fun deleteAllItem(tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteAllItem(tmpWbId)
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

    fun getWeighBridgeDataOnline(material: String): LiveData<Resource<GenericReqAndResp<List<VegaNicTicketListModel>>>> {
        getWeighBridgeDetailOnline(material)
        return weighBridgeOnline
    }

    private fun getWeighBridgeDetailOnline(material: String) =
        viewModelScope.launch(dispatchers.main) {
            _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                weighBridgeOnlineSource = useCase.getWeighBridgeDetailOnline(material)
            }
            _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
                _weighBridgeOnline.value = it
            }
        }

}
