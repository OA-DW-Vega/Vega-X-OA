package com.olam.warehouse.vegax.processingcocoa.ui.rmin

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
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoProcessingRminBomPost
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaProcessingRminResponse
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaRminProcessingPost
import com.olam.warehouse.vegax.processingcocoa.data.domain.usecase.VegaCocoaProcessingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */
class VegaCocoaRminViewModel(private val useCase: VegaCocoaProcessingUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

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

    private var vegaCocoaLotsSource: LiveData<List<VegaCocoaRminLots>> = mutableLiveDataOf()
    private val _vegaCocoaLotItems = MediatorLiveData<List<VegaCocoaRminLots>>()
    val vegaCocoaRminLotItems: LiveData<List<VegaCocoaRminLots>> get() = _vegaCocoaLotItems

    private var vegaCocoaRminWithLotsSource: LiveData<VegaCocoaRminItemWithLots> = mutableLiveDataOf()
    private val _vegaCocoaRminWithItems = MediatorLiveData<VegaCocoaRminItemWithLots>()
    val vegaCocoaRminWithItems: LiveData<VegaCocoaRminItemWithLots> get() = _vegaCocoaRminWithItems

    private var postRminSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingRminResponse>>>> =
        MutableLiveData()
    private val _postRmin = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingRminResponse>>>>()
    val postRmin: LiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingRminResponse>>>> get() = _postRmin

    private var createPoSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> = MutableLiveData()
    private val _createPo = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>()
    val createPo: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> get() = _createPo

    //Fgrn
    private var poDetailListSource: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> =
        MutableLiveData()
    private val _poDetailList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>>()
    val poDetailList: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> get() = _poDetailList

    private var bomListSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> = MutableLiveData()
    private val _bomList = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>>()
    val bomList: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> get() = _bomList

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> get() = _stocks

    private var poGradeSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>> =
        MutableLiveData()
    private val _poGradeList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>>()
    val poGradeList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>> get() = _poGradeList

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

    fun fetchBomList(bomPostReq: VegaCocoProcessingRminBomPost) =
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
        _vegaCocoaLotItems.removeSource(vegaCocoaLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            vegaCocoaLotsSource = useCase.getLotList(cgfNo, poNo, bom, code)
        }
        _vegaCocoaLotItems.addSource(vegaCocoaLotsSource) {
            _vegaCocoaLotItems.value = it
        }
    }

    fun getRminWithLotList(cgfNo: String, poNo: String, bom: String, code: String) =
        viewModelScope.launch(dispatchers.main) {
            _vegaCocoaRminWithItems.removeSource(vegaCocoaRminWithLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                vegaCocoaRminWithLotsSource = useCase.getRminWithLotList(cgfNo, poNo, bom, code)
            }
            _vegaCocoaRminWithItems.addSource(vegaCocoaRminWithLotsSource) {
                _vegaCocoaRminWithItems.value = it
            }
        }

    fun postRmin(poReq: VegaCocoaRminProcessingPost) = viewModelScope.launch(dispatchers.main) {
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

    fun createPoRequest(poReq: VegaCocoaProcessingCreatePoReq) = viewModelScope.launch(dispatchers.main) {
        _createPo.removeSource(createPoSource)
        withContext(dispatchers.io) {
            createPoSource = useCase.postCreatePo(poReq)
        }
        _createPo.addSource(createPoSource) {
            _createPo.value = it
        }
    }
}
