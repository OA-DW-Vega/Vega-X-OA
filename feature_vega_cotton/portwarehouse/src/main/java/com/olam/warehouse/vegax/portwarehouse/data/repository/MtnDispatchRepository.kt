package com.olam.warehouse.vegax.portwarehouse.data.repository

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.MtnDispatchDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.DeliveryWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDeliveryWithGrades
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDispatchPostResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.portwarehouse.data.api.MtnDispatchApi

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
interface MtnDispatchRepository {
    suspend fun getDeliveryWithGrades(deliverNo: String): MtnDeliveryWithGrades
    suspend fun getDeliveryWithBales(deliverNo: String): DeliveryWithBales
    suspend fun getBaleDetails(baleId: String): MtnBale
    suspend fun saveBaleDetails(bale: MtnBale)
    suspend fun fetchOfflineDeliveryDetails(): List<MtnDispatchDelivery>
    suspend fun getDeliverysInOffine(): List<MtnDispatchDelivery>
    suspend fun fetchDeliveryDetails(): LiveData<Resource<GenericReqAndResp<List<MtnDispatchDelivery>>>>
    suspend fun validateBale(baleId: String, deliveryNumber: String): LiveData<Resource<GenericReqAndResp<MtnBale>>>
    suspend fun updateDeliverySaveOffline(deliveryNo: String)
    suspend fun deleteBales(deliveryNo: String)
    suspend fun insertOrReplaceGrade(grade: MtnGrade)
    suspend fun insertOrReplaceDelivery(ot: MtnDispatchDelivery)
    suspend fun isDeliveryExist(deliveryNo: String): List<MtnDispatchDelivery>
    suspend fun postDispatch(deliveryDto: MtnDispatchDelivery): LiveData<Resource<GenericReqAndResp<MtnDispatchPostResponse>>>
}

class MtnDispatchRepositoryImpl(private val api: MtnDispatchApi, private val dao: MtnDispatchDao) :
    MtnDispatchRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun isDeliveryExist(deliveryNo: String) = dao.isDeliveryExist(deliveryNo)
    override suspend fun getDeliveryWithGrades(deliverNo: String) =
        dao.getDeliveryWithGrades(deliverNo)

    override suspend fun getDeliveryWithBales(deliverNo: String): DeliveryWithBales =
        dao.getDeliveryWithBales(deliverNo)

    override suspend fun insertOrReplaceGrade(grade: MtnGrade) = dao.insertOrReplaceGrade(grade)
    override suspend fun insertOrReplaceDelivery(ot: MtnDispatchDelivery) =
        dao.insertOrReplaceDelivery(ot)

    override suspend fun getBaleDetails(baleId: String): MtnBale = dao.getBaleDetails(baleId)
    override suspend fun saveBaleDetails(bale: MtnBale) = dao.saveBaleDetails(bale)
    override suspend fun fetchOfflineDeliveryDetails(): List<MtnDispatchDelivery> =
        dao.fetchOfflineDeliveryDetails()

    override suspend fun getDeliverysInOffine(): List<MtnDispatchDelivery> =
        dao.getDeliverysInOffine()

    override suspend fun fetchDeliveryDetails(): LiveData<Resource<GenericReqAndResp<List<MtnDispatchDelivery>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<MtnDispatchDelivery>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<MtnDispatchDelivery>> =
                api.fetchDeliveryDetails(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun validateBale(
        baleId: String,
        deliveryNumber: String
    ): LiveData<Resource<GenericReqAndResp<MtnBale>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<MtnBale>>() {
            override suspend fun createCall(): GenericReqAndResp<MtnBale> =
                api.validateBale(getCurrentKey(), baleId, deliveryNumber)
        }.build().asLiveData()
    }

    override suspend fun updateDeliverySaveOffline(deliveryNo: String) =
        dao.updateDeliverySaveOffline(deliveryNo)

    override suspend fun deleteBales(deliveryNo: String) {
        dao.deleteBales(deliveryNo)
        dao.deleteMtnDispatchDelivery(deliveryNo)
    }

    override suspend fun postDispatch(deliveryDto: MtnDispatchDelivery): LiveData<Resource<GenericReqAndResp<MtnDispatchPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<MtnDispatchPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<MtnDispatchPostResponse> =
                api.postDispatch(getCurrentKey(), deliveryDto)
        }.build().asLiveData()
    }
}
