package com.olam.warehouse.vegax.ginningwarehouse.data.domain

import com.olam.warehouse.ginning.data.repo.GinningDispatchRepository
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Grade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery

class VegaCottonDispatchUseCase
    (private val repository: GinningDispatchRepository
) {
    suspend fun fetchDeliveryDetails() = repository.fetchDeliveryDetails()
    suspend fun insertOrReplaceDelivery(ot: VegaCottonGinningDispatchDelivery) = repository.insertOrReplaceDelivery(ot)
    suspend fun insertOrReplaceGrade(grade: Grade) = repository.insertOrReplaceGrade(grade)
    suspend fun fetchDeliveryDetailsOffline() = repository.fetchDeliveryDetailsOffline()
    suspend fun getDeliveryWithGrades(deliverNo: String) = repository.getDeliveryWithGrades(deliverNo)
    suspend fun getDeliveryWithBales(deliverNo: String) = repository.getDeliveryWithBales(deliverNo)
    suspend fun getAllDeliveryWithBales() = repository.getAllDeliveryWithBales()
    suspend fun fetchExistedDelivery() = repository.fetchExistedDelivery()
    suspend fun getBaleDetailsByBaleId(baleId: String) = repository.getBaleDetailsByBaleId(baleId)
    suspend fun getBaleDetailsByBaleIdOffline(baleId: String) = repository.getBaleDetailsByBaleIdOffline(baleId)
    suspend fun validateBale(baleId: String, deliverNo: String) = repository.validateBale(baleId, deliverNo)
    suspend fun saveBale(bale: Bale) = repository.saveBale(bale)
    suspend fun deleteBale(deliverNo: String) = repository.deleteBale(deliverNo)
    suspend fun postDispatch(dto: VegaCottonGinningDispatchDelivery) = repository.postDispatch(dto)
    suspend fun updateDeliverySaveOffline(deliveryNo: String) = repository.updateDeliverySaveOffline(deliveryNo)
    suspend fun fetchOfflineDeliveryDetails() = repository.fetchOfflineDeliveryDetails()
    suspend fun updateDeliveryStatusToEdit(deliveryNo: String) = repository.updateDeliveryStatusToEdit(deliveryNo)
    suspend fun deleteOfflineDeliveryWithBales(deliveryNumber: String) =
        repository.deleteOfflineDeliveryWithBales(deliveryNumber)

    suspend fun updateErrorMessage(msg: String, deliveryNumber: String) =
        repository.updateErrorMessage(msg, deliveryNumber)
}
