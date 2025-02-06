package com.olam.warehouse.vegax.qualityofanylot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.model.VegaQualityPostLot
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListData
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListResponse
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostResponse
import com.olam.warehouse.vegax.qualityofanylot.data.domain.usecase.VegaAnyLotQualityUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaAnyLotQualityViewModel (
    private val useCase: VegaAnyLotQualityUsecase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

      var lotPosition=-1

    private var lotListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _lotList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val lotList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lotList

    private var qualityByIdSource: LiveData<Resource<GenericReqAndResp<VegaAnyLotListData>>> = MutableLiveData()
    private val _qualityById = MediatorLiveData<Resource<GenericReqAndResp<VegaAnyLotListData>>>()
    val qualityById: LiveData<Resource<GenericReqAndResp<VegaAnyLotListData>>> get() = _qualityById

    private var qualityPostSource: LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> = MutableLiveData()
    private val _qualityPost = MediatorLiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>>()
    val qualityPost: LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> get() = _qualityPost

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> =
        MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var allStockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _allStockList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val allStockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _allStockList

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var savedQualityListSource:LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> = MutableLiveData()
    private val _savedQualityList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>>()
    val savedQualityLotList:LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> get()= _savedQualityList

    private var deleteTransactionSource:LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> = MutableLiveData()
    private val _deleteTransaction = MediatorLiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>>()
    val deleteTransaction:LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> get()= _deleteTransaction

    private var qualityBatchSource: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> =
        MutableLiveData()
    private val _qualityBatch =
        MediatorLiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>>()
    val qualityBatch: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> get() = _qualityBatch

    fun deleteTransactionListItem(id: String) = viewModelScope.launch(dispatchers.main) {
        _deleteTransaction.removeSource(deleteTransactionSource)
        withContext(dispatchers.io){
            deleteTransactionSource= useCase.deleteTransactionItem(id)
        }
        _deleteTransaction.addSource(deleteTransactionSource){
            _deleteTransaction.value= it
        }
    }


    fun getAllSavedQualityLotList(plant: String,batchNo: String) = viewModelScope.launch(dispatchers.main) {
        _savedQualityList.removeSource(savedQualityListSource)
        withContext(dispatchers.io){
            savedQualityListSource= useCase.getQualityLot(plant,batchNo)
        }
        _savedQualityList.addSource(savedQualityListSource){
            _savedQualityList.value= it
        }
    }


    fun getAllStockList() = viewModelScope.launch(dispatchers.main) {
        _allStockList.removeSource(allStockListSource)
        withContext(dispatchers.io) {
            allStockListSource = useCase.getAllStockByPlant()
        }
        _allStockList.addSource(allStockListSource) {
            _allStockList.value = it
        }
    }


    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityListSource = useCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityListSource) {
                _qualitylist.value = it
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



    fun postOrSaveQualityDetails(request: VegaAnyLotQualityPostRequest) = viewModelScope.launch(dispatchers.main) {
        _qualityPost.removeSource(qualityPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPostSource = useCase.saveOrPostQualityDetails(request)
        }
        _qualityPost.addSource(qualityPostSource) {
            _qualityPost.value = it
        }
    }

    fun getQualityById(key:String,id:String) = viewModelScope.launch(dispatchers.main) {
        _qualityById.removeSource(qualityByIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityByIdSource = useCase.getQualityId(key, id)
        }
        _qualityById.addSource(qualityByIdSource) {
            _qualityById.value = it
        }
    }


    fun getPreSamplingQualityData(batchNo: String, materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
            }
            _preSamplingQuality.addSource(qualityPreSamplingSource) {
                _preSamplingQuality.value = it
            }
        }

    fun getLotDetails(charge: String, material: String, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotList.removeSource(lotListSource)
            withContext(dispatchers.io) {
                lotListSource = useCase.getValidLotsList(charge, material, whId)
            }
            _lotList.addSource(lotListSource) {
                _lotList.value = it
            }
        }

    fun postQualityParamsForBatchChar(qualityPost: VegaQualityNigeriaPost) =
        viewModelScope.launch(dispatchers.main) {
            _qualityBatch.removeSource(qualityBatchSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityBatchSource = useCase.postBatchQuality(qualityPost)
            }
            _qualityBatch.addSource(qualityBatchSource) {
                _qualityBatch.value = it
            }
        }

}
