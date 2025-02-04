package com.olam.warehouse.ginning.db.model

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.ReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnGrades

class ReceivingModel(val dao: ReceivingDao) {

    fun insertOrReplaceMtns(mtns: List<Mtn>) {
        dao.insertOrReplaceMtns(mtns)
    }

    suspend  fun insertOrReplaceMtn(mtn: Mtn) {
        dao.insertOrReplaceMtn(mtn)
    }

    suspend fun insertOrReplaceMtnBales(mtnBales: List<MtnBales>) {
        dao.insertOrReplaceMtnBales(mtnBales)
    }

    suspend  fun insertOrReplaceMtnBale(mtnBale: MtnBales) {
        dao.insertOrReplaceMtnBale(mtnBale)
    }

    suspend  fun insertOrReplaceMtnGrade(mtnGrade: MtnGrades) {
        dao.insertOrReplaceMtnGrade(mtnGrade)
    }

    suspend    fun deleteAllMtnBales() {
        dao.deleteAllMtnBales()
    }

    suspend  fun deleteAllMtns() {
        dao.deleteAllMtns()
    }

    suspend  fun getVerifiedBales(mtnNumber: String): LiveData<List<MtnBales>> {
        return dao.getVerifiedBales(mtnNumber)
    }

    suspend fun updateMtn(mtn: Mtn) = dao.updateMtn(mtn)

    suspend fun updateMtnBales(mtnBale: MtnBales) {
        dao.updateMtnBale(mtnBale)
    }

    suspend fun getBale(mtnNumber: String, baleId: String, lineItem: String) =
        dao.getBale(mtnNumber, baleId)

    suspend fun getBaleByBaleId(baleId: String): LiveData<MtnBales> {
        return dao.getBaleByBaleId(baleId)
    }

    suspend fun getMtnWithBales(mtnId: String) = dao.getMtnWithBalesOffline(mtnId)

    suspend fun getOfflineMtnWithBales() = dao.getOfflineMtnWithBales()

    suspend fun getOfflineBales(mtnId: String) = dao.getOfflineBales(mtnId)

    suspend fun getMtnsWithBales() = dao.getAllMtnWithBales()


    suspend  fun updateOfflineMtnStatus(mtnId: String) {
        dao.updateMtnStatus(mtnId)
        dao.updateMtnBalesStatus(mtnId)
    }

    suspend   fun updateMtnBalesStatusVerified(mtnId: String) = dao.updateMtnBalesStatusVerified(mtnId)

    suspend  fun updateOfflineVerifiedMtnStatus(
        mtnId: String,
        verifiedBales: List<MtnBales>?
    ) {
        dao.updateMtnStatus(mtnId)
        verifiedBales?.forEach { dao.updateVerifiedMtnBalesStatus(it.baleId) }
    }

    suspend  fun updateOfflineMtnSReverttatus(mtnId: String) {
        dao.updateMtnRevertStatus(mtnId)
        dao.updateMtnBalesRevertStatus(mtnId)
    }

    suspend fun getMtns() = dao.getMtns()

    suspend fun deleteMtnsByMtnId(mtnId: String) = dao.deleteMtnsByMtnId(mtnId)

    suspend fun deleteMtnBalesByMtnId(mtnId: String) = dao.deleteMtnBalesByMtnId(mtnId)

    suspend  fun getMtnsAsList() = dao.getMtnsAsList()

    suspend fun updateMtnToDeleteStatus(mtnNumber: String) = dao.updateMtnRevertStatus(mtnNumber)

    suspend fun getGrades(mtnNumber: String) = dao.getGrades(mtnNumber)
}
