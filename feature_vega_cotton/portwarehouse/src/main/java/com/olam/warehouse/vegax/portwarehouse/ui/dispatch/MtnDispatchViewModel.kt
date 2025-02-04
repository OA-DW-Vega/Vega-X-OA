package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDispatchPostResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.repository.MtnDispatchRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
class MtnDispatchViewModel(private val repo: MtnDispatchRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    var Deliverys = emptyList<String>()
    var lastSelectedPosition = 0
    var mBaleList = mutableListOf<MtnBale>()
    var mDeliveryList = emptyList<MtnDispatchDelivery>()
    var mCurrentDelivery = MtnDispatchDelivery()
    var baleHashMap: HashMap<String, List<MtnBale>> = HashMap<String, List<MtnBale>>()
    var mCurrentBaleList: HashMap<String, List<MtnBale>> = HashMap<String, List<MtnBale>>()

    private var deliveyDetailsSource: LiveData<Resource<GenericReqAndResp<List<MtnDispatchDelivery>>>> =
        MutableLiveData()
    private val _deliveyDetails = MediatorLiveData<Resource<GenericReqAndResp<List<MtnDispatchDelivery>>>>()
    val deliveyDetails: LiveData<Resource<GenericReqAndResp<List<MtnDispatchDelivery>>>> get() = _deliveyDetails

    private var validateBaleSource: LiveData<Resource<GenericReqAndResp<MtnBale>>> = MutableLiveData()
    private val _validateBale = MediatorLiveData<Resource<GenericReqAndResp<MtnBale>>>()
    val validateBale: LiveData<Resource<GenericReqAndResp<MtnBale>>> get() = _validateBale

    private var postDispatchSource: LiveData<Resource<GenericReqAndResp<MtnDispatchPostResponse>>> = MutableLiveData()
    private val _postDispatch = MediatorLiveData<Resource<GenericReqAndResp<MtnDispatchPostResponse>>>()
    val postDispatch: LiveData<Resource<GenericReqAndResp<MtnDispatchPostResponse>>> get() = _postDispatch
    suspend fun insertOrReplaceGrade(grade: MtnGrade) = repo.insertOrReplaceGrade(grade)
    suspend fun insertOrReplaceDelivery(ot: MtnDispatchDelivery) = repo.insertOrReplaceDelivery(ot)
    suspend fun isDeliveryExist(deliveryNo: String) = repo.isDeliveryExist(deliveryNo)
    fun setDelivery(it: List<MtnDispatchDelivery>?) {
        if (it != null) {
            mDeliveryList = it
        }
        val data = ArrayList<String>()
        data.add("Select Delivery Number")
        it?.forEach {
            data.add(it.deliveryNumber)
        }
        Deliverys = data
    }

    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(
                0,
                9
            ).noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun isAlreadyExistBale(id: String?): Boolean {
        return mBaleList.any { it.baleID == id }
    }

    suspend fun getDeliveryWithGrades(deliverNo: String) = repo.getDeliveryWithGrades(deliverNo)
    suspend fun getDeliveryWithBales(deliverNo: String) = repo.getDeliveryWithBales(deliverNo)
    suspend fun getBaleDetails(baleId: String) = repo.getBaleDetails(baleId)
    suspend fun saveBaleDetails(bale: MtnBale) = repo.saveBaleDetails(bale)
    suspend fun fetchOfflineDeliveryDetails() = repo.fetchOfflineDeliveryDetails()
    suspend fun fetchOfflineDeliveryList() = repo.getDeliverysInOffine()
    suspend fun updateDeliverySaveOffline(deliveryNo: String) = repo.updateDeliverySaveOffline(deliveryNo)
    suspend fun deleteBales(deliveryNo: String) = repo.deleteBales(deliveryNo)

    fun fetchDeliveryDetails() = viewModelScope.launch(dispatchers.main) {
        _deliveyDetails.removeSource(deliveyDetailsSource)
        withContext(dispatchers.io) {
            deliveyDetailsSource = repo.fetchDeliveryDetails()
        }
        _deliveyDetails.addSource(deliveyDetailsSource) {
            _deliveyDetails.value = it
        }
    }

    fun validateBale(baleId: String, deliveryNumber: String) = viewModelScope.launch(dispatchers.main) {
        _validateBale.removeSource(validateBaleSource)
        withContext(dispatchers.io) {
            validateBaleSource = repo.validateBale(baleId, deliveryNumber)
        }
        _validateBale.addSource(validateBaleSource) {
            _validateBale.value = it
        }
    }

    fun postDispatch(deliveryDto: MtnDispatchDelivery) = viewModelScope.launch(dispatchers.main) {
        _postDispatch.removeSource(postDispatchSource)
        withContext(dispatchers.io) {
            postDispatchSource = repo.postDispatch(deliveryDto)
        }
        _postDispatch.addSource(postDispatchSource) {
            _postDispatch.value = it
        }
    }
}
