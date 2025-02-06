package com.olam.warehouse.master.vegaecuador.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.presentation.enums.Status
import java.util.*

@Dao
abstract class VegaEcuadorDispatchDao {

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDispatchDetail(wbList: VegaEcuadorDispatch)

    @Transaction
    @Query("DELETE FROM VegaEcuadorDispatch WHERE purchaseDocNum = :whId")
    abstract fun deleteDispatchDetail(whId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: List<VegaEcuadorDispatchLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveStocks(item: List<VegaEcuadorDispatchStocks>)

    @Transaction
    @Query("DELETE FROM VegaEcuadorDispatchStocks")
    abstract fun deleteStocks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun savePurchaseOrders(item: List<VegaEcuadorDispatchPurchaseOrders>)

    @Transaction
    @Query("DELETE FROM VegaEcuadorDispatchPurchaseOrders")
    abstract fun deletePurcheseOrder()

    @Query("SELECT * FROM VegaEcuadorDispatchPurchaseOrders WHERE warehouseId = :plantId")
    abstract fun getPurchaseOrders(plantId: String): LiveData<List<VegaEcuadorDispatchPurchaseOrders>>

    @Query("SELECT * FROM VegaEcuadorDispatchPurchaseOrders")
    abstract fun getIndoPurchaseOrders(): LiveData<List<VegaEcuadorDispatchPurchaseOrders>>

    @Query("SELECT * FROM VegaEcuadorDispatchStocks")
    abstract fun getStocks(): LiveData<List<VegaEcuadorDispatchStocks>>

    @Query("SELECT * FROM VegaEcuadorDispatchLots WHERE wbTempId = :tmpId")
    abstract fun getLots(tmpId: String): LiveData<List<VegaEcuadorDispatchLots>>

    @Query("SELECT * FROM VegaEcuadorDispatchLots WHERE batchNumber = :batchNumber and wbTempId = :tmpId")
    abstract fun validateLotAlreadyAdded(batchNumber: String, tmpId: String): VegaEcuadorDispatchLots

    @Query("UPDATE VegaEcuadorDispatch SET remarks =:remark and isStarted =:isStart WHERE purchaseDocNum =:whId")
    abstract suspend fun updateRemark(remark: String, isStart: Boolean, whId: String)

    @Query("DELETE FROM VegaEcuadorDispatchLots where batchNumber = :batchNumber and wbTempId = :tmpId")
    abstract fun deleteLot(batchNumber: String, tmpId: String)

    @Query("SELECT * FROM VegaEcuadorDispatchStocks WHERE batchNumber = :batchNumber")
    abstract fun getLotDetail(batchNumber: String): LiveData<List<VegaEcuadorDispatchStocks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveDispatchLotLineItems(lotList: ArrayList<VegaEcuadorDispatchLots>)

    @Query("SELECT * FROM VegaEcuadorDispatch")
    abstract fun getDispatchWithLineItem(): LiveData<List<VegaEcuadorDispatchWithLineItems>>

    @Query("SELECT * FROM VegaEcuadorDispatch where isSynced = 0")
    abstract fun getDispatchWithLineItemCount(): LiveData<List<VegaEcuadorDispatchWithLineItems>>

    @Query("DELETE FROM VegaEcuadorDispatch where wbTempId = :wbTempId")
    abstract fun deleteDispatchItem(wbTempId: String)

    @Query("DELETE FROM VegaEcuadorDispatchLots where wbTempId = :wbTempId")
    abstract fun deleteDispatchLots(wbTempId: String)

    @Query("SELECT * FROM VegaEcuadorDispatch WHERE wbTempId = :wbid")
    abstract fun getDispatchWithLineItem(wbid: String): VegaEcuadorDispatchWithLineItems

    @Query("UPDATE VegaEcuadorDispatch SET status = :status, deliveryId = :deliveryId, syncStatusMsg = :syncStatusMsg, isSynced = :isSynced, binFormation = :binFormation, delivery = :delivery, pgi = :pgi, picking = :picking where wbTempId = :wbTempId")
    abstract fun updateDispatchStatus(
        wbTempId: String,
        status: Status,
        deliveryId: String,
        syncStatusMsg: String,
        isSynced: Boolean,
        binFormation: Boolean,
        delivery: Boolean,
        pgi: Boolean,
        picking: Boolean
    )

    @Query("SELECT * FROM VegaCocoaPurchaseOrders")
    abstract fun getPurchaseOrderOffline(): LiveData<List<VegaCocoaPurchaseOrders>>
    @Query("SELECT * FROM VegaFeatureMaster WHERE moduleName = :module")
    abstract fun getFeatureMaster(module: String): LiveData<List<VegaFeatureMaster>>
}
