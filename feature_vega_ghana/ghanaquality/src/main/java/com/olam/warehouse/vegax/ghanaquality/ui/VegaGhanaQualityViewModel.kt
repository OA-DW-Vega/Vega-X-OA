package com.olam.warehouse.vegax.ghanaquality.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaLotQualityDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQuality
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQualityMtnBatch
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.VegaGhanaQualityUseCase
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.BatchNumResponse
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityParamPost
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityPost
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityPostResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaGhanaQualityViewModel(
    private val useCase: VegaGhanaQualityUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaGhanaQualityPostResponse>>> = MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaGhanaQualityPostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaGhanaQualityPostResponse>>> get() = _quality

    private var qualitySourceMtnr: LiveData<Resource<GenericReqAndResp<VegaGhanaQualityParamPost>>> = MutableLiveData()
    private val _qualityMtnr = MediatorLiveData<Resource<GenericReqAndResp<VegaGhanaQualityParamPost>>>()
    val qualityMtnr: LiveData<Resource<GenericReqAndResp<VegaGhanaQualityParamPost>>> get() = _qualityMtnr

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var qualityMtnrListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualityMtnrlist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualityMtnrlist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualityMtnrlist

    private var offlineListSource: LiveData<List<VegaQualityWithQualitative>> = MutableLiveData()
    private val _offlinelist = MediatorLiveData<List<VegaQualityWithQualitative>>()
    val offlinelist: LiveData<List<VegaQualityWithQualitative>> get() = _offlinelist

    private var mtnrLotListSource: LiveData<VegaGhanaMtnrQualityLot> = MutableLiveData()
    private val _mtnrLotList = MediatorLiveData<VegaGhanaMtnrQualityLot>()
    val mtnrLotList: LiveData<VegaGhanaMtnrQualityLot> get() = _mtnrLotList

    private var mtnrAllLotListSource: LiveData<List<VegaGhanaMtnrQualityLot>> = MutableLiveData()
    private val _mtnrAllLotList = MediatorLiveData<List<VegaGhanaMtnrQualityLot>>()
    val mtnrAllLotList: LiveData<List<VegaGhanaMtnrQualityLot>> get() = _mtnrAllLotList

    private var qualityOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _qualityOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    val qualityOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityOfflineList

    private var qualityMtnrOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _qualityMtnrOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    val qualityMtnrOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityMtnrOfflineList

    private var batchSource: LiveData<Resource<GenericReqAndResp<BatchNumResponse>>> = MutableLiveData()
    private val _batch = MediatorLiveData<Resource<GenericReqAndResp<BatchNumResponse>>>()
    val batch: LiveData<Resource<GenericReqAndResp<BatchNumResponse>>> get() = _batch

    private var offlineBatchSource: LiveData<VegaGhanaQualityMtnBatch> = MutableLiveData()
    private val _offlineBatch = MediatorLiveData<VegaGhanaQualityMtnBatch>()
    val offlineBatch: LiveData<VegaGhanaQualityMtnBatch> get() = _offlineBatch

    private var weighBridgeSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<List<VegaQualityWBDetails>>()
    val weighBridge: LiveData<List<VegaQualityWBDetails>> get() = _weighBridge

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var qualityOfflinePreSamplingSource: LiveData<List<VegaGhanaLotQualityDetails>> =
        MutableLiveData()
    private val _preSamplingOfflineQuality = MediatorLiveData<List<VegaGhanaLotQualityDetails>>()
    val preSamplingOfflineQuality: LiveData<List<VegaGhanaLotQualityDetails>> get() = _preSamplingOfflineQuality

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

    fun getLotQualityDetails(weighBridgeId: String) = viewModelScope.launch(dispatchers.main) {
        _mtnrLotList.removeSource(mtnrLotListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            mtnrLotListSource = useCase.getLotQualityDetails(weighBridgeId)
        }
        _mtnrLotList.addSource(mtnrLotListSource) {
            _mtnrLotList.value = it
        }
    }

    fun getAllLotQualityDetails() = viewModelScope.launch(dispatchers.main) {
        _mtnrAllLotList.removeSource(mtnrAllLotListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            mtnrAllLotListSource = useCase.getAllLotQualityDetails()
        }
        _mtnrAllLotList.addSource(mtnrAllLotListSource) {
            _mtnrAllLotList.value = it
        }
    }

    // === End MTNR ===
    fun postQualityParams(qualityPost: VegaGhanaQualityPost) = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = useCase.postQuality(qualityPost)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun postQualityParamsMtnr(paramPost: VegaGhanaQualityParamPost) = viewModelScope.launch(dispatchers.main) {
        _qualityMtnr.removeSource(qualitySourceMtnr) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySourceMtnr = useCase.postQualityMtnr(paramPost)
        }
        _qualityMtnr.addSource(qualitySourceMtnr) {
            _qualityMtnr.value = it
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

    fun getMtnrQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualityMtnrlist.removeSource(qualityMtnrListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityMtnrListSource = useCase.getMtnrQualityParams(materialId, isValueExist, wbId)
            }
            _qualityMtnrlist.addSource(qualityMtnrListSource) {
                _qualityMtnrlist.value = it
            }
        }

    fun getOfflineSavedQuality(wbid: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _offlinelist.removeSource(offlineListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineListSource = useCase.getOfflineSavedQuality(material, wbid)
        }
        _offlinelist.addSource(offlineListSource) {
            _offlinelist.value = it
        }
    }

    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveQualityData(qualityParameter, batchNo)
        }
    }

    fun saveVegaQualityWBDetails(wbid: String, batchNo: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveVegaQualityWBDetails(wbid, batchNo)
        }
    }

    fun saveVegaQualityWeightWBDetails(wbid: String, batchNo: String, weight: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.saveVegaQualityWeightWBDetails(wbid, batchNo, weight)
            }
        }

    fun savePostLotDetails(qualityParameter: List<VegaGhanaMtnrQualityLot>) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.savePostLotDetails(qualityParameter)
        }
    }

    fun saveMtnrQualityData(qualityParameter: VegaGhanaQuality, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.saveMtnrQualityData(qualityParameter, batchNo)
            }
        }

    fun saveOfflineQualityData(
        qualityParameter: List<VegaQuality>, batchNo: String,
        wbId: String
    ) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveOfflineQualityData(qualityParameter, batchNo, wbId)
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

    fun getOfflinePreSamplingQualitydata(batchNo: String, materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _preSamplingOfflineQuality.removeSource(qualityOfflinePreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityOfflinePreSamplingSource = useCase.getOfflinePreSamplingQualityList(batchNo, materialId)
            }
            _preSamplingOfflineQuality.addSource(qualityOfflinePreSamplingSource) {
                _preSamplingOfflineQuality.value = it
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

    fun getMtnrQualityOfflineListCount(): LiveData<List<VegaQualityWBDetails>> {
        getQualityMtnrOfflineList()
        return qualityMtnrOfflineList
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

    private fun getQualityMtnrOfflineList() = viewModelScope.launch(dispatchers.main) {
        _qualityMtnrOfflineList.removeSource(qualityMtnrOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityMtnrOfflineSource = useCase.getQualityMtnrOfflineList()
        }
        _qualityMtnrOfflineList.addSource(qualityMtnrOfflineSource) {
            _qualityMtnrOfflineList.value = it
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

    /*
        fun getWBWithQualityPars(weighBridgeID: String) = viewModelScope.launch(dispatchers.main) {
            _wbWithParams.removeSource(wbWithParamSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                wbWithParamSource = useCase.getWBWithQuality(weighBridgeID)
            }
            _wbWithParams.addSource(wbWithParamSource) {
                _wbWithParams.value = it
            }
        }
    */
    fun getGhanaCashewWBWithQuality(weighBridgeID: String) = viewModelScope.launch(dispatchers.main) {
        _wbWithParams.removeSource(wbWithParamSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbWithParamSource = useCase.getGhanaCashewWBWithQuality(weighBridgeID)
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

    fun deleteWBDetals() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteWBDetals()
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

    fun getDeliveryBatchNumber(deliveryNo: String, posnr: String) = viewModelScope.launch(dispatchers.main) {
        _batch.removeSource(batchSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            batchSource = useCase.getDeliveryBatchNumber(deliveryNo, posnr)
        }
        _batch.addSource(batchSource) {
            _batch.value = it
        }
    }

    fun getOfflineDeliveryBatchNumber(deliveryNo: String, posnr: String) = viewModelScope.launch(dispatchers.main) {
        println("Roshna => view model => $deliveryNo")
        _offlineBatch.removeSource(offlineBatchSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineBatchSource = useCase.getOfflineDeliveryBatchNumber(deliveryNo, posnr)
        }
        _offlineBatch.addSource(offlineBatchSource) {
            _offlineBatch.value = it
        }
    }

}
