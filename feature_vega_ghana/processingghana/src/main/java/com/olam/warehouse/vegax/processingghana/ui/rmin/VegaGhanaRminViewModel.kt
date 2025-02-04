package com.olam.warehouse.vegax.processingghana.ui.rmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRMINGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminItems
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminLots
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineRmin
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingFgrnPost
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingRminBomPost
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingRminResponse
import com.olam.warehouse.vegax.processingghana.data.domain.usecase.VegaGhanaProcessingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaGhanaRminViewModel(
    private val useCase: VegaGhanaProcessingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    var lotList = ArrayList<VegaCoffeeRminLots>()

    //Rmin
    private var gradesSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _grades = MediatorLiveData<List<VegaMaterial>>()
    val grades: LiveData<List<VegaMaterial>> get() = _grades

    private var lotSource: LiveData<List<VegaCoffeeRminLots>> = mutableLiveDataOf(emptyList())
    private val _lots = MediatorLiveData<List<VegaCoffeeRminLots>>()
    val lots: LiveData<List<VegaCoffeeRminLots>> get() = _lots

    private var stageSource: LiveData<List<VegaProcessingStage>> = MutableLiveData()
    private val _stages = MediatorLiveData<List<VegaProcessingStage>>()
    val stages: LiveData<List<VegaProcessingStage>> get() = _stages

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var rminSource: LiveData<VegaCoffeeRminProcessing> = MutableLiveData()
    private val _rmin = MediatorLiveData<VegaCoffeeRminProcessing>()
    val rmin: LiveData<VegaCoffeeRminProcessing> get() = _rmin

    private var offlineRminSource: LiveData<List<VegaGhanaOfflineRmin>> = MutableLiveData()
    private val _offlineRminItemLocal = MediatorLiveData<List<VegaGhanaOfflineRmin>>()
    val offlineRminItemLocal: LiveData<List<VegaGhanaOfflineRmin>> get() = _offlineRminItemLocal

    private var processOrderSource: LiveData<List<VegaGhanaProcessingOrder>> = MutableLiveData()
    private val _processOrderLocal = MediatorLiveData<List<VegaGhanaProcessingOrder>>()
    val processOrderItemLocal: LiveData<List<VegaGhanaProcessingOrder>> get() = _processOrderLocal

    private var BomOfflineSource: LiveData<List<VegaProcessingRminBoms>> = MutableLiveData()
    private val _BomOfflineLocal = MediatorLiveData<List<VegaProcessingRminBoms>>()
    val BomOfflineLocal: LiveData<List<VegaProcessingRminBoms>> get() = _BomOfflineLocal

    private var processOrderDetailsSource: LiveData<List<VegaGhanaProcessingOrderDetails>> =
        MutableLiveData()
    private val _processOrderDetailsLocal =
        MediatorLiveData<List<VegaGhanaProcessingOrderDetails>>()
    val processOrderDetailsItemLocal: LiveData<List<VegaGhanaProcessingOrderDetails>> get() = _processOrderDetailsLocal

    private var stockDetailsSource: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _stockDetailsLocal = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val stockDetailsItemLocal: LiveData<List<VegaEcuadorDispatchStocks>> get() = _stockDetailsLocal


    private var offlineRminLots: LiveData<List<VegaGhanaOfflineRminLots>> = MutableLiveData()
    private val _offlineRminLots = MediatorLiveData<List<VegaGhanaOfflineRminLots>>()
    val offlineRminLotsLocal: LiveData<List<VegaGhanaOfflineRminLots>> get() = _offlineRminLots

    private var offlineRminItems: LiveData<List<VegaGhanaOfflineRminItems>> = MutableLiveData()
    private val _offlineRminItems = MediatorLiveData<List<VegaGhanaOfflineRminItems>>()
    val offlineRminItemsLocal: LiveData<List<VegaGhanaOfflineRminItems>> get() = _offlineRminItems

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var vegaCoffeeLotsSource: LiveData<List<VegaCoffeeRminLots>> = mutableLiveDataOf()
    private val _vegaCoffeeLotItems = MediatorLiveData<List<VegaCoffeeRminLots>>()
    val vegaCoffeeRminLotItems: LiveData<List<VegaCoffeeRminLots>> get() = _vegaCoffeeLotItems

    private var stockLotSource: LiveData<VegaEcuadorDispatchStocks> = MutableLiveData()
    private val _stockLot = MediatorLiveData<VegaEcuadorDispatchStocks>()
    val stockLot: LiveData<VegaEcuadorDispatchStocks> get() = _stockLot

    private var postRminSource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaProcessingRminResponse>>>> =
        MutableLiveData()
    private val _postRmin = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaProcessingRminResponse>>>>()
    val postRmin: LiveData<Resource<GenericReqAndResp<List<VegaGhanaProcessingRminResponse>>>> get() = _postRmin

    //Fgrn
    private var poDetailListSource: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> =
        MutableLiveData()
    private val _poDetailList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>>()
    val poDetailList: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> get() = _poDetailList


    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> get() = _stocks

    private var poGradeSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>> =
        MutableLiveData()
    private val _poGradeList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>>()
    val poGradeList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>> get() = _poGradeList

    val rminItem = MutableLiveData<VegaCoffeeRminItemWithGrades>()

    private var rminItemSource: LiveData<VegaCoffeeRminItemWithGrades> =
        MutableLiveData()
    private val _rminItem = MediatorLiveData<VegaCoffeeRminItemWithGrades>()
    val rminItemData: LiveData<VegaCoffeeRminItemWithGrades> get() = _rminItem


    val validateLot = MutableLiveData<VegaCoffeeRminLots>()
    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    val rminModel = MutableLiveData<VegaCoffeeRminProcessing>()
    val bagWithMaterial = MutableLiveData<List<VegaCoffeeFgrnGradesMatrialWeights>>()

    val lotsInfoForMaterial = MutableLiveData<List<VegaCoffeeRminLots>>()

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> get() = _qualityDetails

    private var offlineGradeSource: LiveData<List<VegaCoffeeFgrnItemsGrades>> =
        MutableLiveData()
    private val _offlineGradeList = MediatorLiveData<List<VegaCoffeeFgrnItemsGrades>>()
    val offlineGradeList: LiveData<List<VegaCoffeeFgrnItemsGrades>> get() = _offlineGradeList

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var offlineRminGradeWithBagSource: LiveData<VegaCoffeeRMINGradesWithBagItems> =
        MutableLiveData()
    private val _offlineRminGradeWithBags = MediatorLiveData<VegaCoffeeRMINGradesWithBagItems>()
    val offlineRminGradeWithBags: LiveData<VegaCoffeeRMINGradesWithBagItems> get() = _offlineRminGradeWithBags

    //bom
    private var bomListSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> =
        MutableLiveData()
    private val _bomList = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>>()
    val bomList: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> get() = _bomList

    private var createPoSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> =
        MutableLiveData()
    private val _createPo = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>()
    val createPo: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> get() = _createPo

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

    fun getOfflineRminItem() = viewModelScope.launch(dispatchers.main) {
        _offlineRminItemLocal.removeSource(offlineRminSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineRminSource = useCase.getOfflineRminItem()
        }
        _offlineRminItemLocal.addSource(offlineRminSource) {
            _offlineRminItemLocal.value = it
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

    fun getBomsOffline() = viewModelScope.launch(dispatchers.main) {
        _processOrderLocal.removeSource(processOrderSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            processOrderSource = useCase.getProcessOrders()
        }
        _processOrderLocal.addSource(processOrderSource) {
            _processOrderLocal.value = it
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

    fun getStockDetails(material: String) = viewModelScope.launch(dispatchers.main) {
        _stockDetailsLocal.removeSource(stockDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            stockDetailsSource = useCase.getStockDetails(material)
        }
        _stockDetailsLocal.addSource(stockDetailsSource) {
            _stockDetailsLocal.value = it
        }
    }

    fun getOfflineRminLots() = viewModelScope.launch(dispatchers.main) {
        _offlineRminLots.removeSource(offlineRminLots) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineRminLots = useCase.getOfflineRminLots()
        }
        _offlineRminLots.addSource(offlineRminLots) {
            _offlineRminLots.value = it
        }
    }

    fun getOfflineRminItems() = viewModelScope.launch(dispatchers.main) {
        _offlineRminItems.removeSource(offlineRminItems) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineRminItems = useCase.getOfflineRminItems()
        }
        _offlineRminItems.addSource(offlineRminItems) {
            _offlineRminItems.value = it
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

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
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

    fun fetchStocks(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCase.getStocks(material)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun saveWeighBridgeAndLotDetails(list: MutableList<VegaCoffeeRminLots>, model: VegaCoffeeRminProcessing) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveLotInDispatch(list, model)
            }
        }

    fun saveOfflineRminDetails(rminData: VegaGhanaOfflineRminData) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineRminDetails(rminData)
            }
        }

    fun saveOfflineRminLotDetails(rminLotData: VegaGhanaOfflineRminProcessLotDetails) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineRminLotDetails(rminLotData)
            }
        }

    fun saveOfflineRminSelectedLots(rminLotData: VegaGhanaOfflineRminLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineRminSelectedLots(rminLotData)
            }
        }

    fun saveOfflineRminItem(rminLotData: VegaGhanaOfflineRminItems) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveOfflineRminItem(rminLotData)
            }
        }

    fun updateGradeWeightInfo(weight: String, fgrnId: String, material: String, poNumber: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateWeighToProcess(weight, fgrnId, material, poNumber)
            }
        }

    fun updateStockDetails(weight: String, batchNo: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateStockDetails(weight, batchNo)
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

    fun updateSyncStatus(model: VegaCoffeeRminProcessing) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateAllSyncStatus(model, model.lotList ?: ArrayList())
            }
        }

    fun getLotList(poNo: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _vegaCoffeeLotItems.removeSource(vegaCoffeeLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            vegaCoffeeLotsSource = useCase.getLotList(poNo, material)
        }
        _vegaCoffeeLotItems.addSource(vegaCoffeeLotsSource) {
            _vegaCoffeeLotItems.value = it
        }
    }

    fun getLotInfo(poNo: String, material: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            lotsInfoForMaterial.postValue(useCase.getLots(poNo, material))
        }
    }

    fun getAllLotInfo(poNo: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _postRmin.removeSource(lotSource)
        withContext(dispatchers.io) {
            lotSource = useCase.getAllLots(poNo, material)
        }
        _lots.addSource(lotSource) {
            _lots.value = it
        }
    }

    fun postRmin(poReq: VegaGhanaProcessingFgrnPost) = viewModelScope.launch(dispatchers.main) {
        _postRmin.removeSource(postRminSource)
        withContext(dispatchers.io) {
            postRminSource = useCase.postRminDetails(poReq)
        }
        _postRmin.addSource(postRminSource) {
            _postRmin.value = it
        }
    }

    fun saveRMINProcess(model: VegaCoffeeRminProcessing) = viewModelScope.launch {
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


    fun getOfflineGradeWithBags(fgrnIdWithMatrial: String) = viewModelScope.launch(dispatchers.main) {
        _offlineRminGradeWithBags.removeSource(offlineRminGradeWithBagSource)
        withContext(dispatchers.io) {
            offlineRminGradeWithBagSource = useCase.getOfflineRminLotsWithBags(fgrnIdWithMatrial, "")
        }
        _offlineRminGradeWithBags.addSource(offlineRminGradeWithBagSource) {
            _offlineRminGradeWithBags.value = it
        }
    }


    /*private var offlineRminGradeWithBagSource: LiveData<List<VegaCoffeeFgrnGradesMatrialWeights>> =
        MutableLiveData()
    private val _offlineRminGradeWithBags = MediatorLiveData<List<VegaCoffeeFgrnGradesMatrialWeights>>()
    val offlineRminGradeWithBags: LiveData<List<VegaCoffeeFgrnGradesMatrialWeights>> get() = _offlineRminGradeWithBags*/

    fun getRminOfflineGradeWithBags(fgrnIdWithMatrial: String, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                bagWithMaterial.postValue(useCase.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNo))
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

    fun updateDeletedItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(tmpWbId)
        }
    }

    fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveFgrnGrade(vegaCocoaFgrnItemsGrades)
        }
    }

    fun saveBagDetails(material: VegaCoffeeFgrnGradesMatrialWeights, currentGrade: VegaCoffeeFgrnItemsGrades) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveBagDetails(material, currentGrade)
            }
        }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id)
        }
    }

    fun deleteLotDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteAllRminItems()
        }
    }

    fun deleteAllRminLots() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteAllRminLots()
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
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

    fun saveRMINItem(
        fgrnItem: VegaCoffeeRminProcessing,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveRminItem(fgrnItem, selectedGrades, removeItem)
        }
    }

    fun getOfflineGrades(poNo: String) = viewModelScope.launch(dispatchers.main) {
        _offlineGradeList.removeSource(offlineGradeSource)
        withContext(dispatchers.io) {
            offlineGradeSource = useCase.getOfflineGrades(poNo)
        }
        _offlineGradeList.addSource(offlineGradeSource) {
            _offlineGradeList.value = it
        }
    }

    fun saveLotDetails(list: VegaCoffeeRminLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveLot(list)
            }
        }

    fun getRminItems(fgrnId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            rminItem.postValue(useCase.getRminItems(fgrnId))
        }
    }

    fun getRminLive(rminId: String) = viewModelScope.launch(dispatchers.main) {
        _rminItem.removeSource(rminItemSource)
        withContext(dispatchers.io) {
            rminItemSource = useCase.getRminItemLive(rminId)
        }
        _rminItem.addSource(rminItemSource) {
            _rminItem.value = it
        }
    }

    fun getRminData(poNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            rminModel.postValue(useCase.getProcessingModel(poNumber))
        }
    }

    fun fetchBomList(bomPostReq: VegaGhanaProcessingRminBomPost) =
        viewModelScope.launch(dispatchers.main) {
            _bomList.removeSource(bomListSource)
            withContext(dispatchers.io) {
                bomListSource = useCase.getBom(bomPostReq)
            }
            _bomList.addSource(bomListSource) {
                _bomList.value = it
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

    fun getbomoffline(materialcode: String) = viewModelScope.launch(dispatchers.main) {
        _BomOfflineLocal.removeSource(poGradeSource)
        withContext(dispatchers.io) {
            BomOfflineSource = useCase.getBomoffline(materialcode)
        }
        _BomOfflineLocal.addSource(BomOfflineSource) {
            _BomOfflineLocal.value = it
        }
    }
}
