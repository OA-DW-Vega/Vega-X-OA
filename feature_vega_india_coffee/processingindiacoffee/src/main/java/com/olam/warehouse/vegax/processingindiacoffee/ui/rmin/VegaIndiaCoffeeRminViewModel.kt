package com.olam.warehouse.vegax.processingindiacoffee.ui.rmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaRminItemWithLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminData
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminProcessLotDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaIndiaCoffeeInventoryStocks
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingCreatePoReq
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingRminBomPost
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingRminResponse
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeRminProcessingPost
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.usecase.VegaIndiaCoffeeProcessingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaIndiaCoffeeRminViewModel(
    private val useCase: VegaIndiaCoffeeProcessingUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    // var lotList = ArrayList<VegaCocoaRminLots>()
    val lotList = MutableLiveData<ArrayList<VegaCocoaRminLots>>()

    //Rmin
    private var gradesSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _grades = MediatorLiveData<List<VegaMaterial>>()
    val grades: LiveData<List<VegaMaterial>> get() = _grades

    private var stageSource: LiveData<List<VegaProcessingStage>> = MutableLiveData()
    private val _stages = MediatorLiveData<List<VegaProcessingStage>>()
    val stages: LiveData<List<VegaProcessingStage>> get() = _stages

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var rminSource: LiveData<VegaCocoaRminProcessing> = MutableLiveData()
    private val _rmin = MediatorLiveData<VegaCocoaRminProcessing>()
    val rmin: LiveData<VegaCocoaRminProcessing> get() = _rmin

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var vegaIndiaCoffeeLotsSource: LiveData<List<VegaCocoaRminLots>> = mutableLiveDataOf()
    private val _vegaIndiaCoffeeLotItems = MediatorLiveData<List<VegaCocoaRminLots>>()
    val vegaIndiaCoffeeRminLotItems: LiveData<List<VegaCocoaRminLots>> get() = _vegaIndiaCoffeeLotItems

    private var vegaIndiaCoffeeRminWithLotsSource: LiveData<VegaCocoaRminItemWithLots> = mutableLiveDataOf()
    private val _vegaIndiaCoffeeRminWithItems = MediatorLiveData<VegaCocoaRminItemWithLots>()
    val vegaIndiaCoffeeRminWithItems: LiveData<VegaCocoaRminItemWithLots> get() = _vegaIndiaCoffeeRminWithItems

    private var postRminSource: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeProcessingRminResponse>>>> =
        MutableLiveData()
    private val _postRmin =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeProcessingRminResponse>>>>()
    val postRmin: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeProcessingRminResponse>>>> get() = _postRmin

    private var createPoSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> =
        MutableLiveData()
    private val _createPo = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>()
    val createPo: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> get() = _createPo

    //Fgrn
    private var poDetailListSource: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> =
        MutableLiveData()
    private val _poDetailList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>>()
    val poDetailList: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> get() = _poDetailList

    private var bomListSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> =
        MutableLiveData()
    private val _bomList = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>>()
    val bomList: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> get() = _bomList

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> =
        MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> get() = _stocks

    private var poGradeSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>> =
        MutableLiveData()
    private val _poGradeList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>>()
    val poGradeList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>> get() = _poGradeList


    private var inventoryList =
        MediatorLiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>>()
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> =
        MutableLiveData()
    val inventoryModelList: LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> get() = inventoryList

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product


    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeInventoryStocks>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeInventoryStocks>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeInventoryStocks>>>> get() = _qualityDetails

    private var vendorSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _vendor = MediatorLiveData<List<VegaVendor>>()
    val vendor: LiveData<List<VegaVendor>> get() = _vendor

    fun getVendors() = viewModelScope.launch(dispatchers.main) {
        _vendor.removeSource(vendorSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            vendorSource = useCase.getVendors()
        }
        _vendor.addSource(vendorSource) {
            _vendor.value = it
        }
    }


    fun fetchGrades() = viewModelScope.launch(dispatchers.main) {
        _grades.removeSource(gradesSource)
        withContext(dispatchers.io) {
            gradesSource = useCase.getGrades()
        }
        _grades.addSource(gradesSource) {
            _grades.value = it
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

    fun getRMINProcessModel(cgfNo: String, poNo: String, bom: String, code: String) =
        viewModelScope.launch(dispatchers.main) {
            _rmin.removeSource(rminSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                rminSource = useCase.getRMINProcess(cgfNo, poNo, bom, code)
            }
            _rmin.addSource(rminSource) {
                _rmin.value = it
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

    fun fetchStages() = viewModelScope.launch(dispatchers.main) {
        _stages.removeSource(stageSource)
        withContext(dispatchers.io) {
            stageSource = useCase.getStages()
        }
        _stages.addSource(stageSource) {
            _stages.value = it
        }
    }

    fun fetchFgrnPoDetailsList(
        auart: String,
        cfgNo: String,
        fevor: String
    ) =
        viewModelScope.launch(dispatchers.main) {
            _poDetailList.removeSource(poDetailListSource)
            withContext(dispatchers.io) {
                poDetailListSource = useCase.getFgrnPoDetailsList(auart, cfgNo, fevor)
            }
            _poDetailList.addSource(poDetailListSource) {
                _poDetailList.value = it
            }
        }

    fun fetchBomList(bomPostReq: VegaIndiaCoffeeProcessingRminBomPost) =
        viewModelScope.launch(dispatchers.main) {
            _bomList.removeSource(bomListSource)
            withContext(dispatchers.io) {
                bomListSource = useCase.getBom(bomPostReq)
            }
            _bomList.addSource(bomListSource) {
                _bomList.value = it
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
    fun getStockList(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCase.getStockList(materialList)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun saveWeighBridgeAndLotDetails(list: MutableList<VegaCocoaRminLots>, model: VegaCocoaRminProcessing) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveLotInDispatch(list, model)
            }
        }

    fun deleteLot(batchNo: String, cgfNo: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.deleteLot(batchNo, cgfNo)
            }
        }

    fun deleteAllLot(batchNo: String, cgfNo: String, bom: String, code: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.deleteAllLot(batchNo, cgfNo, bom, code)
            }
        }

    fun getLotList(cgfNo: String, poNo: String, bom: String, code: String) = viewModelScope.launch(dispatchers.main) {
        _vegaIndiaCoffeeLotItems.removeSource(vegaIndiaCoffeeLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            vegaIndiaCoffeeLotsSource = useCase.getLotList(cgfNo, poNo, bom, code)
        }
        _vegaIndiaCoffeeLotItems.addSource(vegaIndiaCoffeeLotsSource) {
            _vegaIndiaCoffeeLotItems.value = it
        }
    }

    fun getRminWithLotList(cgfNo: String, poNo: String, bom: String, code: String) =
        viewModelScope.launch(dispatchers.main) {
            _vegaIndiaCoffeeRminWithItems.removeSource(vegaIndiaCoffeeRminWithLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                vegaIndiaCoffeeRminWithLotsSource = useCase.getRminWithLotList(cgfNo, poNo, bom, code)
            }
            _vegaIndiaCoffeeRminWithItems.addSource(vegaIndiaCoffeeRminWithLotsSource) {
                _vegaIndiaCoffeeRminWithItems.value = it
            }
        }

    fun postRmin(poReq: VegaIndiaCoffeeRminProcessingPost) = viewModelScope.launch(dispatchers.main) {
        _postRmin.removeSource(postRminSource)
        withContext(dispatchers.io) {
            postRminSource = useCase.postRminDetails(poReq)
        }
        _postRmin.addSource(postRminSource) {
            _postRmin.value = it
        }
    }

    fun saveRMINProcess(model: VegaCocoaRminProcessing) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveRMINProcess(model)
        }
    }

    fun getPoGrades(poNo: String, rmin: Boolean) = viewModelScope.launch(dispatchers.main) {
        _poGradeList.removeSource(poGradeSource)
        withContext(dispatchers.io) {
            poGradeSource = useCase.getPoGrades(poNo, rmin)
        }
        _poGradeList.addSource(poGradeSource) {
            _poGradeList.value = it
        }
    }

    fun createPoRequest(poReq: VegaIndiaCoffeeProcessingCreatePoReq) =
        viewModelScope.launch(dispatchers.main) {
            _createPo.removeSource(createPoSource)
            withContext(dispatchers.io) {
                createPoSource = useCase.postCreatePo(poReq)
            }
            _createPo.addSource(createPoSource) {
                _createPo.value = it
            }
        }

    fun getInventoryList(material: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            inventorySource = useCase.getInventoryList(material)
        }
        inventoryList.addSource(inventorySource) {
            inventoryList.value = it
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

    fun saveOfflineRminSelectedLots(rminLotData: VegaNicOfflineRminLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineRminSelectedLots(rminLotData)
            }
        }

    fun saveOfflineRminDetails(rminData: VegaNicOfflineRminData) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineRminDetails(rminData)
            }
        }

    fun saveOfflineRminLotDetails(rminLotData: VegaNicOfflineRminProcessLotDetails) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineRminLotDetails(rminLotData)
            }
        }


}
