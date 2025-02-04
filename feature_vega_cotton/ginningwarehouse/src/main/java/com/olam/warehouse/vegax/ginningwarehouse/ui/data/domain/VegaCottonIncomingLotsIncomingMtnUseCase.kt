package com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain

import com.olam.warehouse.ginning.utils.enums.BaleStatus
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.IncomingLotRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.IncomingMtnRepository

class VegaCottonIncomingLotsIncomingMtnUseCase (private val repository: IncomingMtnRepository
) {
    suspend fun getMtnWithBales(mtnId: String)=repository.getMtnWithBales(mtnId)
    suspend fun   updateMtnBale(mtnBale: MtnBales)=repository.updateMtnBale(mtnBale)
    suspend fun getMtns()=repository.getMtns()
    suspend fun getMtnsOffline()=repository.getMtnsOffline()
    suspend fun getMtnsWithGradesOffline()=repository.getMtnsWithGradesOffline()
    suspend fun insertMtn(mtn: Mtn)=repository.insertMtn(mtn)
    suspend fun deleteMtnsByMtnId(mtnId: String)=repository.deleteMtnsByMtnId(mtnId)
    suspend fun updateMtn(mtn: Mtn) = repository.updateMtn(mtn)
    suspend   fun updateMtnBalesStatusVerified(mtnId: String)=repository. updateMtnBalesStatusVerified(mtnId)
    suspend fun getGrades(mtnNumber: String)   =repository.getGrades(mtnNumber)
    suspend fun getOfflineMtnWithBales()=repository.getOfflineMtnWithBales()
    suspend  fun updateOfflineMtnSReverttatus(mtnId: String)=repository.updateOfflineMtnSReverttatus(mtnId)
    suspend fun getVerifiedBales(mtnNumber: String)=repository.getVerifiedBales(mtnNumber)
    suspend fun getBale(mtnNumber: String, baleId: String, lineItem: String)=repository.getBale(mtnNumber,baleId,lineItem)
    suspend fun updateMtnToDeleteStatus(mtnNumber: String)=repository.updateMtnToDeleteStatus(mtnNumber)
    suspend fun postMtnWithBales(mtn: Mtn)=repository.postMtnWithBales(mtn)
    suspend fun deleteMtnsbyMtnId(mtnId: String)=repository.deleteMtnsByMtnId(mtnId)
}

