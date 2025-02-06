package com.olam.warehouse.master.vegacocoa.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vega.model.VegaCocoaNoWeighmentWithLots
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks

@Dao
abstract class VegaCocoaDispatchDao {

    @Query("SELECT * FROM VegaMaterial where materialCode = :code")
    abstract fun getSingleProducts(code: String): LiveData<VegaMaterial>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDispatchTruckDetail(wbList: VegaCocoaDispatchWB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertNoWeighmentDetail(wbList: VegaCocoaNoWeighmentModel)

    @Transaction
    @Query("DELETE FROM VegaCocoaDispatchWB WHERE weighBridgeId = :whId")
    abstract fun deleteVegaDispatchTrucks(whId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: List<VegaCocoaDispatchLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveNoWeighmentLots(item: List<VegaCocoaNoWeighmentLot>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveNoWeighmentLotsInWork(item: List<VegaCocoaNoWeighmentLot>)

    @Query("SELECT * FROM VegaCocoaDispatchLots WHERE weighBridgeId = :material and isSyncStatus = 0")
    abstract fun geLots(material: String): LiveData<List<VegaCocoaDispatchLots>>

    @Query("SELECT * FROM VegaCocoaDispatchLots WHERE batchNumber = :batchNumber and materialCode = :purchaseOrder")
    abstract fun validateLotAlreadyAdded(batchNumber: String, purchaseOrder: String): VegaCocoaDispatchLots

    @Query("SELECT * FROM VegaVendor WHERE vendorCode = :batchNumber")
    abstract fun vendorInfo(batchNumber: String): VegaVendor

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE batchNumber = :batchNumber and materialCode = :material")
    abstract suspend fun removeLots(batchNumber: String, material: String)

    @Query("DELETE FROM VegaCocoaNoWeighmentLot WHERE batchNumber = :batchNumber and materialCode = :material")
    abstract suspend fun removeNoWeighmentLots(batchNumber: String, material: String)

    @Query("DELETE FROM VegaCocoaNoWeighmentModel WHERE weighBridgeId = :batchNumber")
    abstract suspend fun removeNoWeighment(batchNumber: String)

    @Query("DELETE FROM VegaCocoaNoWeighmentLot WHERE weighBridgeId = :wbId")
    abstract suspend fun removeNoWeighmentAllLotsByWBId(wbId: String)

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE weighBridgeId = :weighBridgeId")
    abstract fun removeLotsFromLocal(weighBridgeId: String)

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE weighBridgeId = :batchNumber")
    abstract suspend fun removeLotsByWeighBridgeId(batchNumber: String)

    @Query("SELECT * FROM VegaCocoaDispatchWB WHERE weighBridgeId =:wbId and isSyncStatus = 0")
    abstract fun getTruckData(wbId: String): LiveData<VegaCocoaDispatchWB>

    @Query("UPDATE VegaCocoaDispatchLots SET weight =:weight WHERE batchNumber =:batchNumber")
    abstract suspend fun updateLotWeight(batchNumber: String, weight: String)

    @Query("UPDATE VegaCocoaDispatchWB SET isStarted = :started,startTime=:time  WHERE weighBridgeId =:whId")
    abstract suspend fun updateStartTime(started: Boolean, time: String, whId: String)

    @Query("UPDATE VegaCocoaDispatchWB SET isEnded = :started,endTime=:time,turnAroundTime =:wholeTime  WHERE weighBridgeId =:whId")
    abstract suspend fun updateEndTime(started: Boolean, time: String, wholeTime: String, whId: String)

    @Query("UPDATE VegaCocoaDispatchWB SET remarks =:remark and isStarted =:isStart WHERE weighBridgeId =:whId")
    abstract suspend fun updateRemark(remark: String, isStart: Boolean, whId: String)

    @Query("UPDATE VegaCocoaDispatchWB SET deliveryItem =:remark and deliveryStatus =:deliveryStatus WHERE weighBridgeId =:whId")
    abstract suspend fun updateDeliveryItem(remark: String, deliveryStatus: Boolean, whId: String)

    @Query("SELECT * FROM VegaCocoaDispatchWB")
    abstract fun getMtntWithLots(): LiveData<List<VegaCocoaMtntWithLots>>

    @Query("SELECT * FROM VegaCocoaDispatchWB where weighBridgeId = :weighBrideId")
    abstract fun getMtntWithLotSingle(weighBrideId: String): VegaCocoaMtntWithLots

    @Query("UPDATE VegaCocoaDispatchWB SET isSyncStatus = :syncStatus, status = :status, message = :msg where weighBridgeId = :weighBridgeId")
    abstract fun updateDispatchMtntStatus(weighBridgeId: String, syncStatus: Boolean, status: Int, msg: String)

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("UPDATE VegaCocoaDispatchWB SET isSyncStatus = :issyncStatus, status = :status, message = :msg where weighBridgeId = :weighBridgeId")
    abstract fun updateDispatchStatus(weighBridgeId: String, issyncStatus: Boolean, status: Int, msg: String)

    @Query("UPDATE VegaCocoaDispatchWB SET delivery = :delivery , isSyncStatus = :issyncStatus, status = :status, message = :msg where weighBridgeId = :weighBridgeId")
    abstract fun updateGhanaDispatchStatus(
        delivery: String,
        weighBridgeId: String,
        issyncStatus: Boolean,
        status: Int,
        msg: String
    )

    @Query("UPDATE VegaCocoaNoWeighmentModel SET isSyncStatus = :syncStatus, status = :status, message = :msg where weighBridgeId = :weighBridgeId")
    abstract fun updateVirtualDispatchStatus(weighBridgeId: String, syncStatus: Boolean, status: Int, msg: String)

    @Query("UPDATE VegaCocoaNoWeighmentModel SET isSyncStatus = 1, status = 1 where weighBridgeId = :weighBridgeId")
    abstract fun updateVirtualDispatchStatusByWorker(weighBridgeId: String)

    @Query("UPDATE VegaCocoaDispatchLots SET isSyncStatus = :syncStatus where weighBridgeId = :weighBridgeId")
    abstract fun updateDispatchLotStatus(weighBridgeId: String, syncStatus: Boolean)

    @Query("UPDATE VegaCocoaNoWeighmentLot SET isSyncStatus = :syncStatus where weighBridgeId = :weighBridgeId")
    abstract fun updateVirtualDispatchLotStatus(weighBridgeId: String, syncStatus: Boolean)

    @Query("UPDATE VegaCocoaNoWeighmentLot SET isSyncStatus = 1 where weighBridgeId = :weighBridgeId")
    abstract fun updateVirtualDispatchLotStatusByWorker(weighBridgeId: String)

    @Query("SELECT * FROM VegaMaterial WHERE thirdPartyFlag = 'X'")
    abstract fun getThirdPartyMaterials(): List<VegaMaterial>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: ArrayList<VegaCocoaSweepingBagMaterial>)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial where id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and weighBridgeId =:mtnNumber")
    abstract fun deleteBagDetails(batchNumber: String, mtnNumber: String)

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0 and weighBridgeId = :material")
    abstract fun getBagItems(
        batchNumber: String,
        material: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePurcheseOrder(preparePurcheseOrder: ArrayList<VegaCocoaPurchaseOrders>)

    @Transaction
    @Query("DELETE FROM VegaCocoaPurchaseOrders")
    abstract fun deletePurcheseOrder()

    @Query("SELECT * FROM VegaCocoaPurchaseOrders")
    abstract fun offlinePOList(): LiveData<List<VegaCocoaPurchaseOrders>>

    @Query("SELECT * FROM VegaEcuadorDispatchStocks WHERE materialCode = :material")
    abstract fun offlineStockList(material: String): LiveData<List<VegaEcuadorDispatchStocks>>

    @Query("SELECT * FROM VegaEcuadorDispatchStocks WHERE batchNumber = :batchNumber and materialCode = :material")
    abstract fun offlineStockInfo(
        batchNumber: String,
        material: String
    ): List<VegaEcuadorDispatchStocks>

    @Query("SELECT * FROM VegaCocoaNoWeighmentModel where warehouseId = :whId and purchaseDocNum = :stono and purchaseDocDesc = :purchase and isSyncStatus = 0 and isOfflineData = :isOnline")
    abstract fun getNoWeighmentWithLotSingle(
            whId: String,
            stono: String,
            purchase: String, isOnline: Boolean
    ): VegaCocoaNoWeighmentWithLots

    @Query("SELECT * FROM VegaCocoaNoWeighmentModel where weighBridgeId = :whId and isSyncStatus = 1 ")
    abstract fun getNoWeighmentWithLot(whId: String): VegaCocoaNoWeighmentWithLots

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT * FROM VegaCocoaNoWeighmentModel where isSyncStatus = 1")
    abstract fun getTransactionData(): LiveData<List<VegaCocoaNoWeighmentWithLots>>

    @Query("SELECT * FROM VegaCocoaNoWeighmentModel where isOfflineData = 0")
    abstract fun getPendingList(): LiveData<List<VegaCocoaNoWeighmentModel>>

    @Query("SELECT * FROM VegaCocoaNoWeighmentModel where isOfflineData = 0 and isSyncStatus = 0")
    abstract fun getPendingListWithLot(): LiveData<List<VegaCocoaNoWeighmentWithLots>>


    @Query("SELECT COUNT(*) FROM VegaCocoaNoWeighmentModel where isOfflineData = 0 and isSyncStatus = 0")
    abstract fun getOfflinePendingCount(): Int

    @Query("SELECT * FROM VegaCocoaNoWeighmentModel where wbTempId = :tempId and isSyncStatus = 1")
    abstract fun getPendingList(tempId: String): VegaCocoaNoWeighmentWithLots

    @Query("SELECT * FROM VegaCocoaNoWeighmentModel where weighBridgeId = :whId and isSyncStatus = 0")
    abstract fun getNoWeighmentWithLotForWorker(
            whId: String
    ): VegaCocoaNoWeighmentWithLots

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItemsForWorker(): List<VegaCocoaSweepingBagMaterial>

    @Transaction
    @Query("DELETE FROM VegaCoffeePurchaseOrderMaterialModel WHERE weighBridgeId = :whId")
    abstract fun deleteMaterialData(whId: String)

    @Transaction
    @Query("DELETE FROM VegaCoffeePurchaseOrderMaterialModel WHERE materialCode = :mc")
    abstract fun deleteMaterialCodeData(mc: String)

    @Query("SELECT * FROM VegaCocoaDispatchWB where plantId = :whId and purchaseDocNum = :stoNo and weighBridgeType =:type and isSyncStatus = 0 ")
    abstract fun getMtntWeighsclaeWithLotSingle(whId: String, stoNo: String, type: String): VegaCocoaMtntWithLots

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMaterialDetail(list: List<VegaCoffeePurchaseOrderMaterialModel>)

    @Transaction
    @Query("DELETE FROM VegaCoffeePurchaseOrderMaterialModel")
    abstract fun deleteMaterialInfo()

    @Query("SELECT * FROM VegaCocoaDispatchLots WHERE batchNumber = :batchNumber and isSyncStatus = 0")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaCocoaDispatchLots

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE batchNumber = :batchNumber")
    abstract suspend fun removeLotsWs(batchNumber: String)

    @Query("SELECT * FROM VegaCocoaDispatchWB where weighBridgeId = :weighBrideId and isSyncStatus = 0")
    abstract fun getMtntWithLotAndMaterial(weighBrideId: String): LiveData<VegaCocoaMtntWithLots>

    @Query("UPDATE VegaCocoaDispatchLots SET isSyncStatus = :synStatus, status = :status where vendorWithTransferType = :batchNumber")
    abstract fun updateSyncStatusLot(batchNumber: String, synStatus: Int, status: Int)

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0 and baseMaterial = :material")
    abstract fun getBagItemsWeighscale(batchNumber: String, material: String): LiveData<List<VegaCocoaSweepingBagMaterial>>

    /*   @Transaction
       @Query("UPDATE VegaCocoaDispatchWB SET status = :status, isSyncStatus = :isSynced, delivery = :delivery where weighBridgeId = :wbTempId")
       abstract fun updateMtntDispatchStatus(
           wbTempId: String,
           status: Status,
           syncStatusMsg: String,
           isSynced: Boolean,
           delivery: String?
       )*/
}
