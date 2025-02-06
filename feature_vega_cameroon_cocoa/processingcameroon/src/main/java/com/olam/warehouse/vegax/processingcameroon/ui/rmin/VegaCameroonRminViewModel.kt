package com.olam.warehouse.vegax.processingcameroon.ui.rmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesWithBagMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRMINGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingFgrnPost
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingRminResponse
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonRminWeighScalePallet
import com.olam.warehouse.vegax.processingcameroon.data.domain.usecase.VegaCameroonProcessingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCameroonRminViewModel(
    private val useCase: VegaCameroonProcessingUseCase,
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

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var vegaCoffeeLotsSource: LiveData<List<VegaCoffeeRminLots>> = mutableLiveDataOf()
    private val _vegaCoffeeLotItems = MediatorLiveData<List<VegaCoffeeRminLots>>()
    val vegaCoffeeRminLotItems: LiveData<List<VegaCoffeeRminLots>> get() = _vegaCoffeeLotItems


    private var postRminSource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonProcessingRminResponse>>>> =
        MutableLiveData()
    private val _postRmin = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonProcessingRminResponse>>>>()
    val postRmin: LiveData<Resource<GenericReqAndResp<List<VegaCameroonProcessingRminResponse>>>> get() = _postRmin


    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonRminWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonRminWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCameroonRminWeighScalePallet>>>> get() = _pallet

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


    private var offlineRminGradeWithBagSource: LiveData<VegaCoffeeRMINGradesWithBagItems> =
        MutableLiveData()
    private val _offlineRminGradeWithBags = MediatorLiveData<VegaCoffeeRMINGradesWithBagItems>()
    val offlineRminGradeWithBags: LiveData<VegaCoffeeRMINGradesWithBagItems> get() = _offlineRminGradeWithBags

    private var rminBagWithMaterialSource: LiveData<VegaCoffeeFgrnGradesWithBagMatrialWeights> =
        MutableLiveData()
    private val _rminBagWithMaterial = MediatorLiveData<VegaCoffeeFgrnGradesWithBagMatrialWeights>()

    val rminBagWithMaterial: LiveData<VegaCoffeeFgrnGradesWithBagMatrialWeights> get() = _rminBagWithMaterial

    fun getRminGradeWithBags(fgrnId: String, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        _rminBagWithMaterial.removeSource(rminBagWithMaterialSource)
        withContext(dispatchers.io) {
            rminBagWithMaterialSource = useCase.getRminGradeWithBags(fgrnId, batchNo)
        }
        _rminBagWithMaterial.addSource(rminBagWithMaterialSource) {
            _rminBagWithMaterial.value = it
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


    fun deleteLotDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteAllRminItems()
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

    fun updateGradeWeightInfo(weight: String, fgrnId: String, material: String, poNumber: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateWeighToProcess(weight, fgrnId, material, poNumber)
            }
        }

    fun deleteLot(batchNo: String, cgfNo: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.deleteLot(batchNo, cgfNo)
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

    fun postRmin(poReq: VegaCameroonProcessingFgrnPost) = viewModelScope.launch(dispatchers.main) {
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



    fun getRminOfflineGradeWithBags(fgrnIdWithMatrial: String, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                bagWithMaterial.postValue(useCase.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNo))
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


    fun getPalletInfo(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _pallet.removeSource(palletResource)
        withContext(dispatchers.io) {
            palletResource = useCase.getPalletDetails(batchNumber, material)
        }
        _pallet.addSource(palletResource) {
            _pallet.value = it
        }
    }

}
