package com.olam.warehouse.vegax.sweepingcocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingPost
import com.olam.warehouse.vegax.sweepingcocoa.data.repo.VegaCocoaSweepingRepository

class VegaCocoaSweepingUseCase(private val repo: VegaCocoaSweepingRepository) {
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repo.getConfigItems(role)

    suspend fun getLotDetails(charge: String, material: String, whId: String) =
        repo.getLotInfo(charge, material, whId)

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repo.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repo.deleteBagDetails(bagMaterial)
    suspend fun getBagItems(batchNumber: String) = repo.getBagItems(batchNumber)
    suspend fun postSweeping(vegaSweepingPost: VegaSweepingPost) = repo.postSweeping(vegaSweepingPost)
    suspend fun updateDataToDB(msg: String, status: Int, batchNumber: String) =
        repo.updateDataToDB(msg, status, batchNumber)
}
