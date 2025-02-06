package com.olam.warehouse.vegax.processingcameroon.ui.fgrn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingFgrnPost
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingFgrnResponse
import com.olam.warehouse.vegax.processingcameroon.data.domain.usecase.VegaCameroonProcessingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class VegaCameroonFgrnViewModel(
    private val useCase: VegaCameroonProcessingUseCase,
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

    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    private var postFgrnSource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonProcessingFgrnResponse>>>> =
        MutableLiveData()
    private val _postFgrn = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonProcessingFgrnResponse>>>>()
    val postFgrn: LiveData<Resource<GenericReqAndResp<List<VegaCameroonProcessingFgrnResponse>>>> get() = _postFgrn

    private var fgrnSource: LiveData<VegaCoffeeFgrnItems> =
        MutableLiveData()
    private val _fgrn = MediatorLiveData<VegaCoffeeFgrnItems>()
    val fgrn: LiveData<VegaCoffeeFgrnItems> get() = _fgrn

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var featureMasterSource: LiveData<List<VegaFeatureMaster>> = MutableLiveData()
    private val _featureMaster = MediatorLiveData<List<VegaFeatureMaster>>()
    val featureMaster: LiveData<List<VegaFeatureMaster>> get() = _featureMaster

    private var materialsSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _materials = MediatorLiveData<List<VegaMaterial>>()
    val materials: LiveData<List<VegaMaterial>> get() = _materials

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

    fun saveFgrnItemPallet( fgrnItem: VegaCoffeeFgrnItems)= viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveFgrnItemPallet(fgrnItem)
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

    fun postFgrn(poReq: VegaCameroonProcessingFgrnPost) = viewModelScope.launch(dispatchers.main) {
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
    fun getFeatureMaster(module: String) = viewModelScope.launch(dispatchers.main) {
        _featureMaster.removeSource(featureMasterSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            featureMasterSource = useCase.getFeatureMaster(module)
        }
        _featureMaster.addSource(featureMasterSource) {
            _featureMaster.value = it
        }
    }

    fun fetchMaterials() = viewModelScope.launch(dispatchers.main) {
        _materials.removeSource(materialsSource)
        withContext(dispatchers.io) {
            materialsSource = useCase.getGrades()
        }
        _materials.addSource(materialsSource) {
            _materials.value = it
        }
    }


}
