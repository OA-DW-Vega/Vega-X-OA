package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.*
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeExportOTWithContainer
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.vegaindocoffee.model.IndoContainerWithLots
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeExportOTWithContainer
import java.util.*

@Dao
abstract class VegaCoffeeExportSalesDao {

    @Query("SELECT * FROM VegaCoffeeExportSalesOrder WHERE saleOrderId = :otNumber")
    abstract fun getOTWithContainer(otNumber: String): LiveData<VegaCoffeeExportOTWithContainer>

    @Query("SELECT * FROM VegaIndoCoffeeExportSalesOrder WHERE tmpId = :tmpId")
    abstract fun getOTWithContainerIndo(tmpId: String): LiveData<VegaIndoCoffeeExportOTWithContainer>

    @Query("SELECT * FROM VegaIndoCoffeeExportSalesOrder WHERE tmpId = :tmpId")
    abstract fun getOTWithContainerIndoWork(tmpId: String): VegaIndoCoffeeExportOTWithContainer

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveContainer(containerId: VegaCoffeeExportSalesContainer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOT(ot: VegaCoffeeExportSalesOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveIndoOT(ot: VegaIndoCoffeeExportSalesOrder)

    @Query("SELECT * FROM VegaCoffeeExportSalesLots WHERE batchNumber = :batchNumber")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaCoffeeExportSalesLots

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0 and baseMaterial = :material")
    abstract fun getBagItems(batchNumber: String, material: String): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial where id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>)

    @Query("SELECT * FROM VegaCoffeeExportSalesContainer WHERE containerNumber = :containerNumber")
    abstract fun getContainerWithLots(containerNumber: String): LiveData<ContainerWithLots>

    @Query("SELECT * FROM VegaCoffeeExportSalesContainer WHERE containerNumber = :containerNumber")
    abstract fun getContainerWithLotsIndo(containerNumber: String): LiveData<IndoContainerWithLots>

    @Query("DELETE FROM VegaCoffeeExportSalesLots where batchNumber = :batchNumber and materialCode =:materialCode")
    abstract fun removeLotDetails(batchNumber: String, materialCode: String?)

    @Query("SELECT * FROM VegaCoffeeExportSalesContainer WHERE containerNumber = :containerNumber")
    abstract fun validateContainer(containerNumber: String): VegaCoffeeExportSalesContainer

    @Query("SELECT * FROM VegaCoffeeExportSalesLots")
    abstract fun getAddedLotList(): LiveData<List<VegaCoffeeExportSalesLots>>

    @Query("UPDATE VegaCoffeeExportSalesLots SET delivery = :delivery,deliveryItem = :deliveryItem, weighBridgeId = :weighBridgeId, deliveryFlag =:deliveryFlag, pickingFlag = :pickingFlag, containerFlag = :containerFlag  where batchNumber = :batchNumber and tmpId =:tmpId")
    abstract fun updateLotStatus(
        batchNumber: String,
        delivery: String,
        deliveryItem: String,
        weighBridgeId: String,
        deliveryFlag: Boolean,
        pickingFlag: Boolean,
        containerFlag: Boolean,
        tmpId: String
    )

    @Query("DELETE FROM VegaCoffeeExportSalesOrder WHERE saleOrderId = :saleOrderId")
    abstract fun deleteSalesOrder(saleOrderId: String)

    @Query("DELETE FROM VegaCoffeeExportSalesContainer WHERE saleOrderId = :saleOrderId")
    abstract fun deleteSalesConatainer(saleOrderId: String)

    @Query("DELETE FROM VegaCoffeeExportSalesLots WHERE saleOrderId = :saleOrderId")
    abstract fun deleteSalesContainerLots(saleOrderId: String)

    @Query("DELETE FROM VegaCoffeeExportSalesLots WHERE containerNumber = :containerNumber")
    abstract fun deleteLots(containerNumber: String)

    @Query("DELETE FROM VegaCoffeeExportSalesContainer WHERE containerNumber = :containerNumber")
    abstract fun deleteContainer(containerNumber: String)

    @Query("UPDATE VegaCoffeeExportSalesContainer SET containerNumber = :newContainerId where containerNumber = :oldContainerId")
    abstract fun updateContainer(oldContainerId: String, newContainerId: String)

    @Query("UPDATE VegaCoffeeExportSalesLots SET containerNumber = :newContainerId where containerNumber = :oldContainerId")
    abstract fun updateLot(oldContainerId: String, newContainerId: String)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial WHERE batchNumber = :batchNumber")
    abstract fun deleteContainerLotBagDetails(batchNumber: String)

    @Query("SELECT * FROM IndoExporSalesMaterialList")
    abstract fun getOfflinePurchaseOrder(): LiveData<List<IndoExporSalesMaterialList>>

    @Query("SELECT * FROM VegaEcuadorDispatchStocks")
    abstract fun getStocks(): LiveData<List<VegaEcuadorDispatchStocks>>

    @Query("UPDATE VegaIndoCoffeeExportSalesOrder SET isTransStatus = 1, isSynced = :isSync, syncStatusMsg = :synStatusMsg, status =:status, deliveryFlag=:deliveryFlag, pickingFlag=:pickingFlag, containerFlag=:containerFlag  where tmpId =:tmpId")
    abstract fun updateSalesOrderStatus(
        tmpId: String?,
        synStatusMsg: String?,
        status: Int,
        deliveryFlag: Boolean,
        pickingFlag: Boolean,
        containerFlag: Boolean,
        isSync: Boolean
    )

    @Query("UPDATE VegaIndoCoffeeExportSalesOrder SET isOffline = 1, isTransStatus = 1  where tmpId =:tmpId")
    abstract fun updateSalesOrderOfflineStatus(tmpId: String?)

    @Query("SELECT * FROM VegaIndoCoffeeExportSalesOrder where isTransStatus =1")
    abstract fun getIndoExportSalesItem(): LiveData<List<VegaIndoCoffeeExportSalesOrder>>

    @Query("DELETE FROM VegaIndoCoffeeExportSalesOrder WHERE tmpId = :tmpId")
    abstract fun deleteIndoSalesOrder(tmpId: String)

    @Query("DELETE FROM VegaCoffeeExportSalesContainer WHERE tmpId = :tmpId")
    abstract fun deleteIndoSalesConatainer(tmpId: String)

    @Query("DELETE FROM VegaCoffeeExportSalesLots WHERE tmpId = :tmpId")
    abstract fun deleteIndoSalesContainerLots(tmpId: String)

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Query("SELECT * FROM VegaCoffeeExportSalesLots where batchNumber =:batchNumber")
    abstract fun getOfflineExportSalesPalletBags(batchNumber: String): LiveData<VegaCameroonExportSalesWithBagItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveExportDetails(vegaCoffeeSalesLots: VegaCoffeeExportSalesLots)

    @Query("UPDATE VegaCoffeeExportSalesLots SET palletWeight = :palletWeight,palletAvg = :palletAvg, palletCount = :palletCount where batchNumber = :batchNumber")
    abstract fun saveExportDetails(
        palletWeight: String,
        palletAvg: String,
        palletCount: String,
        batchNumber: String
    )
    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllProducts(): LiveData<List<VegaMaterial>>

}
