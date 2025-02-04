package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.master.vega.model.VegaDispatchWithLots

/**
 * Created by Keerthi Santhanam on 2/17/2020.
 */
@Dao
abstract class VegaDispatchDao {
    @Query("SELECT * FROM VegaDispatchTrucks WHERE isSyncStatus = 0 and isOfflineData = 0")
    abstract suspend fun getDispatchTruckListDetails(): List<VegaDispatchTrucks>

    @Query("SELECT * FROM VegaDispatchTrucks WHERE weighBridgeId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<VegaDispatchTrucks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDispatchTruckDetail(wbList: VegaDispatchTrucks)

    fun save(item: List<VegaDispatchTrucks>) {
        deleteVegaDispatchTrucks()
        item.forEach {
            if (isWBExist(it.weighBridgeId).isNotEmpty()) return@forEach
            it.wbTempId = it.weighBridgeId
            it.supplierCode = it.supplierCode
            insertDispatchTruckDetail(it)
        }
    }

    @Transaction
    @Query("DELETE FROM VegaDispatchTrucks WHERE isSyncStatus = 0")
    abstract fun deleteVegaDispatchTrucks()

    @Query("DELETE FROM VegaDispatchTrucks  where weighBridgeId = :wbid")
    abstract fun updateDB(wbid: String)

    @Query("UPDATE VegaDispatchTrucks SET isOfflineData = 1, batchNumber =:bid where weighBridgeId = :wbid")
    abstract fun updateWBListStatus(wbid: String, bid: String)

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveStock(item: List<VegaDispatchLots>)

    @Query("DELETE FROM VegaDispatchLots where materialCode = :material")
    abstract fun deleteStocks(material: String)

    @Query("SELECT * FROM VegaDispatchLots WHERE materialCode = :material")
    abstract suspend fun getStocks(material: String): List<VegaDispatchLots>

    @Query("SELECT * FROM VegaDispatchLots WHERE materialCode = :material")
    abstract fun getStocksOfflineSingle(material: String): LiveData<List<VegaDispatchLots>>

    @Query("UPDATE VegaDispatchLots SET isAdded = 0 where batchNumber = :batchNumber")
    abstract fun updateLot(batchNumber: String)

    @Query("UPDATE VegaDispatchLots SET isAdded = 0, weighBridgeId = :empty where weighBridgeId = :wbid")
    abstract fun updateLotStatus(wbid: String, empty: String)

    @Query("UPDATE VegaDispatchTrucks SET isSyncStatus = :syncStatus, status = :status, message = :msg, batchNumber =:batchNumber where weighBridgeId = :weighBridgeId")
    abstract fun updateDispatchStatus(
        weighBridgeId: String,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        batchNumber: String
    )

    @Query("SELECT * FROM VegaDispatchTrucks")
    abstract fun getDispatchWithLots(): LiveData<List<VegaDispatchWithLots>>

    @Query("SELECT * FROM VegaDispatchTrucks where weighBridgeId = :weighBridgeId")
    abstract fun getSingleDispatchWithLots(weighBridgeId: String): VegaDispatchWithLots

    @Query("DELETE FROM VegaDispatchTrucks WHERE weighBridgeId = :weighBridgeId")
    abstract fun deleteVegaDispatchTruckItem(weighBridgeId: String)

}
