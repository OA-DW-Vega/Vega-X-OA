package com.olam.warehouse.vegax.portwarehouse.data.domain

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortIncomingRepository

class VegaCottonPortWareHouseIncomingUseCase(
    private val repository: PortIncomingRepository
) {
    //suspend fun getMtns() = repository.getMtns()
    suspend fun getMtnsOffline() = repository.getMtnsOffline()
    suspend fun insertMtn(mtn: PortMtn) = repository.insertMtn(mtn)
    suspend fun getMtnsWithGradesOffline() = repository.getMtnsWithGradesOffline()
    suspend fun updateMtn(mtn: PortMtn) = repository.updateMtn(mtn)
    suspend fun updateOfflineMtnStatus(mtnId: String) = repository.updateOfflineMtnStatus(mtnId,"1001")
    suspend fun getMtnWithBales(mtnId: String) = repository.getMtnWithBales(mtnId)
    suspend fun postMtnWithBales(mtn: PortMtn) = repository.postMtnWithBales(mtn)
    suspend fun deleteMtnsByMtnId(mtnId: String) = repository.deleteMtnsByMtnId(mtnId)
    suspend fun getMtnsWithBales()=repository.getMtnsWithBales()
}
