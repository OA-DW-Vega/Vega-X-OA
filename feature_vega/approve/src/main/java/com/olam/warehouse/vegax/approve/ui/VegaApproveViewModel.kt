package com.olam.warehouse.vegax.approve.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.approve.data.domain.model.*
import com.olam.warehouse.vegax.approve.data.domain.usecase.VegaApproveUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
class VegaApproveViewModel(
    private val useCase: VegaApproveUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>> =
        MutableLiveData()
    private val _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>> get() = _weighBridge

    private var grnSource: LiveData<Resource<GenericReqAndResp<VegaGrnResponse>>> = MutableLiveData()
    private val _grn = MediatorLiveData<Resource<GenericReqAndResp<VegaGrnResponse>>>()
    val grn: LiveData<Resource<GenericReqAndResp<VegaGrnResponse>>> get() = _grn

    private var approvalSource: LiveData<Resource<GenericReqAndResp<VegaApprovalResponse>>> = MutableLiveData()
    private val _approval = MediatorLiveData<Resource<GenericReqAndResp<VegaApprovalResponse>>>()
    val approval: LiveData<Resource<GenericReqAndResp<VegaApprovalResponse>>> get() = _approval


    fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>> {
        fetchWeighBridgeList()
        return weighBridge
    }

    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<VegaApproveQuality>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaApproveQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaApproveQuality>>>> get() = _qualityDetails
    fun getQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaApproveQuality>>>> {
        fetchQualityDetails(charge, material)
        return qualityDetails
    }

    /*private var qualityGetSource: LiveData<List<QualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<QualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<QualityParamsWithQualitative>> get() = _qualitylist*/


    private var approveSource: LiveData<Resource<GenericReqAndResp<VegaApproveWeighBridgeId>>> =
        MutableLiveData()
    private val _approveDetails =
        MediatorLiveData<Resource<GenericReqAndResp<VegaApproveWeighBridgeId>>>()
    val approveDetails: LiveData<Resource<GenericReqAndResp<VegaApproveWeighBridgeId>>> get() = _approveDetails
    fun postWbIdApprove(postdata: VegaApprovePostData): LiveData<Resource<GenericReqAndResp<VegaApproveWeighBridgeId>>> {
        wbIdLotApprove(postdata)
        return approveDetails
    }

    private fun fetchWeighBridgeList() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.fetchWeighBridgeList()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }

    private fun fetchQualityDetails(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualitySource)
            withContext(dispatchers.io) {
                qualitySource = useCase.fetchQualityDetails(charge, material)
            }
            _qualityDetails.addSource(qualitySource) {
                _qualityDetails.value = it
            }
        }

    /*fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityGetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityGetSource = useCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityGetSource) {
                _qualitylist.value = it
            }
        }*/

    private fun wbIdLotApprove(charge: VegaApprovePostData) = viewModelScope.launch(dispatchers.main) {
        _approveDetails.removeSource(approveSource)
        withContext(dispatchers.io) {
            approveSource = useCase.wbIdLotApprove(charge)
        }
        _approveDetails.addSource(approveSource) {
            _approveDetails.value = it
        }
    }

    fun postGrn(grnPost: VegaGrnPost) = viewModelScope.launch(dispatchers.main) {
        _grn.removeSource(grnSource)
        withContext(dispatchers.io) {
            grnSource = useCase.postGrn(grnPost)
        }
        _grn.addSource(grnSource) {
            _grn.value = it
        }
    }

    fun postApproval(postApprovalData: PostApprovalData) = viewModelScope.launch(dispatchers.main) {
        _approval.removeSource(approvalSource)
        withContext(dispatchers.io) {
            approvalSource = useCase.postApproval(postApprovalData)
        }
        _approval.addSource(approvalSource) {
            _approval.value = it
        }
    }


}
