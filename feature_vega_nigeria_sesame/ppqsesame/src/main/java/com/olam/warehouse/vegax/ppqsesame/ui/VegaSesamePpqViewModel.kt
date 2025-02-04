package com.olam.warehouse.vegax.ppqsesame.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaPpqModel
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLots
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqPostResponse
import com.olam.warehouse.vegax.ppqsesame.data.domain.usecase.VegaSesamePpqUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaSesamePpqViewModel(
    private val useCase: VegaSesamePpqUsecase, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inspectionLotsSource: LiveData<Resource<GenericReqAndResp<List<VegaSesamePpqInspectionLots>>>> =
        MutableLiveData()
    private var _inspectionLots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaSesamePpqInspectionLots>>>>()
    val inspectionLots: LiveData<Resource<GenericReqAndResp<List<VegaSesamePpqInspectionLots>>>> get() = _inspectionLots

    private var lotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaSesamePpqInspectionLotDetails>>> =
        MutableLiveData()
    private var _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaSesamePpqInspectionLotDetails>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<VegaSesamePpqInspectionLotDetails>>> get() = _lotDetails

    private var postLotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaSesamePpqPostResponse>>> =
        MutableLiveData()
    private var _postLotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaSesamePpqPostResponse>>>()
    val postLotDetails: LiveData<Resource<GenericReqAndResp<VegaSesamePpqPostResponse>>> get() = _postLotDetails

    private var inventoryList = MediatorLiveData<Resource<GenericReqAndResp<VegaPpqModel>>>()
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaPpqModel>>> =
        MutableLiveData()
    val inventoryModelList: LiveData<Resource<GenericReqAndResp<VegaPpqModel>>> get() = inventoryList

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
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

    fun saveInspectionLotDetails(lotDetail: VegaSesamePpqInspectionLotDetails) =
        viewModelScope.launch(dispatchers.main) {
            _postLotDetails.removeSource(postLotDetailsSource)
            withContext(dispatchers.io) {
                postLotDetailsSource = useCase.saveInspectionLotDetails(lotDetail)
            }
            _postLotDetails.addSource(postLotDetailsSource) {
                _postLotDetails.value = it
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

    private var storageLocationSource: LiveData<List<VegaStorageLocation>> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<List<VegaStorageLocation>>()
    val storageLocation: LiveData<List<VegaStorageLocation>> get() = _storageLocation

    fun getStorageLocations() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getStoreLocations()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

}
