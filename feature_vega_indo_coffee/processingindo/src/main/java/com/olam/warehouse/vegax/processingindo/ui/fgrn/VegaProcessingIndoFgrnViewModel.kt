package com.olam.warehouse.vegax.processingindo.ui.fgrn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnPost
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.processingindo.data.domain.usecase.VegaProcessingIndoUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */
class VegaProcessingIndoFgrnViewModel(private val useCase: VegaProcessingIndoUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var poDetailListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItems>>>> =
        MutableLiveData()
    private val _poDetailList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItems>>>>()
    val poDetailList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItems>>>> get() = _poDetailList

    private var offlineFgrnSource: LiveData<List<VegaCocoaFgrnItems>> =
        MutableLiveData()
    private val _offlineFgrn = MediatorLiveData<List<VegaCocoaFgrnItems>>()
    val offlineFgrn: LiveData<List<VegaCocoaFgrnItems>> get() = _offlineFgrn

    private var stageSource: LiveData<List<VegaProcessingStage>> = MutableLiveData()
    private val _stages = MediatorLiveData<List<VegaProcessingStage>>()
    val stages: LiveData<List<VegaProcessingStage>> get() = _stages

    private var poGradeSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>> =
        MutableLiveData()
    private val _poGradeList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>>()
    val poGradeList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>> get() = _poGradeList

    private var offlineGradeSource: LiveData<List<VegaCocoaFgrnItemsGrades>> =
        MutableLiveData()
    private val _offlineGradeList = MediatorLiveData<List<VegaCocoaFgrnItemsGrades>>()
    val offlineGradeList: LiveData<List<VegaCocoaFgrnItemsGrades>> get() = _offlineGradeList

    private var offlineGradeWithBagSource: LiveData<VegaCocoaFgrnGradesWithBagItems> =
        MutableLiveData()
    private val _offlineGradeWithBags = MediatorLiveData<VegaCocoaFgrnGradesWithBagItems>()
    val offlineGradeWithBags: LiveData<VegaCocoaFgrnGradesWithBagItems> get() = _offlineGradeWithBags

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> get() = _stocks

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> get() = _stockList

    private var fgrnItemSource: LiveData<VegaCocoaFgrnItemWithGrades> =
        MutableLiveData()
    private val _fgrnItem = MediatorLiveData<VegaCocoaFgrnItemWithGrades>()
    val fgrnItem: LiveData<VegaCocoaFgrnItemWithGrades> get() = _fgrnItem

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var postFgrnSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>> =
        MutableLiveData()
    private val _postFgrn = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>>()
    val postFgrn: LiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>> get() = _postFgrn

    private var fgrnSource: LiveData<VegaCocoaFgrnItems> =
        MutableLiveData()
    private val _fgrn = MediatorLiveData<VegaCocoaFgrnItems>()
    val fgrn: LiveData<VegaCocoaFgrnItems> get() = _fgrn

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

    fun fetchStages() = viewModelScope.launch(dispatchers.main) {
        _stages.removeSource(stageSource)
        withContext(dispatchers.io) {
            stageSource = useCase.getStages()
        }
        _stages.addSource(stageSource) {
            _stages.value = it
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

    fun getPoGrades(poNo: String, rmin: Boolean) = viewModelScope.launch(dispatchers.main) {
        _poGradeList.removeSource(poGradeSource)
        withContext(dispatchers.io) {
            poGradeSource = useCase.getPoGrades(poNo, rmin)
        }
        _poGradeList.addSource(poGradeSource) {
            _poGradeList.value = it
        }
    }

    fun saveFgrnItem(
        fgrnItem: VegaCocoaFgrnItems,
        selectedGrades: List<VegaCocoaFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveFgrnItem(fgrnItem, selectedGrades, removeItem)
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

    fun saveBagDetails(material: VegaCocoaFgrnGradesMatrialWeights, currentGrade: VegaCocoaFgrnItemsGrades) =
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

    fun fetchStocks(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCase.getStocks(material)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
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

    fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCocoaFgrnItemsGrades) = viewModelScope.launch {
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

    fun postFgrn(poReq: VegaCocoaProcessingFgrnPost) = viewModelScope.launch(dispatchers.main) {
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
            useCase.updateBatchToBagDetails(bagMaterialCode, batchNo, bagId)
        }
    }
}
