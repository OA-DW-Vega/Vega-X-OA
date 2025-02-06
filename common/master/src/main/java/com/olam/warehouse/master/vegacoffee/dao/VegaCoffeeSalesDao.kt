package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacameroon.model.VegaCameroonMtntWithBagItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCameroonSalesWithBagItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots

@Dao
abstract class VegaCoffeeSalesDao {

    @Query("SELECT * FROM VegaCoffeeSalesOrder where saleOrderId = :soNumber and salesType = :salesType")
    abstract fun getDispatchSalesItem(
        soNumber: String,
        salesType: String
    ): LiveData<VegaCoffeeSalesOrderWithLots>

    @Query("SELECT * FROM VegaCoffeeSalesOrder where saleOrderId = :soNumber and salesType = :salesType")
    abstract fun getDispatchSummarySalesItem(
        soNumber: String,
        salesType: String
    ): LiveData<VegaCoffeeSalesOrderWithLots>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaCoffeeSalesOrder where saleOrderId = :soNumber and salesType = :salesType and salesTempId = :salesTempId")
    abstract fun getDispatchSalesItems(
        soNumber: String,
        salesType: String,
        salesTempId: String
    ): LiveData<VegaCoffeeSalesOrderWithLots>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSalesOrderDetail(salesOrder: VegaCoffeeSalesOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: List<VegaCoffeeSalesLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLot(item: VegaCoffeeSalesLots)

    @Query("SELECT * FROM VegaCoffeeSalesLots WHERE batchNumber = :batchNumber and isProgress = 0")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaCoffeeSalesLots

    @Query("DELETE FROM VegaCoffeeSalesLots where batchNumber = :batchNumber  and salesTempId = :salesTempId and materialCode = :materialCode")
    abstract fun removeLotDetails(batchNumber: String, salesTempId: String, materialCode: String)

    @Query("SELECT * FROM VegaCoffeeSalesBagMaterial where batchNumber = :batchNumber and materialCode =:materialCode and isSyncStatus = 0")
    abstract fun getBagItems(batchNumber: String, materialCode: String): LiveData<List<VegaCoffeeSalesBagMaterial>>

    @Query("SELECT * FROM VegaCoffeeSalesBagMaterial where batchNumber = :batchNumber and materialCode =:materialCode and salesTempId = :salesTempId and isSyncStatus = 0")
    abstract fun getCameroonSalesBagItems(batchNumber: String, materialCode: String, salesTempId: String): LiveData<List<VegaCoffeeSalesBagMaterial>>

    @Query("SELECT * FROM VegaCoffeeSalesBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCoffeeSalesBagMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial)

    @Query("DELETE FROM VegaCoffeeSalesBagMaterial WHERE id = :id and batchNumber = :batchNumber")
    abstract fun deleteBagDetails(id: Int, batchNumber: String)

    @Query("SELECT * FROM VegaCoffeeSalesOrder WHERE salesType =:type")
    abstract fun getPendingLotList(type: String): LiveData<List<VegaCoffeePendingSalesOrderWithLots>>

    @Query("UPDATE VegaCoffeeSalesLots SET editedWeight = :weight and weightToDispatchUOM = :UOM where batchNumber = :batchNumber and saleOrderId = :orderId and salesType = :type")
    abstract fun updateLotWeight(batchNumber: String, orderId: String, type: String, weight: String, UOM: String = "")

    @Query("UPDATE VegaCoffeeSalesLots SET delivery = :delivery,deliveryItem = :deliveryItem, weighBridgeId = :weighBridgeId, deliveryFlag =:deliveryFlag, pgiFlag = :pgiFlag, pickingFlag = :pickingFlag, storageLossFlag = :storageLossFlag  where batchNumber = :batchNumber")
    abstract fun updateLotStatus(
        batchNumber: String,
        delivery: String,
        deliveryItem: String,
        weighBridgeId: String,
        deliveryFlag: Boolean,
        pgiFlag: Boolean,
        pickingFlag: Boolean,
        storageLossFlag: Boolean
    )

    @Query("DELETE FROM VegaCoffeeSalesOrder WHERE salesTempId = :salesTempId")
    abstract fun deleteSalesOrder(salesTempId: String)

    @Query("DELETE FROM VegaCoffeeSalesLots WHERE salesTempId = :salesTempId")
    abstract fun deleteSalesLots(salesTempId: String)

    @Query("DELETE FROM VegaCoffeeSalesBagMaterial WHERE salesTempId = :salesTempId")
    abstract fun deleteSalesLotsBagItems(salesTempId: String)

    @Query("SELECT * FROM VegaCoffeeSalesLots where batchNumber =:batchNumber")
    abstract fun getOfflineSalesPalletBags(batchNumber: String): LiveData<VegaCameroonSalesWithBagItems>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllProducts(): LiveData<List<VegaMaterial>>
}
