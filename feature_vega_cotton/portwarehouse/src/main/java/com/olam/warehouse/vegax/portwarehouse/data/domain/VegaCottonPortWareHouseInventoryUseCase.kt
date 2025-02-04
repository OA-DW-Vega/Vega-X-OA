package com.olam.warehouse.vegax.portwarehouse.data.domain

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.ChangeBaleStatus
import com.olam.warehouse.vegax.portwarehouse.data.repository.InventoryRepository

class VegaCottonPortWareHouseInventoryUseCase  (private val repository: InventoryRepository
) {
    suspend fun getPileList()=repository.getPileList()
    suspend fun syncBaleByGrade(grade: String)=repository.syncBaleByGrade(grade)
    suspend fun getInventoryGrades(selectedPileList: List<String>)=repository.getInventoryGrades(selectedPileList)
    suspend fun fetchInventoryBaleList(
        baleNo: String,
        baleTypeList: ArrayList<String>,
        gradeList: ArrayList<String>,
        pageNo: Int,
        pageSize: Int,
        marks: ArrayList<String>,
        years: ArrayList<String>
    )=repository.fetchInventoryBaleList(baleNo,baleTypeList,gradeList,pageNo,pageSize,marks,years)
    suspend fun  changeBaleStatus(changeBaleStatus: ChangeBaleStatus)=repository.chageBaleStatus(changeBaleStatus)
}

