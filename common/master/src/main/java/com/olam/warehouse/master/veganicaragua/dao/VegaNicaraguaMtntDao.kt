package com.olam.warehouse.master.veganicaragua.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags

/**
 * Created by Baskaran Kannan on 11/12/2020.
 */
@Dao
abstract class VegaNicaraguaMtntDao {

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllProducts(): LiveData<List<VegaMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTruckInfo(mtnt: VegaNicaraguaMtnt)

    @Query("SELECT * FROM VegaNicaraguaMtnt where tempId = :tmpId")
    abstract fun getMtntWithLots(tmpId: String): LiveData<VegaMtntWithLotsWithBags>

    @Query("SELECT * FROM VegaNicaraguaMtnt where tempId = :tmpId")
    abstract fun getMtntWithLotsSync(tmpId: String): VegaMtntWithLotsWithBags

    @Query("SELECT * FROM VegaNicDispatchLots WHERE batchNumber = :batchNumber")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaNicDispatchLots

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveLotDetails(lot: VegaNicDispatchLots)

    @Query("DELETE FROM VegaNicDispatchLots WHERE batchNumber = :batchNumber and tempId = :tempId")
    abstract fun removeLotFromList(batchNumber: String, tempId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial)

    @Query("SELECT * FROM VegaNicaraguaWeighmentBagMaterial WHERE batchNumber = :batchNumber and tmpWbId = :tempId")
    abstract fun getBagItems(batchNumber: String, tempId: String): LiveData<List<VegaNicaraguaWeighmentBagMaterial>>

    @Query("DELETE FROM VegaNicaraguaWeighmentBagMaterial WHERE id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveLotList(lotList: List<VegaNicDispatchLots>)

    @Query("SELECT * FROM VegaNicaraguaGRNInventoryDetails WHERE materialCode = :material")
    abstract fun getStockListOffline(material: String): LiveData<List<VegaNicaraguaGRNInventoryDetails>>

    @Query("SELECT * FROM VegaQualitative WHERE materialCode = :materialId and nameChar = :nameChar")
    abstract fun getQualityGrades(materialId: String, nameChar: String): LiveData<List<VegaQualitative>>

    @Query("SELECT * FROM VegaQualitative WHERE materialCode = :materialId and nameChar = :nameChar")
    abstract fun getCertification(materialId: String, nameChar: String): LiveData<List<VegaQualitative>>

    @Query("SELECT * FROM VegaNicaraguaMaterialQualitGrades WHERE materialCode = :materialId ")
    abstract fun getMaterialQualityGrades(materialId: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>

    @Query("SELECT * FROM VegaNicaraguaMtnt")
    abstract fun getListOfMtntWithLots(): LiveData<List<VegaMtntWithLotsWithBags>>

    @Query("DELETE FROM VegaNicaraguaMtnt WHERE tempId = :tmpWbId")
    abstract fun deleteMtntItems(tmpWbId: String)

    @Query("DELETE FROM VegaNicaraguaWeighmentBagMaterial WHERE tmpWbId = :tmpWbId")
    abstract fun deleteMtntBagItems(tmpWbId: String)

    @Query("DELETE FROM VegaNicDispatchLots WHERE tempId = :tmpWbId")
    abstract fun deleteMtntLots(tmpWbId: String)

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): List<VegaMaterial>

    @Query("SELECT * FROM VegaCocoaPurchaseOrders")
    abstract fun getPurchaseOrderOffline(): List<VegaCocoaPurchaseOrders>

    @Query("SELECT * FROM VegaNicaraguaGRNInventoryDetails where lotId = :lotId and materialCode = :materialCode")
    abstract fun getOfflineLotDetails(lotId: String, materialCode: String): List<VegaNicaraguaGRNInventoryDetails>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Transaction
    @androidx.room.Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>
}
