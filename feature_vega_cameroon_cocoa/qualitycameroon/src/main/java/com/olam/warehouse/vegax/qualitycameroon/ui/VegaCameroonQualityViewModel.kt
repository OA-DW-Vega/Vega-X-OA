package com.olam.warehouse.vegax.qualitycameroon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.VegaCameroonQualityUseCase
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityParamPost
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityPost
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityPostResponse
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualitySecretId
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaCameroonQualityViewModel(
    private val useCase: VegaCameroonQualityUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaCameroonQualityPostResponse>>> = MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonQualityPostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaCameroonQualityPostResponse>>> get() = _quality

    private var qualitySourceMtnr: LiveData<Resource<GenericReqAndResp<VegaCameroonQualityParamPost>>> = MutableLiveData()
    private val _qualityMtnr = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonQualityParamPost>>>()
    val qualityMtnr: LiveData<Resource<GenericReqAndResp<VegaCameroonQualityParamPost>>> get() = _qualityMtnr

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var qualityOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _qualityOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    val qualityOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityOfflineList

    private var weighBridgeSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<List<VegaQualityWBDetails>>()
    val weighBridge: LiveData<List<VegaQualityWBDetails>> get() = _weighBridge

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline

    private var wbWithParamSource: LiveData<List<VegaWeighBridgeWithQualityParams>> = MutableLiveData()
    private val _wbWithParams = MediatorLiveData<List<VegaWeighBridgeWithQualityParams>>()
    val wbWithParams: LiveData<List<VegaWeighBridgeWithQualityParams>> get() = _wbWithParams


    // === Start MTNR ===
    private var lotOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>> =
        MutableLiveData()
    private val _lotOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>>()
    val lotOnline: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>> get() = _lotOnline

    fun getLotDetailOnline(weighBridgeId: String) = viewModelScope.launch(dispatchers.main) {
        _lotOnline.removeSource(lotOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotOnlineSource = useCase.getLotDetailOnline(weighBridgeId)
        }
        _lotOnline.addSource(lotOnlineSource) {
            _lotOnline.value = it
        }
    }

    private var secretIdSource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonQualitySecretId>>>> =
        MutableLiveData()
    private var _secretId = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonQualitySecretId>>>>()
    val secretId: LiveData<Resource<GenericReqAndResp<List<VegaCameroonQualitySecretId>>>> get() = _secretId

    fun getSecretIdList(startDate: String, endDate: String, isMtnt: Boolean) = viewModelScope.launch(dispatchers.main) {
        _secretId.removeSource(secretIdSource)
        withContext(dispatchers.io) {
            secretIdSource = useCase.getDashboardResult(startDate, endDate, isMtnt)
        }
        _secretId.addSource(secretIdSource) {
            _secretId.value = it
        }
    }

    // === End MTNR ===
    fun postQualityParams(qualityPost: VegaCameroonQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = useCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun postQualityParamsMtnr(paramPost: VegaCameroonQualityParamPost) = viewModelScope.launch(dispatchers.main) {
        _qualityMtnr.removeSource(qualitySourceMtnr) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySourceMtnr = useCase.postQualityMtnr(paramPost)
        }
        _qualityMtnr.addSource(qualitySourceMtnr) {
            _qualityMtnr.value = it
        }
    }

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) = viewModelScope.launch(dispatchers.main) {
        _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityListSource = useCase.getQualityParams(materialId, isValueExist, wbId)
        }
        _qualitylist.addSource(qualityListSource) {
            _qualitylist.value = it
        }
    }

    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveQualityData(qualityParameter, batchNo)
        }
    }
    fun getPreSamplingQualitydata(batchNo: String, materialId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }

    fun saveWBDB(weighBridge: VegaQualityWBDetails) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveWBDB(weighBridge)
        }
    }

    fun getQualityOfflineListCount(): LiveData<List<VegaQualityWBDetails>> {
        getQualityOfflineList()
        return qualityOfflineList
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

    private fun getQualityOfflineList() = viewModelScope.launch(dispatchers.main) {
        _qualityOfflineList.removeSource(qualityOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityOfflineSource = useCase.getQualityOfflineList()
        }
        _qualityOfflineList.addSource(qualityOfflineSource) {
            _qualityOfflineList.value = it
        }
    }

    fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.getWeighBridgeDetail()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun getWeighBridgeDataOnline(qcFlag: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        getWeighBridgeDetailOnline(qcFlag)
        return weighBridgeOnline
    }

    fun getWeighBridgeDataOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        getMtnrWeighBridgeDetailOnline()
        return weighBridgeOnline
    }

    private fun getWeighBridgeDetailOnline(qcFlag: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOnlineSource = useCase.getWeighBridgeDetailOnline(qcFlag)
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    private fun getMtnrWeighBridgeDetailOnline() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOnlineSource = useCase.getMtnrWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getWBWithQualityPars(weighBridgeID: String) = viewModelScope.launch(dispatchers.main) {
        _wbWithParams.removeSource(wbWithParamSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbWithParamSource = useCase.getWBWithQuality(weighBridgeID)
        }
        _wbWithParams.addSource(wbWithParamSource) {
            _wbWithParams.value = it
        }
    }

    fun updateDeletedItem(weighBridgeId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(weighBridgeId)
        }
    }

    fun getProcessTypeList(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCase.getProcessTypeList(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }


}
