package com.olam.warehouse.vegax.qualityapprovecameroon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.usecase.VegaQualityApproveCameroonUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
class VegaQualityApproveCameroonViewModel(
    private val useCase: VegaQualityApproveCameroonUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var qualitySources: LiveData<Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>> =
        MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>> get() = _quality

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

    private var grnSource: LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>>> =
        MutableLiveData()
    private val _grn = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>>>()
    val grn: LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>>> get() = _grn

    private var approvalSource: LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>> =
        MutableLiveData()
    private val _approval = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>>()
    val approval: LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>> get() = _approval

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var mtntWeightSource: LiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>> =
        MutableLiveData()
    private val _mtntWeight = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>>()
    val mtntWeight: LiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>> get() = _mtntWeight

    private var weightbridgeListSource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>> =
        MutableLiveData()
    private val _weightbrigeList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>>()
    val weightbridgeList: LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>> get() = _weightbrigeList

    private var multiPlantSource: LiveData<List<Plant>> = MutableLiveData()
    private val _multiPlant = MediatorLiveData<List<Plant>>()
    val multiPlant: LiveData<List<Plant>> get() = _multiPlant

    private var reprintListSource: LiveData<Resource<GenericReqAndResp<List<VegaReprintList>>>> =
        MutableLiveData()
    private val _reprintList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReprintList>>>>()
    val reprintList: LiveData<Resource<GenericReqAndResp<List<VegaReprintList>>>> get() = _reprintList

    private var printDownloadSource: LiveData<Resource<GenericReqAndResp<String>>> =
        MutableLiveData()
    private val _printDownload = MediatorLiveData<Resource<GenericReqAndResp<String>>>()
    val printDownload: LiveData<Resource<GenericReqAndResp<String>>> get() = _printDownload

    fun downloadSelectedItemPrint(selectedItemId: String) = viewModelScope.launch(dispatchers.main) {
        _printDownload.removeSource(printDownloadSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            printDownloadSource = useCase.downloadReprintItem(selectedItemId)
        }
        _printDownload.addSource(printDownloadSource) {
            _printDownload.value = it
        }
    }


    fun getReprintList() = viewModelScope.launch(dispatchers.main) {
        _reprintList.removeSource(reprintListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            reprintListSource = useCase.getReprintList()
        }
        _reprintList.addSource(reprintListSource) {
            _reprintList.value = it
        }
    }


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

    fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> {
        fetchWeighBridgeList(selectedPlantId)
        return weighBridge
    }

    fun getQCWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        fetchQcWeighBridgeList(selectedPlantId)
        return qcweighBridge
    }


    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>> get() = _qualityDetails

    fun getQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>> {
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


    private var approveSource: LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>> =
        MutableLiveData()
    private val _approveDetails =
        MediatorLiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>>()
    val approveDetails: LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>> get() = _approveDetails
    fun postWbIdApprove(postdata: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>> {
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

    fun postGrn(grnPost: VegaQualityApproveCameroonGrnPost) = viewModelScope.launch(dispatchers.main) {
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

    fun postQualityParams(qualityPost: VegaCameroonQcPost) =
        viewModelScope.launch(dispatchers.main) {
            _quality.removeSource(qualitySources) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualitySources = useCase.postQuality(qualityPost)
            }
            _quality.addSource(qualitySources) {
                _quality.value = it
            }
        }


    //Add Live Data to View Model DSE Data
    private var DSEApproveDataSource: LiveData<Resource<GenericReqAndResp<VegaQualityApproveDSE>>> =
        MutableLiveData()
    private val _DSEApproveData =
        MediatorLiveData<Resource<GenericReqAndResp<VegaQualityApproveDSE>>>()
    val DSEApproveData: LiveData<Resource<GenericReqAndResp<VegaQualityApproveDSE>>> get() = _DSEApproveData


    fun getDSEDetails(
        status: String
    ): LiveData<Resource<GenericReqAndResp<VegaQualityApproveDSE>>> {
        fetchDSEResponse(status)
        return DSEApproveData
    }

    private fun fetchDSEResponse(status: String) =
        viewModelScope.launch(dispatchers.main) {
            _DSEApproveData.removeSource(DSEApproveDataSource)
            withContext(dispatchers.io) {
                DSEApproveDataSource = useCase.getDSEResponse(status)
            }
            _DSEApproveData.addSource(DSEApproveDataSource) {
                _DSEApproveData.value = it
            }
        }

    // Get Threshold Values
    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = MutableLiveData()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems


    fun getThresholdValue(role: String) =
        viewModelScope.launch(dispatchers.main) {
            _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                miscellaneousSource = useCase.getThresholdValue(role)
            }
            _miscellaneousItems.addSource(miscellaneousSource) {
                _miscellaneousItems.value = it
            }
        }



}
