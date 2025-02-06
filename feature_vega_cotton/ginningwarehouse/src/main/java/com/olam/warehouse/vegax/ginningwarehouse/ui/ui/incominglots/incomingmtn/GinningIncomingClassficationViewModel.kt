package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnWithBales
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsIncomingMtnUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GinningIncomingClassficationViewModel(private val useCase: VegaCottonIncomingLotsIncomingMtnUseCase,
                                            private val dispatchers: AppDispatchers
) :  BaseViewModel() {

    private var verifiedBales:List<MtnBales> = listOf()
    private var mtnWithBales: MtnWithBales? = null
    private var mtnBales: List<MtnBales>? = null

    private var getVerifiedBalesSource:  LiveData<List<MtnBales>> = MutableLiveData()
    private val _getVerifiedBales = MediatorLiveData<List<MtnBales>>()
    val getVerifiedBales: LiveData<List<MtnBales>> get() = _getVerifiedBales

    private var getMtnWithBalesOfflineSource:  LiveData<MtnWithBales> = MutableLiveData()
    private val _getMtnWithBalesOffline= MediatorLiveData<MtnWithBales>()
    val getMtnWithBalesOffline: LiveData<MtnWithBales> get() = _getMtnWithBalesOffline


    private var getBaleSource:  LiveData<MtnBales> = MutableLiveData()
    private val _getBale= MediatorLiveData<MtnBales>()
    val getBale: LiveData<MtnBales> get() = _getBale

    fun getMtnWithBales(mtnId: String) = viewModelScope.launch(dispatchers.main) {
        _getMtnWithBalesOffline.removeSource(getMtnWithBalesOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getMtnWithBalesOfflineSource = useCase.getMtnWithBales(mtnId)
        }
        _getMtnWithBalesOffline.addSource(getMtnWithBalesOfflineSource) {
            _getMtnWithBalesOffline.value = it
        }
    }
    fun getBale(mtnNumber: String, baleId: String, lineItem: String)= viewModelScope.launch(dispatchers.main) {
        _getBale.removeSource(getBaleSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getBaleSource = useCase.getBale(mtnNumber,baleId,lineItem)
        }
        _getBale.addSource(getBaleSource) {
            _getBale.value = it
        }
    }
    fun setMtnWithBales(mtnWithBales: MtnWithBales) {
        mtnBales = mtnWithBales.bales
    }

    fun deleteBale(
        mtnBale: MtnBales,
        itemCount: Int
    )  = viewModelScope.launch(dispatchers.main) {
        mtnBale.isVerified = 0
        mtnBale.isOfflineData = false
        withContext(dispatchers.io) {
            useCase.updateMtnBale(mtnBale)
            if (itemCount == 1) useCase.updateMtnToDeleteStatus(mtnBale.mtnNumber)
        }

    }

    fun getVerifiedBales(mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        _getVerifiedBales.removeSource(getVerifiedBalesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getVerifiedBalesSource = useCase.getVerifiedBales(mtnNumber)
        }
        _getVerifiedBales.addSource(getVerifiedBalesSource) {
            _getVerifiedBales.value = it
        }
    }

    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(0, 9).noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun getTotalCount(): String? {

        return mtnWithBales?.mtn?.baleCount
    }

    fun getVerifiedCount(): String? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 1 }?.size?.toString()
    }

    fun getVerifiedBales(): List<MtnBales>? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 1 }
    }

    fun getYetToVerifyCount(): String? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 0 }?.size?.toString()
    }
}
