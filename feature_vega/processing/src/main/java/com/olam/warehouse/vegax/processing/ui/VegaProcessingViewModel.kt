package com.olam.warehouse.vegax.processing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingFgrnPost
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingFgrnResponse
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingQualityDetails
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingRminBomPost
import com.olam.warehouse.vegax.processing.data.domain.usecase.VegaProcessingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaProcessingViewModel(private val useCase: VegaProcessingUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {
    //Rmin
    private var gradesSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _grades = MediatorLiveData<List<VegaMaterial>>()
    val grades: LiveData<List<VegaMaterial>> get() = _grades

    private var stageSource: LiveData<List<VegaProcessingStage>> = MutableLiveData()
    private val _stages = MediatorLiveData<List<VegaProcessingStage>>()
    val stages: LiveData<List<VegaProcessingStage>> get() = _stages

    private var bomListSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> = MutableLiveData()
    private val _bomList = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>>()
    val bomList: LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> get() = _bomList

    private var storageLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val storageLocation: LiveData<List<VegaCustomStLocation>> get() = _storageLocation


    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>> get() = _stocks

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaProcessingQualityDetails>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaProcessingQualityDetails>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaProcessingQualityDetails>>>> get() = _qualityDetails

    private var createPoSource: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> = MutableLiveData()
    private val _createPo = MediatorLiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>()
    val createPo: LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> get() = _createPo

    //Fgrn
    private var poDetailListSource: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> =
        MutableLiveData()
    private val _poDetailList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>>()
    val poDetailList: LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> get() = _poDetailList

    private var fgrnGradeSource: LiveData<Resource<GenericReqAndResp<List<VegaFgrnGrades>>>> = MutableLiveData()
    private val _fgrnGradeList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaFgrnGrades>>>>()
    val fgrnGradeList: LiveData<Resource<GenericReqAndResp<List<VegaFgrnGrades>>>> get() = _fgrnGradeList

    private var postFgrnSource: LiveData<Resource<GenericReqAndResp<List<VegaProcessingFgrnResponse>>>> =
        MutableLiveData()
    private val _postFgrn = MediatorLiveData<Resource<GenericReqAndResp<List<VegaProcessingFgrnResponse>>>>()
    val postFgrn: LiveData<Resource<GenericReqAndResp<List<VegaProcessingFgrnResponse>>>> get() = _postFgrn


    fun fetchGrades() = viewModelScope.launch(dispatchers.main) {
        _grades.removeSource(gradesSource)
        withContext(dispatchers.io) {
            gradesSource = useCase.getGrades()
        }
        _grades.addSource(gradesSource) {
            _grades.value = it
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

    fun fetchBomList(bomPostReq: VegaProcessingRminBomPost) =
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

    fun getQualityParams(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }


    fun fetchStorageLocation() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getStorageLocation()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }


    fun createPoRequest(poReq: VegaProcessingCreatePoReq) = viewModelScope.launch(dispatchers.main) {
        _createPo.removeSource(createPoSource)
        withContext(dispatchers.io) {
            createPoSource = useCase.postCreatePo(poReq)
        }
        _createPo.addSource(createPoSource) {
            _createPo.value = it
        }
    }


    fun fetchFgrnPoDetailsList() =
        viewModelScope.launch(dispatchers.main) {
            _poDetailList.removeSource(poDetailListSource)
            withContext(dispatchers.io) {
                poDetailListSource = useCase.getFgrnPoDetailsList()
            }
            _poDetailList.addSource(poDetailListSource) {
                _poDetailList.value = it
            }
        }

    fun fetchFgrnGradeList(poNumber: String) =
        viewModelScope.launch(dispatchers.main) {
            _fgrnGradeList.removeSource(fgrnGradeSource)
            withContext(dispatchers.io) {
                fgrnGradeSource = useCase.getFgrnGrades(poNumber)
            }
            _fgrnGradeList.addSource(fgrnGradeSource) {
                _fgrnGradeList.value = it
            }
        }

    fun postFgrn(poReq: VegaProcessingFgrnPost) = viewModelScope.launch(dispatchers.main) {
        _postFgrn.removeSource(postFgrnSource)
        withContext(dispatchers.io) {
            postFgrnSource = useCase.postFgrnDetails(poReq)
        }
        _postFgrn.addSource(postFgrnSource) {
            _postFgrn.value = it
        }
    }

    fun saveRmin(
        bom: VegaProcessingRminBoms,
        processLots: ProcessingLotDetails,
        stageFevor: String?,
        materialName: String,
        materialNo: String
    ) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.saveRmin(bom, processLots, stageFevor, materialName, materialNo)
            }
        }

    fun updateRminData(batchNo: String?, msg: String, syncStatus: Boolean) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.updateRminData(batchNo, msg, syncStatus)
            }
        }

}
