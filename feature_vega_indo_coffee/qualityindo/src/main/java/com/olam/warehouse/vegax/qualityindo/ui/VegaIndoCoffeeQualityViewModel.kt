package com.olam.warehouse.vegax.qualityindo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualityindo.data.domain.usecase.VegaIndoCoffeeQualityUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */

class VegaIndoCoffeeQualityViewModel(
    private val useCase: VegaIndoCoffeeQualityUseCase,
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

    private var lotOfflineLocalSource: LiveData<List<VegaCoffeeReceiveLots>> =
        MutableLiveData()
    private val _lotOfflineLocal = MediatorLiveData<List<VegaCoffeeReceiveLots>>()
    val lotOfflineLocal: LiveData<List<VegaCoffeeReceiveLots>> get() = _lotOfflineLocal

    private var lotOfflineQtyLocalSource: LiveData<List<VegaCoffeeLot>> =
        MutableLiveData()
    private val _lotOfflineQtyLocal = MediatorLiveData<List<VegaCoffeeLot>>()
    val lotOfflineQtyLocal: LiveData<List<VegaCoffeeLot>> get() = _lotOfflineQtyLocal

    private var qualityGetSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>> = MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>> get() = _quality

    private var qualitySourceSupplier: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>> =
        MutableLiveData()
    private val _qualitySupplier =
        MediatorLiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>>()
    val qualitySupplier: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>> get() = _qualitySupplier

    private var qualityOfflineSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _qualityOfflineList = MediatorLiveData<List<VegaQualityWBDetails>>()
    val qualityOfflineList: LiveData<List<VegaQualityWBDetails>> get() = _qualityOfflineList

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var weighBridgeSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<List<VegaQualityWBDetails>>()
    val weighBridge: LiveData<List<VegaQualityWBDetails>> get() = _weighBridge

    private var vegaMaterialResource: LiveData<List<VegaMaterial>> =
        MutableLiveData()
    private val _vegaMaterials =
        MediatorLiveData<List<VegaMaterial>>()
    val vegaMaterials: LiveData<List<VegaMaterial>> get() = _vegaMaterials

    fun getWeighBridgeDataOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        getWeighBridgeDetailOnline()
        return weighBridgeOnline
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

    fun getLotDetailOfflineLocal(tempWbId: String) = viewModelScope.launch(dispatchers.main) {
        _lotOfflineLocal.removeSource(lotOfflineLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotOfflineLocalSource = useCase.getLotDetailOfflineLocal(tempWbId)
        }
        _lotOfflineLocal.addSource(lotOfflineLocalSource) {
            _lotOfflineLocal.value = it
        }
    }

    fun getLotDetailOfflineQtyLocal(tempWbId: String) = viewModelScope.launch(dispatchers.main) {
        _lotOfflineQtyLocal.removeSource(lotOfflineQtyLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotOfflineQtyLocalSource = useCase.getLotDetailOfflineQtyLocal(tempWbId)
        }
        _lotOfflineQtyLocal.addSource(lotOfflineQtyLocalSource) {
            _lotOfflineQtyLocal.value = it
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

    fun postQualityParams(paramPost: VegaIndoCoffeeQualityParamPost) = viewModelScope.launch(dispatchers.main) {
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

    fun postQualitySupplierParams(paramPost: VegaIndoCoffeeQualitySupplierParamPost) =
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

    fun getQualityOfflineList() = viewModelScope.launch(dispatchers.main) {
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

    fun getPreSamplingQualitydata(batchNo: String, materialId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }

    fun saveQualityLot(lot: List<VegaCoffeeLot>) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveQualityLot(lot)
        }
    }

    fun deleteAllItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteAllItem(tmpWbId)
        }
    }

    fun getVegaMaterials() {
        viewModelScope.launch(dispatchers.main) {
            _vegaMaterials.removeSource(vegaMaterialResource)
            withContext(dispatchers.io) {
                vegaMaterialResource = useCase.getVegaMaterials()
            }
            _vegaMaterials.addSource(vegaMaterialResource) {
                _vegaMaterials.value = it
            }
        }
    }

}
