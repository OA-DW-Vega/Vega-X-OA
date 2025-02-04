package com.olam.warehouse.vegax.reconcilnicaragua.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.GrnDetailsResponse
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.QualitativeParams
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItems
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaReconcilCashMovement
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.reconcilnicaragua.data.repo.VegaNicaraguaReConcilReportRepository

class VegaNicaraguaReconcilReportUseCase(
    private val repository: VegaNicaraguaReConcilReportRepository
) {
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getGrnDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<GrnDetailsResponse>>>> =
        repository.getGrnDetails(vendorCode)

    suspend fun getAdvanceLineDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>>> =
        repository.geAdvanceLineItemDetails(vendorCode)

    suspend fun getReceivingWithLineItem(startTime: Long, endTime: Long): LiveData<List<VegaReceiving>> =
        repository.getReceivingWithLineItem(startTime, endTime)

    suspend fun getInvoiceOfflineData(startTime: Long, endTime: Long) =
        repository.getInvoiceOfflineData(startTime, endTime)

    suspend fun getTransactionAdvanceData(startTime: Long, endTime: Long) =
        repository.getTransactionAdvanceData(startTime, endTime)

    suspend fun insertOrUpdatereport(item: VegaNicaraguaReconcilCashMovement) = repository.updateReconReport(item)
    suspend fun getReconReport(): LiveData<VegaNicaraguaReconcilCashMovement> = repository.getReconReport()
    suspend fun getQualityGradesListWithDesc(): LiveData<List<QualitativeParams>> =
        repository.getQualityGradesListWithDesc()
}
