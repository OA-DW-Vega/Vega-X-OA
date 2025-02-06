package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnWithBales
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsIncomingMtnUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.enums.BaleStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GinningIncomingReviewViewModel(private val useCase: VegaCottonIncomingLotsIncomingMtnUseCase,
                                     private val dispatchers: AppDispatchers
) :
        BaseViewModel() {
    private var mtnWithBales: MtnWithBales? = null
    private var mtnBales: List<MtnBales>? = null

    private var getMtnWithBalesOfflineSource:  LiveData<MtnWithBales> = MutableLiveData()
    private val _getMtnWithBalesOffline= MediatorLiveData<MtnWithBales>()
    val getMtnWithBalesOffline: LiveData<MtnWithBales> get() = _getMtnWithBalesOffline

    private var  postMtnWithBalesSource:  LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
            MutableLiveData()
    private val _postMtnWithBales = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val  postMtnWithBales: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _postMtnWithBales

    fun postMtnWithBales(mtn: Mtn) = viewModelScope.launch(dispatchers.main) {
        _postMtnWithBales.removeSource(postMtnWithBalesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postMtnWithBalesSource = useCase.postMtnWithBales(mtn)
        }
        _postMtnWithBales.addSource(postMtnWithBalesSource) {
            _postMtnWithBales.value = it
        }
    }
    fun getMtnWithBales(mtnId: String) = viewModelScope.launch(dispatchers.main) {
        _getMtnWithBalesOffline.removeSource(getMtnWithBalesOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getMtnWithBalesOfflineSource = useCase.getMtnWithBales(mtnId)
        }
        _getMtnWithBalesOffline.addSource(getMtnWithBalesOfflineSource) {
            _getMtnWithBalesOffline.value = it
        }
    }

    fun setMtnWithBales(mtnWithBales: MtnWithBales) {
        this.mtnWithBales = mtnWithBales
        mtnBales = mtnWithBales.bales
    }


    fun getBaleGoodCount(): String? {
        return mtnBales?.filter { mtnBale ->
            mtnBale.baleStatus == BaleStatus.Good.status && mtnBale.isVerified == 1
        }?.size?.toString()
    }

    fun getBaleCottonCleanCount(): String? {
        return mtnBales?.filter { mtnBale ->
            mtnBale.baleStatus == BaleStatus.CottonClean.status && mtnBale.isVerified == 1
        }?.size?.toString()
    }

    fun getBaleCottonDirtyCount(): String? {
        return mtnBales?.filter { mtnBale ->
            mtnBale.baleStatus == BaleStatus.CottonDirty.status && mtnBale.isVerified == 1
        }?.size?.toString()
    }

    fun getBaleTieDamageCount(): String? {
        return mtnBales?.filter { mtnBale ->
            mtnBale.baleStatus == BaleStatus.TieDamage.status && mtnBale.isVerified == 1
        }?.size?.toString()
    }

    fun getWetBaleCount(): String? {
        return mtnBales?.filter { mtnBale ->
            mtnBale.baleStatus == BaleStatus.WetBale.status && mtnBale.isVerified == 1
        }?.size?.toString()
    }

    fun getNoBaleTagCount(): String? {
        return mtnBales?.filter { mtnBale ->
            mtnBale.baleStatus == BaleStatus.NoBaleTag.status && mtnBale.isVerified == 1
        }?.size?.toString()
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


   /* fun updateOfflineMtnStatus(verifiedBales: List<MtnBales>?) =
        mtnWithBales?.mtn?.mtnNumber?.let { repo.updateOfflineVerifiedMtnStatus(it, verifiedBales) }*/

     fun getMtnModel(): Mtn? {
        val mtn = mtnWithBales?.mtn
        val bales = mtnWithBales?.bales?.filter { it.isVerified == 1 }
        bales?.forEach { it.blineItem = it.lineItem }

        mtn?.let {
            return Mtn(
                mtnNumber = it.mtnNumber,
                suplierPlantId = it.suplierPlantId,
                suplierPlantDesc = it.suplierPlantDesc,
                recievedPlantId = it.recievedPlantId,
                recievedPlantDesc = it.recievedPlantDesc,
                lineItem = it.lineItem,
                containerNo = it.containerNo,
                grade = it.grade,
                uom = it.uom,
                sourceNetWeight = it.sourceNetWeight,
                baleCount = it.baleCount,
                truckNumber = it.truckNumber,
                istogrnPost = "X",
                itransferPost = "",
                mtnBales = bales

            )
        }
        return null
    }


   // fun deleteAllMtns() = repo.deleteAllMtns()

    //fun deleteAllMtnBales() = repo.deleteAllMtnBales()

    fun deleteMtnsByMtnId(mtnId: String)= viewModelScope.launch(dispatchers.main) {
          withContext(dispatchers.io) {
              useCase.deleteMtnsByMtnId(mtnId)
        }
    }

}
