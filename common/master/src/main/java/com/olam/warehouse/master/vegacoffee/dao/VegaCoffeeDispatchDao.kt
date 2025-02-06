package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vega.model.VegaCoffeeMtntWithLots
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaCameroonMtntWithBagItems
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails

@Dao
abstract class VegaCoffeeDispatchDao {

    @Query("SELECT * FROM VegaMaterial where materialCode = :code")
    abstract fun getSingleProducts(code: String): LiveData<VegaMaterial>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaPlanRoute")
    abstract fun getAllPlantRoute(): LiveData<List<VegaPlanRoute>>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDispatchTruckDetail(wbList: VegaCocoaDispatchWB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMaterialDetail(list: List<VegaCoffeePurchaseOrderMaterialModel>)

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(
        materialId: String,
        wbId: String?
    ): List<VegaQualityWithQualitative>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Transaction
    @Query("DELETE FROM VegaCocoaDispatchWB WHERE weighBridgeId = :whId")
    abstract fun deleteVegaDispatchTrucks(whId: String)

    @Transaction
    @Query("DELETE FROM VegaCoffeePurchaseOrderMaterialModel WHERE weighBridgeId = :whId")
    abstract fun deleteMaterialData(whId: String)

    @Query("SELECT * FROM VegaCoffeePurchaseOrderMaterialModel WHERE weighBridgeId = :whId")
    abstract fun getMaterialData(whId: String): List<VegaCoffeePurchaseOrderMaterialModel>

    @Query("SELECT * FROM VegaGhanaPurchaseOrderMaterialModel WHERE weighBridgeId = :whId")
    abstract fun getGhanaMaterialData(whId: String): List<VegaCoffeePurchaseOrderMaterialModel>

    @Transaction
    @Query("DELETE FROM VegaGhanaPurchaseOrderMaterialModel WHERE weighBridgeId = :whId")
    abstract fun deleteGhanaMaterialData(whId: String)

    @Transaction
    @Query("DELETE FROM VegaCoffeePurchaseOrderMaterialModel ")
    abstract fun deleteMaterialData()

    @Transaction
    @Query("DELETE FROM VegaGhanaCocoaDispatchLots")
    abstract fun deleteGhanaLotList()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSalesOrderDetail(salesOrder: VegaCoffeeSalesOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLotset(item: List<VegaCoffeeSalesLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: List<VegaCocoaDispatchLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: VegaCocoaDispatchLots)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveThirdPartyInfo(item: VegaCoffeeThirdPartyRequestModel)

    @Query("SELECT * FROM VegaCocoaPurchaseOrders")
    abstract fun getPurchaseOrdersOffline(): LiveData<List<VegaCocoaPurchaseOrders>>

    @Query("SELECT * FROM VegaCocoaDispatchLots WHERE weighBridgeId = :material and isSyncStatus = 0")
    abstract fun geLots(material: String): LiveData<List<VegaCocoaDispatchLots>>

    @Query("SELECT * FROM VegaCocoaDispatchLots WHERE batchNumber = :batchNumber and isSyncStatus = 0")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaCocoaDispatchLots

    @Query("SELECT * FROM VegaCoffeeSalesLots WHERE batchNumber = :batchNumber and isProgress = 0")
    abstract fun validateLotAlreadyAddedSales(batchNumber: String): VegaCoffeeSalesLots

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE batchNumber = :batchNumber")
    abstract suspend fun removeLots(batchNumber: String)

    @Query("DELETE FROM VegaGhanaCocoaDispatchLots WHERE batchNumber = :batchNumber and weighBridgeId = :wbId")
    abstract suspend fun removeGhanaLotFromList(batchNumber: String,wbId: String)

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE weighBridgeId = :weighBridgeId")
    abstract fun removeLotsFromLocal(weighBridgeId: String)

    @Query("DELETE FROM VegaCocoaDispatchLots ")
    abstract fun removeLotsList()

    @Query("DELETE FROM VegaGhanaCocoaDispatchLots WHERE weighBridgeId = :wbId")
    abstract suspend fun removeGhanaLotList(wbId: String)

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE weighBridgeId = :batchNumber")
    abstract suspend fun removeLotsByWeighBridgeId(batchNumber: String)

    @Query("SELECT * FROM VegaCocoaFgrnGradesMatrialWeights where fgrnIdMaterialCode =:fgrnIdWithMatrial and batchNumber = :batchNo")
    abstract fun getOfflineGradeWithBagsRmin(
        fgrnIdWithMatrial: String, batchNo: String
    ): List<VegaCocoaFgrnGradesMatrialWeights>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial where id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial")
    abstract fun deleteBagDetails()

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber")
    abstract fun deleteBag(batchNumber: String)

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0 and baseMaterial = :material")
    abstract fun getBagItems(batchNumber: String, material: String): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0 and baseMaterial = :material and weighBridgeId = :wbid")
    abstract fun getBagItems(
        batchNumber: String,
        material: String,
        wbid: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliersOffline(): List<VegaVendor>

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where isSyncStatus = 0")
    abstract fun getOfflineBagItems(): List<VegaCocoaSweepingBagMaterial>

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where weighBridgeId = :wbId and isSyncStatus = 0")
    abstract fun getGhanaOfflineBagItems(wbId: String): List<VegaCocoaSweepingBagMaterial>

    @Query("SELECT * FROM VegaCocoaDispatchWB WHERE weighBridgeId =:wbId and isSyncStatus = 0")
    abstract fun getTruckData(wbId: String): LiveData<VegaCocoaDispatchWB>

    @Query("SELECT * FROM VegaCocoaDispatchWB ")
    abstract fun getTruckData(): LiveData<List<VegaCocoaDispatchWB>>

    @Query("SELECT * FROM VegaCocoaDispatchWB where isOfflineData = 1 ")
    abstract fun getGhanaTruckData(): LiveData<List<VegaCocoaDispatchWB>>

    @Query("SELECT * FROM VegaCocoaDispatchLots WHERE  weighBridgeId = :wbId")
    abstract fun getDispatchLotsData(wbId: String): List<VegaCocoaDispatchLots>

    @Query("SELECT * FROM VegaGhanaCocoaDispatchLots WHERE  weighBridgeId = :wbId")
    abstract fun getGhanaDispatchLotsData(wbId: String): List<VegaGhanaCocoaDispatchLots>

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

    @Query("SELECT * FROM VegaCocoaDispatchWB WHERE isSyncStatus = 0")
    abstract fun getMtntWithLots(): LiveData<List<VegaCoffeeMtntWithLots>>

    @Query("SELECT * FROM VegaCocoaDispatchWB where weighBridgeId = :weighBrideId and isSyncStatus = 0")
    abstract fun getMtntWithLotSingle(weighBrideId: String): VegaCocoaMtntWithLots

    @Query("SELECT * FROM VegaCocoaDispatchWB where plantId = :whId and purchaseDocNum = :stoNo and weighBridgeType =:type and isSyncStatus = 0 ")
    abstract fun getMtntWithLotSingle(whId: String, stoNo: String, type: String): VegaCocoaMtntWithLots

    @Query("SELECT * FROM VegaCocoaDispatchWB where weighBridgeId = :weighBrideId and isSyncStatus = 0")
    abstract fun getMtntWithLotAndMaterial(weighBrideId: String): LiveData<VegaCocoaMtntWithLots>

    @Query("UPDATE VegaCocoaDispatchWB SET isSyncStatus = :syncStatus, status = :status, message = :msg where weighBridgeId = :weighBridgeId")
    abstract fun updateDispatchMtntStatus(weighBridgeId: String, syncStatus: Boolean, status: Int, msg: String)

    @Query("UPDATE VegaCocoaDispatchLots SET isSyncStatus = :synStatus, status = :status where vendorWithTransferType = :batchNumber")
    abstract fun updateSyncStatusLot(batchNumber: String, synStatus: Int, status: Int)

    @Query("SELECT * FROM VegaMaterial WHERE thirdPartyFlag = 'X'")
    abstract fun getThirdPartyMaterials(): List<VegaMaterial>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllMaterials(): List<VegaMaterial>

    @Query("SELECT * FROM VegaCoffeeThirdPartyRequestModel where vendorWithTransferType = :vendorTypeWithMaterial and status = 1")
    abstract fun getThirdPartyInfo(vendorTypeWithMaterial: String): VegaCoffeeThirdPartyModelWithLots

    @Query("UPDATE VegaCoffeeThirdPartyRequestModel SET isSyncStatus = :syncStatus, status = :status where vendorWithTransferType = :from")
    abstract fun updateThirdPartyOwnershipStatus(from: String, syncStatus: Boolean, status: Int)

    @Query("DELETE FROM VegaCoffeeThirdPartyRequestModel where vendorWithTransferType = :type")
    abstract fun deleteThirdPartyOwnershipStatus(type: String)

    @Query("DELETE FROM VegaCocoaDispatchLots where vendorWithTransferType = :type")
    abstract fun deleteLot(type: String)

    @Query("SELECT * FROM VegaSupplyStorageLocation")
    abstract fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaCustomStLocation where storageLocationType = 'P'")
    abstract fun getPCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getPileCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaCoffeeThirdPartyMaterialDetail")
    abstract fun getThirdPartyMaterialDetails(): List<VegaCoffeeThirdPartyMaterialDetail>

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getProcessList(role: String): List<VegaCocoaMiscellaneous>

    @Query("SELECT * FROM VegaEcuadorDispatchStocks")
    abstract fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>>

    @Query("UPDATE VegaEcuadorDispatchStocks SET weight = :weight where batchNumber = :batchNo")
    abstract fun updateStockDetails(weight: String, batchNo: String)

    @Query("UPDATE VegaCocoaPurchaseOrders SET openQuantity = :weight where purchaseDocNum = :po")
    abstract fun updateMtntPurchaseOrderDetails(weight: String, po: String)

    @Transaction
    @Query("DELETE FROM VegaCocoaDispatchWB WHERE status = :status")
    abstract fun deleteSyncedOfflineMtntDetails(status: Int)

    @Query("SELECT * FROM VegaUomDetails")
    abstract fun getuomDetail(): LiveData<List<VegaUomDetails>>

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT * FROM VehicleDetails")
    abstract fun getSeasonDetailsOffline(): LiveData<List<VehicleDetails>>

    @Query("SELECT * FROM VegaStorageLocationDetail ")
    abstract fun getMaterialStlocDetails(): LiveData<List<VegaStorageLocationDetail>>

    @Query("SELECT * FROM VegaStorageLocationDetail")
    abstract fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>>

    @Query("SELECT * FROM VegaCocoaDispatchLots where batchNumber =:batchNumber")
    abstract fun getOfflineMtntPalletBags(batchNumber: String): LiveData<VegaCameroonMtntWithBagItems>

    @Query("SELECT * FROM VegaFeatureMaster WHERE moduleName = :module")
    abstract fun getTTFeatureMaster(module: String): LiveData<List<VegaFeatureMaster>>


}
