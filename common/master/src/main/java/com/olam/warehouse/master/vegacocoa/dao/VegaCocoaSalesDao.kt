package com.olam.warehouse.master.vegacocoa.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial

@Dao
abstract class VegaCocoaSalesDao {
    @Query("SELECT * FROM VegaMaterial where materialCode = :code")
    abstract fun getSingleProducts(code: String): LiveData<VegaMaterial>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDispatchTruckDetail(wbList: VegaCocoaSalesWB)

    @Transaction
    @Query("DELETE FROM VegaCocoaSalesWB WHERE weighBridgeId = :whId")
    abstract fun deleteVegaDispatchTrucks(whId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: List<VegaCocoaSalesLots>)

    @Query("SELECT * FROM VegaMaterial WHERE thirdPartyFlag = 'X'")
    abstract fun getThirdPartyMaterials(): List<VegaMaterial>

    @Query("SELECT * FROM VegaCocoaSalesLots WHERE weighBridgeId = :material and isProgress = 0")
    abstract fun geLots(material: String): List<VegaCocoaSalesLots>

    @Query("SELECT * FROM VegaCocoaSalesLots WHERE salesOrderId =:salesOrder and salesType=:salesType and isProgress = 0")
    abstract fun geLotsBySalesOrder(
        salesOrder: String,
        salesType: String
    ): List<VegaCocoaSalesLots>

    @Query("SELECT * FROM VegaCocoaSalesLots WHERE batchNumber = :batchNumber and isProgress = 0")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaCocoaSalesLots

    @Query("DELETE FROM VegaCocoaSalesLots WHERE batchNumber = :batchNumber")
    abstract suspend fun removeLots(batchNumber: String)

    @Query("DELETE FROM VegaCocoaSalesLots WHERE weighBridgeId = :weighBridgeId")
    abstract fun removeLotsFromLocal(weighBridgeId: String)

    @Query("DELETE FROM VegaCocoaSalesLots WHERE weighBridgeId = :batchNumber")
    abstract suspend fun removeLotsByWeighBridgeId(batchNumber: String)

    @Query("SELECT * FROM VegaCocoaSalesWB WHERE weighBridgeId =:wbId and isSyncStatus =0")
    abstract fun getTruckData(wbId: String): LiveData<VegaCocoaSalesWB>

    @Query("SELECT * FROM VegaCocoaSalesWB WHERE weighBridgeId =:wbId and saleOrderId = :salesOrder and salesType = :salesType and isSyncStatus = 0")
    abstract fun getTruckDataForNoWB(wbId: String, salesOrder: String, salesType: String): LiveData<VegaCocoaSalesWB>

    @Query("UPDATE VegaCocoaSalesLots SET weight =:weight WHERE batchNumber =:batchNumber")
    abstract suspend fun updateLotWeight(batchNumber: String, weight: String)


    @Query("UPDATE VegaCocoaSalesWB SET isSyncStatus = :syncStatus, status = :status, message = :msg where weighBridgeId = :weighBridgeId")
    abstract fun updateDispatchSalesStatus(weighBridgeId: String, syncStatus: Boolean, status: Int, msg: String)

    @Query("SELECT * FROM VegaCocoaSalesWB WHERE salesType =:type and isSyncStatus = 0")
    abstract fun getPendingLotList(type: String): LiveData<List<VegaCocoaSalesWB>>

    @Query("SELECT * FROM VegaCocoaSalesWB WHERE salesType =:type and isSyncStatus = 0")
    abstract fun getPendingList(type: String): List<VegaCocoaSalesWB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial WHERE id = :id and batchNumber = :batchNumber")
    abstract fun deleteBagDetails(id: Int, batchNumber: String)

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0")
    abstract fun getBagItems(batchNumber: String): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("UPDATE VegaCocoaSalesLots SET isProgress = :syncStatus where weighBridgeId = :weighBridgeId and salesOrderId = :orderId and salesType = :type")
    abstract fun updateDispatchSalesLotStatus(weighBridgeId: String, syncStatus: Boolean, orderId: String, type: String)

    @Query("UPDATE VegaCocoaSalesLots SET editedWeight = :weight and weightToDispatchUOM = :UOM where batchNumber = :batchNumber and salesOrderId = :orderId and salesType = :type")
    abstract fun updateLotWeight(batchNumber: String, orderId: String, type: String, weight: String, UOM: String = "")

    @Query("UPDATE VegaCocoaSweepingBagMaterial SET isSyncStatus = 4 where batchNumber = :batchNumber and id =:bag")
    abstract fun updateSalesBagSyncStatus(batchNumber: String, bag: Int)

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>
    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllProducts(): LiveData<List<VegaMaterial>>
}
