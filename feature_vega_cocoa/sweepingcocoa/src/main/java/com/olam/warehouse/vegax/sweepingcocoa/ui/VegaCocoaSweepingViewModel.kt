package com.olam.warehouse.vegax.sweepingcocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaCocoaSweepingLots
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingPost
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingResponse
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.usecase.VegaCocoaSweepingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCocoaSweepingViewModel(
    private val useCase: VegaCocoaSweepingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSweepingLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaSweepingLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaSweepingLots>>>> get() = _qualityDetails

    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems

    private var sweepingPostSource: LiveData<Resource<GenericReqAndResp<VegaSweepingResponse>>> = MutableLiveData()
    private val _sweepingPost = MediatorLiveData<Resource<GenericReqAndResp<VegaSweepingResponse>>>()
    val sweepingPost: LiveData<Resource<GenericReqAndResp<VegaSweepingResponse>>> get() = _sweepingPost

    var lotId: String = ""
    var materialNumber: String = ""
    var plantId: String = ""

    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
        }
    }

    fun getLotDetails() =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getLotDetails(lotId, materialNumber, plantId)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }

    fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(bagMaterial)
        }
    }

    fun getBagItems(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun postSweeping(vegaSweepingPost: VegaSweepingPost) = viewModelScope.launch(dispatchers.main) {
        _sweepingPost.removeSource(sweepingPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            sweepingPostSource = useCase.postSweeping(vegaSweepingPost)
        }
        _sweepingPost.addSource(sweepingPostSource) {
            _sweepingPost.value = it
        }
    }

    fun updateDataToDB(msg: String, status: Int, batchNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateDataToDB(msg, status, batchNumber)
        }
    }


}
