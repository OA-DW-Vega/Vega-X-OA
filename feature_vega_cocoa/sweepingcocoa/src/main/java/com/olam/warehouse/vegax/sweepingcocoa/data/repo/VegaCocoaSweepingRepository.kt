package com.olam.warehouse.vegax.sweepingcocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaSweepingDao
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.sweepingcocoa.data.api.VegaCocoaSweepingApi
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaCocoaSweepingLots
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingPost
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingResponse

interface VegaCocoaSweepingRepository {
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    suspend fun getLotInfo(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSweepingLots>>>>

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)
    suspend fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)
    suspend fun getBagItems(batchNumber: String): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun postSweeping(vegaSweepingPost: VegaSweepingPost): LiveData<Resource<GenericReqAndResp<VegaSweepingResponse>>>
    suspend fun updateDataToDB(msg: String, status: Int, batchNumber: String)
}

class VegaCocoaSweepingRepositoryImpl(
    private val api: VegaCocoaSweepingApi,
    private val dao: VegaCocoaSweepingDao,
    private val masterDao: MasterDao
) : VegaCocoaSweepingRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun getLotInfo(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSweepingLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaSweepingLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaSweepingLots>> =
                api.getLoTInfo(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = dao.saveBagDetails(bagMaterial)

    override suspend fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) =
        dao.deleteBagDetails(bagMaterial.id, bagMaterial.batchNumber)

    override suspend fun getBagItems(batchNumber: String) = dao.getBagItems(batchNumber)

    override suspend fun postSweeping(vegaSweepingPost: VegaSweepingPost): LiveData<Resource<GenericReqAndResp<VegaSweepingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaSweepingResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaSweepingResponse> =
                api.postSweeping(vegaSweepingPost)
        }.build().asLiveData()
    }

    override suspend fun updateDataToDB(msg: String, status: Int, batchNumber: String) {
        when (status) {
            3 -> dao.updateDataToDB(msg, status, batchNumber, false)
            4 -> dao.updateDataToDB(msg, status, batchNumber, true)
        }

    }
}
