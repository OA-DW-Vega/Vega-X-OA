package com.olam.warehouse.vegax.qualitycoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaUpdateTallySequencePost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.usecase.VegaCoffeeQualityUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCoffeeQualityViewModel(
    private val useCase: VegaCoffeeQualityUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline

    private var lotOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>> =
        MutableLiveData()
    private val _lotOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>>()
    val lotOnline: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>> get() = _lotOnline

    private var lotOnlineSourceWB: LiveData<Resource<GenericReqAndResp<VegaCoffeeLot>>> =
        MutableLiveData()
    private val _lotOnlineWB = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeLot>>>()
    val lotOnlineWB: LiveData<Resource<GenericReqAndResp<VegaCoffeeLot>>> get() = _lotOnlineWB

    private var qualityGetSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPost>>> = MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPost>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPost>>> get() = _quality

    private var qualitySourceSupplier: LiveData<Resource<GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>>> =
        MutableLiveData()
    private val _qualitySupplier = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>>>()
    val qualitySupplier: LiveData<Resource<GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>>> get() = _qualitySupplier

    private var qualityOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _qualityOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    val qualityOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityOfflineList

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var tallySequenceSource: LiveData<Resource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>> =
        MutableLiveData()
    private val _updatetallySequence =
        MediatorLiveData<Resource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>>()
    val updatetallySequence: LiveData<Resource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>> get() = _updatetallySequence

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

    fun getLotDetailOnline(weighBridgeId: String, isDual: Boolean) = viewModelScope.launch(dispatchers.main) {
        _lotOnline.removeSource(lotOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotOnlineSource = useCase.getLotDetailOnline(weighBridgeId, isDual)
        }
        _lotOnline.addSource(lotOnlineSource) {
            _lotOnline.value = it
        }
    }

    fun getLotDetailOnlineWB(weighBridgeId: String, isDual: Boolean) = viewModelScope.launch(dispatchers.main) {
        _lotOnlineWB.removeSource(lotOnlineSourceWB) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotOnlineSourceWB = useCase.getLotDetailOnlineWB(weighBridgeId, isDual)
        }
        _lotOnlineWB.addSource(lotOnlineSourceWB) {
            _lotOnlineWB.value = it
        }
    }

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityGetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityGetSource = useCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityGetSource) {
                _qualitylist.value = it
            }
        }

    fun postQualityParams(paramPost: VegaCoffeeQualityParamPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
           qualitySource = useCase.postQuality(paramPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }
    fun getWeighBridgeIdDetail(wbid: String, isWeighscale: Boolean) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeId.removeSource(weighBridgeIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeIdSource = useCase.getWeighBridgeIdDetail(wbid, isWeighscale)
        }
        _weighBridgeId.addSource(weighBridgeIdSource) {
            _weighBridgeId.value = it
        }
    }
    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId

    fun postQualitySupplierParams(paramPost: VegaCoffeeQualitySupplierParamPost) =
        viewModelScope.launch(dispatchers.main) {
            _qualitySupplier.removeSource(qualitySourceSupplier) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualitySourceSupplier = useCase.postQualitySupplier(paramPost)
            }
            _qualitySupplier.addSource(qualitySourceSupplier) {
                _qualitySupplier.value = it
            }
        }

    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveQualityData(qualityParameter, batchNo)
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

    private fun getQualityOfflineList() = viewModelScope.launch(dispatchers.main) {
        _qualityOfflineList.removeSource(qualityOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityOfflineSource = useCase.getQualityOfflineList()
        }
        _qualityOfflineList.addSource(qualityOfflineSource) {
            _qualityOfflineList.value = it
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

    fun getPreSamplingQualitydata(batchNo: String, materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
            }
            _preSamplingQuality.addSource(qualityPreSamplingSource) {
                _preSamplingQuality.value = it
            }
        }

    fun updateTallySequence(paramPost: NicaraguaUpdateTallySequencePost) =
        viewModelScope.launch(dispatchers.main) {
            _updatetallySequence.removeSource(tallySequenceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                tallySequenceSource = useCase.updateTallySequence(paramPost)
            }
            _updatetallySequence.addSource(tallySequenceSource) {
                _updatetallySequence.value = it
            }
        }

    fun generateTallySequnceNumber(): String {
        var tallySequnceId = ""
        val tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        val cropyear = PreferenceHelper.get(Constants.CROP_YEAR, "")
        tallySequnceId = cropyear.plus("-").plus(tallySequence)

        return tallySequnceId
    }


}
