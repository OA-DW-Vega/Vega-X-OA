package com.olam.warehouse.vegax.processingghana.ui.fgrn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineFgrn
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineRmin
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingFgrnPost
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingFgrnResponse
import com.olam.warehouse.vegax.processingghana.data.domain.usecase.VegaGhanaProcessingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class VegaGhanaFgrnViewModel(
    private val useCase: VegaGhanaProcessingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    private var poDetailListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItems>>>> =
        MutableLiveData()
    private val _poDetailList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItems>>>>()
    val poDetailList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItems>>>> get() = _poDetailList

    private var offlineFgrnSource: LiveData<List<VegaCoffeeFgrnItems>> =
        MutableLiveData()
    private val _offlineFgrn = MediatorLiveData<List<VegaCoffeeFgrnItems>>()
    val offlineFgrn: LiveData<List<VegaCoffeeFgrnItems>> get() = _offlineFgrn

    private var stageSource: LiveData<List<VegaProcessingStage>> = MutableLiveData()
    private val _stages = MediatorLiveData<List<VegaProcessingStage>>()
    val stages: LiveData<List<VegaProcessingStage>> get() = _stages

    private var stockDetailsSource: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _stockDetailsLocal = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val stockDetailsItemLocal: LiveData<List<VegaEcuadorDispatchStocks>> get() = _stockDetailsLocal

    private var poGradeSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>> =
        MutableLiveData()
    private val _poGradeList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>>()
    val poGradeList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>> get() = _poGradeList

    private var offlineGradeSource: LiveData<List<VegaCoffeeFgrnItemsGrades>> =
        MutableLiveData()
    private val _offlineGradeList = MediatorLiveData<List<VegaCoffeeFgrnItemsGrades>>()
    val offlineGradeList: LiveData<List<VegaCoffeeFgrnItemsGrades>> get() = _offlineGradeList

    private var offlineGradeWithBagSource: LiveData<VegaCoffeeFgrnGradesWithBagItems> =
        MutableLiveData()
    private val _offlineGradeWithBags = MediatorLiveData<VegaCoffeeFgrnGradesWithBagItems>()
    val offlineGradeWithBags: LiveData<VegaCoffeeFgrnGradesWithBagItems> get() = _offlineGradeWithBags

    private var bagListSource: LiveData<VegaCoffeeFgrnGradesMatrialWeights> = MutableLiveData()
    private val _bagList = MediatorLiveData<VegaCoffeeFgrnGradesMatrialWeights>()
    val bagList: LiveData<VegaCoffeeFgrnGradesMatrialWeights> get() = _bagList

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> get() = _stocks

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> get() = _stockList

    private var fgrnItemSource: LiveData<VegaCoffeeFgrnItemWithGrades> =
        MutableLiveData()
    private val _fgrnItem = MediatorLiveData<VegaCoffeeFgrnItemWithGrades>()
    val fgrnItem: LiveData<VegaCoffeeFgrnItemWithGrades> get() = _fgrnItem

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var stockLotSource: LiveData<VegaEcuadorDispatchStocks> = MutableLiveData()
    private val _stockLot = MediatorLiveData<VegaEcuadorDispatchStocks>()
    val stockLot: LiveData<VegaEcuadorDispatchStocks> get() = _stockLot

    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var offlineFgrnSources: LiveData<List<VegaGhanaOfflineFgrn>> = MutableLiveData()
    private val _offlineFgrnItemLocal = MediatorLiveData<List<VegaGhanaOfflineFgrn>>()
    val offlineFgrnItemLocal: LiveData<List<VegaGhanaOfflineFgrn>> get() = _offlineFgrnItemLocal

    private var fgrnSources: LiveData<List<VegaGhanaOfflineFgrnData>> = MutableLiveData()
    private val _fgrnItemLocal = MediatorLiveData<List<VegaGhanaOfflineFgrnData>>()
    val fgrnItemLocal: LiveData<List<VegaGhanaOfflineFgrnData>> get() = _fgrnItemLocal

    private var offlineRminSources: LiveData<List<VegaGhanaOfflineRminProcessLotDetails>> = MutableLiveData()
    private val _rminItemLocal = MediatorLiveData<List<VegaGhanaOfflineRminProcessLotDetails>>()
    val rminItemLocal: LiveData<List<VegaGhanaOfflineRminProcessLotDetails>> get() = _rminItemLocal

    private var postFgrnSource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaProcessingFgrnResponse>>>> =
        MutableLiveData()
    private val _postFgrn = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaProcessingFgrnResponse>>>>()
    val postFgrn: LiveData<Resource<GenericReqAndResp<List<VegaGhanaProcessingFgrnResponse>>>> get() = _postFgrn

    private var fgrnSource: LiveData<VegaCoffeeFgrnItems> =
        MutableLiveData()
    private val _fgrn = MediatorLiveData<VegaCoffeeFgrnItems>()
    val fgrn: LiveData<VegaCoffeeFgrnItems> get() = _fgrn

    private var offlineRminSource: LiveData<List<VegaGhanaOfflineRmin>> = MutableLiveData()
    private val _offlineRminItemLocal = MediatorLiveData<List<VegaGhanaOfflineRmin>>()
    val offlineRminItemLocal: LiveData<List<VegaGhanaOfflineRmin>> get() = _offlineRminItemLocal

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var processOrderDetailsSource: LiveData<List<VegaGhanaProcessingOrderDetails>> = MutableLiveData()
    private val _processOrderDetailsLocal = MediatorLiveData<List<VegaGhanaProcessingOrderDetails>>()
    val processOrderDetailsItemLocal: LiveData<List<VegaGhanaProcessingOrderDetails>> get() = _processOrderDetailsLocal


    private var processOrderSource: LiveData<List<VegaGhanaProcessingOrder>> = MutableLiveData()
    private val _processOrderLocal = MediatorLiveData<List<VegaGhanaProcessingOrder>>()
    val processOrderItemLocal: LiveData<List<VegaGhanaProcessingOrder>> get() = _processOrderLocal

    private var poDetailListSourceFgrn: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> =
        MutableLiveData()
    private val _poDetailListFgrn =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>>()
    val poDetailListFgrn: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> get() = _poDetailListFgrn

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var createPoSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> =
        MutableLiveData()
    private val _createPo = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>()
    val createPo: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> get() = _createPo

    private var vegaOfflineRMINsource: LiveData<List<VegaGhanaOfflineRminData>> =
        mutableLiveDataOf()
    private val _vegaOfflineRMINItems = MediatorLiveData<List<VegaGhanaOfflineRminData>>()
    val vegaOfflineRMINItems: LiveData<List<VegaGhanaOfflineRminData>> get() = _vegaOfflineRMINItems

    fun fetchFgrnPoDetailsList(stageFevor: String, cfgNo: String, auart: String) =
        viewModelScope.launch(dispatchers.main) {
            _poDetailList.removeSource(poDetailListSource)
            withContext(dispatchers.io) {
                poDetailListSource = useCase.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)
            }
            _poDetailList.addSource(poDetailListSource) {
                _poDetailList.value = it
            }
        }

    fun fetchFgrnPoDetailsListSummary(
        auart: String,
        cfgNo: String,
        fevor: String
    ) =
        viewModelScope.launch(dispatchers.main) {
            _poDetailListFgrn.removeSource(poDetailListSource)
            withContext(dispatchers.io) {
                poDetailListSourceFgrn = useCase.getFgrnPoDetailsList(auart, cfgNo, fevor)
            }
            _poDetailListFgrn.addSource(poDetailListSourceFgrn) {
                _poDetailListFgrn.value = it
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

    fun getProcessOrderDetails(poNumber: String) = viewModelScope.launch(dispatchers.main) {
        _processOrderDetailsLocal.removeSource(processOrderDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            processOrderDetailsSource = useCase.getProcessOrderDetails(poNumber)
        }
        _processOrderDetailsLocal.addSource(processOrderDetailsSource) {
            _processOrderDetailsLocal.value = it
        }
    }

    fun fetchOfflineFgrnList(processOrderNo: String) =
        viewModelScope.launch(dispatchers.main) {
            _offlineFgrn.removeSource(offlineFgrnSource)
            withContext(dispatchers.io) {
                offlineFgrnSource = useCase.fetchOfflineFgrnList(processOrderNo)
            }
            _offlineFgrn.addSource(offlineFgrnSource) {
                _offlineFgrn.value = it
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

    fun getPoGrades(poNo: String, rmin: Boolean) = viewModelScope.launch(dispatchers.main) {
        _poGradeList.removeSource(poGradeSource)
        withContext(dispatchers.io) {
            poGradeSource = useCase.getPoGrades(poNo, rmin)
        }
        _poGradeList.addSource(poGradeSource) {
            _poGradeList.value = it
        }
    }


    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }

    fun saveFgrnItem(
        fgrnItem: VegaCoffeeFgrnItems,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveFgrnItem(fgrnItem, selectedGrades, removeItem)
        }
    }

    fun saveRMINItem(
        fgrnItem: VegaCoffeeRminProcessing,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            //useCase.saveFgrnItem(fgrnItem, selectedGrades, removeItem)
        }
    }

    fun getOfflineGrades(poNo: String, fgrnId: String) = viewModelScope.launch(dispatchers.main) {
        _offlineGradeList.removeSource(offlineGradeSource)
        withContext(dispatchers.io) {
            offlineGradeSource = useCase.getOfflineGrades(poNo, fgrnId)
        }
        _offlineGradeList.addSource(offlineGradeSource) {
            _offlineGradeList.value = it
        }
    }

    fun getOfflineGradeWithBags(fgrnIdWithMatrial: String) = viewModelScope.launch(dispatchers.main) {
        _offlineGradeWithBags.removeSource(offlineGradeWithBagSource)
        withContext(dispatchers.io) {
            offlineGradeWithBagSource = useCase.getOfflineGradeWithBags(fgrnIdWithMatrial)
        }
        _offlineGradeWithBags.addSource(offlineGradeWithBagSource) {
            _offlineGradeWithBags.value = it
        }
    }

    fun getOfflineRminItem() = viewModelScope.launch(dispatchers.main) {
        _offlineRminItemLocal.removeSource(offlineRminSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineRminSource = useCase.getOfflineRminItem()
        }
        _offlineRminItemLocal.addSource(offlineRminSource) {
            _offlineRminItemLocal.value = it
        }
    }

    fun getAllFGRNItems() = viewModelScope.launch(dispatchers.main) {
        _bagList.removeSource(bagListSource)
        withContext(dispatchers.io) {
            bagListSource = useCase.getAllFGRNItems()
        }
        _bagList.addSource(bagListSource) {
            _bagList.value = it
        }
    }

    fun saveBagDetails(material: VegaCoffeeFgrnGradesMatrialWeights, currentGrade: VegaCoffeeFgrnItemsGrades) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveBagDetails(material, currentGrade)
            }
        }

    fun deleteLotDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteAllRminItems()
        }
    }

    fun deleteAllFgrnItems() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteAllFgrnItems()
        }
    }

    fun updateFgrnStatus(status: Boolean, rminId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateFgrnStatus(status, rminId)
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id)
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

    fun getStockDetails(material: String) = viewModelScope.launch(dispatchers.main) {
        _stockDetailsLocal.removeSource(stockDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            stockDetailsSource = useCase.getStockDetails(material)
        }
        _stockDetailsLocal.addSource(stockDetailsSource) {
            _stockDetailsLocal.value = it
        }
    }

    fun deleteItemInAllTable(fgrnId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteItemInAllTable(fgrnId)
        }
    }

    fun getFgrnItems(fgrnId: String) = viewModelScope.launch(dispatchers.main) {
        _fgrnItem.removeSource(fgrnItemSource)
        withContext(dispatchers.io) {
            fgrnItemSource = useCase.getFgrnItems(fgrnId)
        }
        _fgrnItem.addSource(fgrnItemSource) {
            _fgrnItem.value = it
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

    fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveFgrnGrade(vegaCocoaFgrnItemsGrades)
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getStockDetailsByBatchNo(batchNo: String) = viewModelScope.launch(dispatchers.main) {
        _stockLot.removeSource(stockLotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            stockLotSource = useCase.getStockDetailsByBatchNo(batchNo)
        }
        _stockLot.addSource(stockLotSource) {
            _stockLot.value = it
        }
    }

    fun postFgrn(poReq: VegaGhanaProcessingFgrnPost) = viewModelScope.launch(dispatchers.main) {
        _postFgrn.removeSource(postFgrnSource)
        withContext(dispatchers.io) {
            postFgrnSource = useCase.postFgrnDetails(poReq)
        }
        _postFgrn.addSource(postFgrnSource) {
            _postFgrn.value = it
        }
    }

    fun updateFgrnStatus(message: String, status: Int, fgrnId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateFgrnStatus(message, status, fgrnId)
        }
    }

    fun updateFgrnShiftStatus(fgrnId: String, shiftSelection: String, operatorName: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateFgrnShiftStatus(fgrnId, shiftSelection, operatorName)
        }
    }

    fun getProcessOrders() = viewModelScope.launch(dispatchers.main) {
        _processOrderLocal.removeSource(processOrderSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            processOrderSource = useCase.getProcessOrders()
        }
        _processOrderLocal.addSource(processOrderSource) {
            _processOrderLocal.value = it
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

    fun saveOfflineFgrnDetails(fgrnData: VegaGhanaOfflineFgrnData) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineFgrnDetails(fgrnData)
            }
        }

    fun saveOfflineFgrnLotDetails(fgrnLotData: VegaGhanaOfflineFgrnProcessLotDetails) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineFgrnLotDetails(fgrnLotData)
            }
        }

    fun getOfflineFgrnItem() = viewModelScope.launch(dispatchers.main) {
        _offlineFgrnItemLocal.removeSource(offlineFgrnSources) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineFgrnSources = useCase.getofflineFgrnItem()
        }
        _offlineFgrnItemLocal.addSource(offlineFgrnSources) {
            _offlineFgrnItemLocal.value = it
        }
    }

    fun getofflineFgrnPostItem() = viewModelScope.launch(dispatchers.main) {
        _fgrnItemLocal.removeSource(fgrnSources) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            fgrnSources = useCase.getofflineFgrnPostItem()
        }
        _fgrnItemLocal.addSource(fgrnSources) {
            _fgrnItemLocal.value = it
        }
    }

    fun getofflineRminPostItem(rmin: String) = viewModelScope.launch(dispatchers.main) {
        _rminItemLocal.removeSource(offlineRminSources) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineRminSources = useCase.getofflineRminPostItem(rmin)
        }
        _rminItemLocal.addSource(offlineRminSources) {
            _rminItemLocal.value = it
        }
    }

    fun updateDeletedItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateFgrnDeletedItem(tmpWbId)
        }
    }

    fun updateRminDeletedItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(tmpWbId)
        }
    }

    fun updateSyncedFgrnDeletedItem(status: Int) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateSyncedFgrnDeletedItem(status)
        }
    }

    fun updateStockDetails(weight: String, batchNo: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateStockDetails(weight, batchNo)
            }
        }

    fun updateFgrnTotal(total: String, poNo: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateFgrnTotal(total, poNo)
            }
        }

    fun fetchFgrnItem(fgrnId: String) =
        viewModelScope.launch(dispatchers.main) {
            _fgrn.removeSource(fgrnSource)
            withContext(dispatchers.io) {
                fgrnSource = useCase.fetchFgrnItem(fgrnId)
            }
            _fgrn.addSource(fgrnSource) {
                _fgrn.value = it
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

    fun updateBatchToBagDetails(bagMaterialCode: String, batchNo: String, bagId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateBatchToBagDetails(bagMaterialCode, batchNo, bagId, true)
        }
    }

    fun createBatchFormat(vegaMaterial: VegaCoffeeThirdPartyMaterialDetail): String {
        val pile = "NP"
        var lotId = ""
        val materialName = vegaMaterial.materialName.toString()
        val rightNow = Calendar.getInstance()
        val day = rightNow.get(Calendar.DATE)
        val year = rightNow.get(Calendar.YEAR).toString().substring(2)
        val month = rightNow.get(Calendar.MONTH).plus(1)
        var monthValue = if (month.toString().length == 1) "0".plus(month.toString()) else month.toString()
        var dayValue = if (day.toString().length == 1) "0".plus(day.toString()) else day.toString()

        if (vegaMaterial.typeCode.toString().contains("export", true)) {
            lotId = pile + dayValue + monthValue
        } else if (vegaMaterial.typeCode.toString().contains("bush", true)) {
            lotId = lotId + dayValue + monthValue + year
        } else {
            var type = "O"
            type = vegaMaterial.typeCode.toString()
            /*if (materialName.contains("cherry", true)) {
                type = "C"
            }
            if (materialName.contains("rejects", true)) {
                type = "R"
            }
            if (materialName.contains("micro broken", true)) {
                type = "M"
            }
            if (materialName.contains("stone", true)) {
                type = "S"
            }
            if (materialName.contains("dust", true)) {
                type = "P"
            }
            if (materialName.contains("hulled coffee", true)) {
                type = "D"
            }
            if (materialName.contains("ficelles", true)) {
                type = "F"
            }*/

            lotId = pile + dayValue + monthValue + type
        }
        // return "NP".plus(year.toString().plus(monthValue))
        return lotId
    }

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getCoffeeProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun createPoRequest(poReq: VegaGhanaProcessingCreatePoReq) =
        viewModelScope.launch(dispatchers.main) {
            _createPo.removeSource(createPoSource)
            withContext(dispatchers.io) {
                createPoSource = useCase.postCreatePo(poReq)
            }
            _createPo.addSource(createPoSource) {
                _createPo.value = it
            }
        }

    fun getofflineRMINVersionID(RminTempId: String) = viewModelScope.launch(dispatchers.main) {
        _vegaOfflineRMINItems.removeSource(vegaOfflineRMINsource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            vegaOfflineRMINsource = useCase.getofflineRmindata(RminTempId)
        }
        _vegaOfflineRMINItems.addSource(vegaOfflineRMINsource) {
            _vegaOfflineRMINItems.value = it
        }
    }
}
