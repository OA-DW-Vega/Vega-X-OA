package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.PortReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnGrades

class ReceivingModel(val dao: PortReceivingDao) {

    fun insertOrReplaceMtns(mtns: List<PortMtn>) {
        dao.insertOrReplaceMtns(mtns)
    }

    fun insertOrReplaceMtn(mtn: PortMtn) {
        dao.insertOrReplaceMtn(mtn)
    }

    fun insertOrReplaceMtnBales(mtnBales: List<PortMtnBales>) {
        dao.insertOrReplaceMtnBales(mtnBales)
    }

    fun insertOrReplaceMtnBale(mtnBale: PortMtnBales) {
        dao.insertOrReplaceMtnBale(mtnBale)
    }

    fun insertOrReplaceMtnGrade(mtnGrade: PortMtnGrades) {
        dao.insertOrReplaceMtnGrade(mtnGrade)
    }

    fun deleteAllMtnBales() {
        dao.deleteAllMtnBales()
    }

    fun deleteAllMtns() {
        dao.deleteAllMtns()
    }

    fun getVerifiedBales(mtnNumber: String): LiveData<List<PortMtnBales>> {
        return dao.getVerifiedBales(mtnNumber)
    }

    fun updateMtn(mtn: PortMtn) = dao.updateMtn(mtn)

    fun updateMtnBales(mtnBale: PortMtnBales) {
        dao.updateMtnBale(mtnBale)
    }

    fun getBale(mtnNumber: String, baleId: String, lineItem: String) =
        dao.getBale(mtnNumber, baleId)

    fun getBaleByBaleId(baleId: String): LiveData<PortMtnBales> {
        return dao.getBaleByBaleId(baleId)
    }

    fun getMtnWithBales(mtnId: String) = dao.getMtnWithBales(mtnId)

    fun getOfflineMtnWithBales() = dao.getOfflineMtnWithBales()

    fun getOfflineBales(mtnId: String) = dao.getOfflineBales(mtnId)

    fun getMtnsWithBales() = dao.getAllMtnWithBales()

    fun updateOfflineMtnStatus(mtnId: String) {
        dao.updateMtnStatus(mtnId)
        dao.updateMtnBalesStatus(mtnId,"1001")
    }

    fun updateOfflineVerifiedMtnStatus(
        mtnId: String,
        verifiedBales: List<PortMtnBales>?
    ) {
        dao.updateMtnStatus(mtnId)
        verifiedBales?.forEach { dao.updateVerifiedMtnBalesStatus(it.baleId) }
    }

    fun updateOfflineMtnSReverttatus(mtnId: String) {
        dao.updateMtnRevertStatus(mtnId)
        dao.updateMtnBalesRevertStatus(mtnId)
    }

    fun getMtns() = dao.getMtns()

    fun deleteMtnsByMtnId(mtnId: String) = dao.deleteMtnsByMtnId(mtnId)

    fun deleteMtnBalesByMtnId(mtnId: String) = dao.deleteMtnBalesByMtnId(mtnId)

    fun getMtnsAsList() = dao.getMtnsAsList()

    fun updateMtnToDeleteStatus(mtnNumber: String) = dao.updateMtnRevertStatus(mtnNumber)

    fun getGrades(mtnNumber: String) = dao.getGrades(mtnNumber)
}
