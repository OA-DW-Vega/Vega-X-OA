package com.olam.warehouse.vegax.stockrecon.data.domian.usecase

import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconCreateReconIdReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPrintRecipt
import com.olam.warehouse.vegax.stockrecon.data.repo.VegaStockReconRepo

class VegaStockReconUseCase(private val repo: VegaStockReconRepo) {

    suspend fun getStockList(materialList: ArrayList<String>, plantId: String) =
        repo.getStockList(materialList, plantId)

    suspend fun validateLot(batchNumber: String): VegaDispatchLots = repo.validateLot(batchNumber)
    suspend fun getLotDetails(charge: String, whId: String) =
        repo.getLotDetails(charge, whId)

    suspend fun getStockReconInProgressCompletedList(plantId: String, storageLocation: String) =
        repo.getStockReconInprogressCompletedList(plantId, storageLocation)

    suspend fun getStockReconId(request: VegaStockReconCreateReconIdReq) = repo.getStockReconId(request)

    suspend fun postAuditData(request: VegaStockReconPostAuditDataReq) = repo.postAuditData(request)

    suspend fun fetchAllAuditData(reconId: String) = repo.fetchAllAuditData(reconId)

    suspend fun updateReconIdStatus(reconId: String, status: String) = repo.updateReconIdStatus(reconId, status)

    suspend fun deleteAuditData(reconId: String, auditId: String) = repo.deleteAuditData(reconId, auditId)

    suspend fun postPrintRecipt(request: VegaStockReconPrintRecipt) = repo.postPrintRecipt(request)

    suspend fun getPackageMaterial() = repo.getSAPMaterials()

    suspend fun getVegaMaterials() = repo.getVegaMaterials()

    suspend fun getCustomLocations() = repo.getCustomLocations()

    suspend fun getReconReportReconList(plantId: String, fromDate: String, toDate: String) =
        repo.getReconReportReconList(plantId, fromDate, toDate)

    suspend fun getReconReportAuditDetails(reconId: String, auditId: String) =
        repo.getReconReportAuditDetails(reconId, auditId)

    suspend fun getReconReportAuditList(reconId: String)= repo.getReconReportAuditList(reconId)

    suspend fun getAuditImage(reconId: String, auditId: String) = repo.getAuditImage(reconId, auditId)

    suspend fun getReconReportImage(reconId: String) = repo.getReconReportImage(reconId)

}

