package com.olam.warehouse.vegax.dummyquality.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.dummyquality.data.domain.model.*
import com.olam.warehouse.vegax.dummyquality.data.domain.usecase.VegaCommonDummyQualityUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCommonDummyQualityViewModel(
    private val useCase: VegaCommonDummyQualityUseCase,
    private val dispatchers: AppDispatchers
    ) : BaseViewModel() {

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var qualityGetSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist


    private var dummySamplePostSource: LiveData<Resource<GenericMessage>> = MutableLiveData()
    private val _dummySamplePost = MediatorLiveData<Resource<GenericMessage>>()
    val dummySamplePost: LiveData<Resource<GenericMessage>> get() = _dummySamplePost


    private var getDummySamplePostSource: LiveData<Resource<GenericReqAndResp<ResponseDummySample>>> = MutableLiveData()
    private val _getDummySamplePost = MediatorLiveData<Resource<GenericReqAndResp<ResponseDummySample>>>()
    val getDummySamplePost: LiveData<Resource<GenericReqAndResp<ResponseDummySample>>> get() = _getDummySamplePost

    private var dummySampleSourceList: LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>> = MutableLiveData()
    private val _dummySampleSourceList = MediatorLiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>>()
    val dummySampleList: LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>> get() = _dummySampleSourceList

    private var dummySampleSourceDetails: LiveData<Resource<GenericReqAndResp<ResponseDummySampleList>>> = MutableLiveData()
    private val _dummySampleSourceDetails = MediatorLiveData<Resource<GenericReqAndResp<ResponseDummySampleList>>>()
    val dummySampleDetails: LiveData<Resource<GenericReqAndResp<ResponseDummySampleList>>> get() = _dummySampleSourceDetails

    private var dummySampleSourceDeleteList: LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>> = MutableLiveData()
    private val _dummySampleSourceDeleteList = MediatorLiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>>()
    val dummySampleDeleteList: LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>> get() = _dummySampleSourceDeleteList

    private var vendorAndMaterialSourceDList: LiveData<Resource<GenericReqAndResp<List<ResponseVendorAndMaterial>>>> = MutableLiveData()
    private val _vendorAndMaterialList = MediatorLiveData<Resource<GenericReqAndResp<List<ResponseVendorAndMaterial>>>>()
    val vendorAndMaterialList: LiveData<Resource<GenericReqAndResp<List<ResponseVendorAndMaterial>>>> get() = _vendorAndMaterialList

    fun getVendorAndMaterial(plantId: String) = viewModelScope.launch(dispatchers.main) {
        _vendorAndMaterialList.removeSource(vendorAndMaterialSourceDList) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            vendorAndMaterialSourceDList =
                useCase.getVendorAndMaterial(plantId)
        }
        _vendorAndMaterialList.addSource(vendorAndMaterialSourceDList) {
            _vendorAndMaterialList.value = it
        }
    }

    fun deleteDummySample(
        id: String
    ) = viewModelScope.launch(dispatchers.main) {
        _dummySampleSourceDeleteList.removeSource(dummySampleSourceDeleteList) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dummySampleSourceDeleteList =
                useCase.deleteDummySample(id)
        }
        _dummySampleSourceDeleteList.addSource(dummySampleSourceDeleteList) {
            _dummySampleSourceDeleteList.value = it
        }
    }



    fun getDummySampleDetails(
        key: String, id: String
    ) = viewModelScope.launch(dispatchers.main) {
        _dummySampleSourceDetails.removeSource(dummySampleSourceDetails) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dummySampleSourceDetails =
                useCase.getDummySampleDetails(key, id)
        }
        _dummySampleSourceDetails.addSource(dummySampleSourceDetails) {
            _dummySampleSourceDetails.value = it
        }
    }

    fun getDummySampleList(
        key: String, plantId: String, materialCode: String, vendorCode: String, dummySampleFlag: Boolean
    ) = viewModelScope.launch(dispatchers.main) {
        _dummySampleSourceList.removeSource(dummySampleSourceList) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dummySampleSourceList =
                useCase.getDummySampleList(key, plantId, materialCode, vendorCode, dummySampleFlag)
        }
        _dummySampleSourceList.addSource(dummySampleSourceList) {
            _dummySampleSourceList.value = it
        }
    }

    fun getDummySample(key: String,plantId:String,materialCode:String,vendorCode:String,dummySampleFlag: Boolean) = viewModelScope.launch(dispatchers.main) {
        _getDummySamplePost.removeSource(getDummySamplePostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getDummySamplePostSource = useCase.getDummySample(key, plantId, materialCode, vendorCode,dummySampleFlag)
        }
        _getDummySamplePost.addSource(getDummySamplePostSource) {
            _getDummySamplePost.value = it
        }

    }
    fun postDummySample(postData: VegaCommonDummySampleModel) = viewModelScope.launch(dispatchers.main) {
        _dummySamplePost.removeSource(dummySamplePostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dummySamplePostSource = useCase.postDummySample(postData)
        }
        _dummySamplePost.addSource(dummySamplePostSource) {
            _dummySamplePost.value = it
        }
    }

    fun getQualityParams(materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityGetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityGetSource = useCase.getQualityParams(materialId)
            }
            _qualitylist.addSource(qualityGetSource) {
                _qualitylist.value = it
            }
        }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplierList()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
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



}
