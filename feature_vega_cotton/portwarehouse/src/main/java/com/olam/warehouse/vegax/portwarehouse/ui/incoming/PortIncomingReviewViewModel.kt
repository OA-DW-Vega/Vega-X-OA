package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortIncomingRepository
import com.olam.warehouse.vegax.portwarehouse.utils.enums.BaleStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */

class PortIncomingReviewViewModel(val repo: PortIncomingRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var mtnWithBales: MtnWithBales? = null
    private var mtnBales: List<PortMtnBales>? = null

    private var offloadBalesSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _offloadBales = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val offloadBales: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _offloadBales

    fun deleteMtnsByMtnId(mtnId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            repo.deleteMtnsByMtnId(mtnId)
        }
    }

    suspend fun setMtnWithBales(mtnNumber: String) {
        mtnWithBales = repo.getMtnWithBales(mtnNumber)
        mtnBales = mtnWithBales?.bales

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

    fun getVerifiedBales(): List<PortMtnBales>? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 1 }
    }

    fun getYetToVerifyCount(): String? {
        return mtnBales?.filter { mtnBale -> mtnBale.isVerified == 0 }?.size?.toString()
    }


    fun postMtnWithBales(mtnModel: PortMtn) = viewModelScope.launch(dispatchers.main) {
        _offloadBales.removeSource(offloadBalesSource)
        withContext(dispatchers.io) {
            offloadBalesSource = repo.postMtnWithBales(mtnModel)
        }
        _offloadBales.addSource(offloadBalesSource) {
            _offloadBales.value = it
        }
    }

    fun updateOfflineMtnStatus(verifiedBales: List<PortMtnBales>?) =
        mtnWithBales?.mtn?.mtnNumber?.let { repo.updateOfflineVerifiedMtnStatus(it, verifiedBales) }

    fun getMtnModel(): PortMtn? {
        val mtn = mtnWithBales?.mtn
        val bales = mtnWithBales?.bales?.filter { it.isVerified == 1 }

        mtn?.let {
            return PortMtn(
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


    /* fun deleteAllMtns() = repo.deleteAllMtns()

     fun deleteAllMtnBales() = repo.deleteAllMtnBales()

     fun deleteMtnsByMtnId(mtnId: String) = repo.deleteMtnsbyMtnId(mtnId)*/

}
