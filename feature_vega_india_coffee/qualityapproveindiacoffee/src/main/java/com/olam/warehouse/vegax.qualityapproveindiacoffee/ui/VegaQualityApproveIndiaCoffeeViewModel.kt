package com.olam.warehouse.vegax.qualityapproveindiacoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.*
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.usecase.VegaQualityApproveIndiaCoffeeUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaQualityApproveIndiaCoffeeViewModel(
    private val useCase: VegaQualityApproveIndiaCoffeeUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()


    private var qualitySources: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>> =
        MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>> get() = _quality

    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> =
        MutableLiveData()
    private val _weighBridge =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> get() = _weighBridge

    private var qcweighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _qcweighBridge =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val qcweighBridge: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _qcweighBridge

    private var grnSource: LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>>> =
        MutableLiveData()
    private val _grn = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>>>()
    val grn: LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>>> get() = _grn

    private var approvalSource: LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>> =
        MutableLiveData()
    private val _approval = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>>()
    val approval: LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>> get() = _approval

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var mtntWeightSource: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>> =
        MutableLiveData()
    private val _mtntWeight = MediatorLiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>>()
    val mtntWeight: LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>> get() = _mtntWeight

    private var weightbridgeListSource: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>>> =
        MutableLiveData()
    private val _weightbrigeList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>>>()
    val weightbridgeList: LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>>> get() = _weightbrigeList

    private var multiPlantSource: LiveData<List<Plant>> = MutableLiveData()
    private val _multiPlant = MediatorLiveData<List<Plant>>()
    val multiPlant: LiveData<List<Plant>> get() = _multiPlant

    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline

    fun getMtntWeightDetails(wbid: String) = viewModelScope.launch(dispatchers.main) {
        _mtntWeight.removeSource(mtntWeightSource)
        withContext(dispatchers.io) {
            mtntWeightSource = useCase.getMtntWeightDetails(wbid)
        }
        _mtntWeight.addSource(mtntWeightSource) {
            _mtntWeight.value = it
        }
    }

    fun getWeightbridgeListDetails() = viewModelScope.launch(dispatchers.main) {
        _weightbrigeList.removeSource(weightbridgeListSource)
        withContext(dispatchers.io) {
            weightbridgeListSource = useCase.getWeightbridgeListDetails()
        }
        _weightbrigeList.addSource(weightbridgeListSource) {
            _weightbrigeList.value = it
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

    fun getWeighBridgeIdDetail(wbid: String, isWeighScale: Boolean) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeId.removeSource(weighBridgeIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeIdSource = useCase.getWeighBridgeIdDetail(wbid, isWeighScale)
        }
        _weighBridgeId.addSource(weighBridgeIdSource) {
            _weighBridgeId.value = it
        }
    }

    fun getWeighBridgeDataOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        getWeighBridgeDetailOnline()
        return weighBridgeOnline
    }

    private fun getWeighBridgeDetailOnline() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOnlineSource = useCase.getWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> {
        fetchWeighBridgeList(selectedPlantId)
        return weighBridge
    }

    fun getQCWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        fetchQcWeighBridgeList(selectedPlantId)
        return qcweighBridge
    }


    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>> get() = _qualityDetails

    fun getQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>> {
        fetchQualityDetails(charge, material)
        return qualityDetails
    }

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityListSource = useCase.getQualityParam(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityListSource) {
                _qualitylist.value = it
            }
        }

    /*private var qualityGetSource: LiveData<List<QualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<QualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<QualityParamsWithQualitative>> get() = _qualitylist*/


    private var approveSource: LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>> =
        MutableLiveData()
    private val _approveDetails =
        MediatorLiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>>()
    val approveDetails: LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>> get() = _approveDetails
    fun postWbIdApprove(postdata: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>> {
        wbIdLotApprove(postdata)
        return approveDetails
    }

    private fun fetchWeighBridgeList(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.fetchWeighBridgeList(selectedPlantId)
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }

    private fun fetchQcWeighBridgeList(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _qcweighBridge.removeSource(qcweighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qcweighBridgeSource = useCase.fetchQcWeighBridgeList(selectedPlantId)
        }
        _qcweighBridge.addSource(qcweighBridgeSource) {
            _qcweighBridge.value = it
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

    private fun wbIdLotApprove(charge: VegaQualityApproveCameroonPostData) = viewModelScope.launch(dispatchers.main) {
        _approveDetails.removeSource(approveSource)
        withContext(dispatchers.io) {
            approveSource = useCase.wbIdLotApprove(charge)
        }
        _approveDetails.addSource(approveSource) {
            _approveDetails.value = it
        }
    }

    fun postGrn(grnPost: VegaQualityApproveIndiaCoffeeGrnPost) = viewModelScope.launch(dispatchers.main) {
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

    fun postQualityParams(qualityPost: VegaCameroonQcPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySources) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySources = useCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySources) {
            _quality.value = it
        }
    }
    /*fun getMultiPlantList() = viewModelScope.launch(dispatchers.main) {
        _multiPlant.removeSource(multiPlantSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            multiPlantSource = useCase.getMultiPlantList()
        }
        _multiPlant.addSource(multiPlantSource) {
            _multiPlant.value = it
        }
    }*/
}
