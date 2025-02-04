package com.olam.warehouse.vegax.lotqualitynigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
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
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityInspectionLotDetails
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityInspectionLots
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityPostResponse
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaLotQualityModel
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.usecase.VegaCocoaLotQualityUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaCocoaLotQualityViewModel(
    private val useCase: VegaCocoaLotQualityUsecase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inspectionLotsSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>>>> =
        MutableLiveData()
    private var _inspectionLots =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>>>>()
    val inspectionLots: LiveData<Resource<GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>>>> get() = _inspectionLots

    private var lotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails>>> =
        MutableLiveData()
    private var _lotDetails =
        MediatorLiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails>>> get() = _lotDetails

    private var postLotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityPostResponse>>> =
        MutableLiveData()
    private var _postLotDetails =
        MediatorLiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityPostResponse>>>()
    val postLotDetails: LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityPostResponse>>> get() = _postLotDetails

    private var inventoryList = MediatorLiveData<Resource<GenericReqAndResp<VegaLotQualityModel>>>()
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaLotQualityModel>>> =
        MutableLiveData()
    val inventoryModelList: LiveData<Resource<GenericReqAndResp<VegaLotQualityModel>>> get() = inventoryList

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> =
        MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var qualitySource: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> =
        MutableLiveData()
    private val _quality = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>>()
    val quality: LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> get() = _quality

    private var qualityNigeriaSource: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> =
        MutableLiveData()
    private val _qualityNigeria =
        MediatorLiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>>()
    val qualityNigeria: LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> get() = _qualityNigeria

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _stockList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _stockList


    fun getStockList(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stockList.removeSource(stockListSource)
        withContext(dispatchers.io) {
            stockListSource = useCase.getStockList(materialList)
        }
        _stockList.addSource(stockListSource) {
            _stockList.value = it
        }
    }

    fun updateDeletedItem(weighBridgeId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(weighBridgeId)
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

    fun saveInspectionLotDetails(lotDetail: VegaCocoaLotQualityInspectionLotDetails) =
        viewModelScope.launch(dispatchers.main) {
            _postLotDetails.removeSource(postLotDetailsSource)
            withContext(dispatchers.io) {
                postLotDetailsSource = useCase.saveInspectionLotDetails(lotDetail)
            }
            _postLotDetails.addSource(postLotDetailsSource) {
                _postLotDetails.value = it
            }
        }

    fun postQualityParams(qualityPost: VegaQualityPostLot) =
        viewModelScope.launch(dispatchers.main) {
            _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualitySource = useCase.postQuality(qualityPost)
            }
            _quality.addSource(qualitySource) {
                _quality.value = it
            }
        }

    fun saveWBDB(weighBridge: VegaQualityWBDetails) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveWBDB(weighBridge)
        }
    }


    fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.saveQualityData(qualityParameter, batchNo)
            }
        }

    fun postQualityParamsNigeria(qualityPost: VegaQualityNigeriaLotPost) =
        viewModelScope.launch(dispatchers.main) {
            _qualityNigeria.removeSource(qualityNigeriaSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityNigeriaSource = useCase.postNigeriaQuality(qualityPost)
            }
            _qualityNigeria.addSource(qualityNigeriaSource) {
                _qualityNigeria.value = it
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


    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getInventoryList() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            inventorySource = useCase.getInventoryList()
        }
        inventoryList.addSource(inventorySource) {
            inventoryList.value = it
        }
    }

    fun getInspectionLots() = viewModelScope.launch(dispatchers.main) {
        _inspectionLots.removeSource(inspectionLotsSource)
        withContext(dispatchers.io) {
            inspectionLotsSource = useCase.getInspectionLots()
        }
        _inspectionLots.addSource(inspectionLotsSource) {
            _inspectionLots.value = it
        }
    }

    fun getInspectionLotDetails(lotId: String) = viewModelScope.launch(dispatchers.main) {
        _lotDetails.removeSource(lotDetailsSource)
        withContext(dispatchers.io) {
            lotDetailsSource = useCase.getInspectionLotDetails(lotId)
        }
        _lotDetails.addSource(lotDetailsSource) {
            _lotDetails.value = it
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

}
