package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortIncomingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */
class PortIncomingClassficationViewModel(
    private val repo: PortIncomingRepository,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var verifiedBales: List<PortMtnBales> = listOf()
    private var mtnWithBales: MtnWithBales? = null
    private var mtnBales: List<PortMtnBales>? = null

    private var verifyMtnBalesSource: LiveData<List<PortMtnBales>> = MutableLiveData()
    private val _verifyMtnBales = MediatorLiveData<List<PortMtnBales>>()
    val verifyMtnBales: LiveData<List<PortMtnBales>> get() = _verifyMtnBales

    private var mtnClasifySource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _mtnClasify = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val mtnClasify: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _mtnClasify

    fun getMtnInClassification(mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        _mtnClasify.removeSource(mtnClasifySource)
        withContext(dispatchers.io) {
            mtnClasifySource = repo.getMtnInClassification(mtnNumber)
        }
        _mtnClasify.addSource(mtnClasifySource) {
            _mtnClasify.value = it
        }
    }

    fun getVerifiedBales(mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        _verifyMtnBales.removeSource(verifyMtnBalesSource)
        withContext(dispatchers.io) {
            verifyMtnBalesSource = repo.getVerifiedBales(mtnNumber)
        }
        _verifyMtnBales.addSource(verifyMtnBalesSource) {
            _verifyMtnBales.value = it
        }
    }

    fun setMtnWithBales(mtnNumber: String) {
        runBlocking {
            withContext(Dispatchers.IO) {
                mtnWithBales = repo.getMtnWithBales(mtnNumber)
            }
        }
        mtnBales = mtnWithBales?.bales
    }

    fun deleteBale(
        mtnBale: PortMtnBales,
        itemCount: Int
    ) {
        mtnBale.isVerified = 0
        mtnBale.isOfflineData = false
        repo.updateMtnBale(mtnBale)
        if (itemCount == 1) repo.updateMtnToDeleteStatus(mtnBale.mtnNumber)
    }

    // fun getVerifiedBales(mtnNumber: String) = repo.getVerifiedBales(mtnNumber)

    fun getBale(mtnNumber: String, baleId: String, lineItem: String) =
        repo.getBale(mtnNumber, baleId, lineItem)

    //fun getBaleByBaleId(baleId: String) = repo.getBaleByBaleId(baleId)

    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(0, 9)
                .noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun getTotalCount(): String? {
        return mtnWithBales?.mtn?.baleCount
    }

    fun getVerifiedCount(): String? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 1 }?.size?.toString()
    }

    fun getVerifiedBales(): List<PortMtnBales>? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 1 }
    }

    fun getYetToVerifyCount(): String? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 0 }?.size?.toString()
    }

//    fun getMtnInClassification(mtnNumber: String) = repo.getMtnInClassification(mtnNumber)
}
