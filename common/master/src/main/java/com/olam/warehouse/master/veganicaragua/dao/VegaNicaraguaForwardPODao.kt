package com.olam.warehouse.master.veganicaragua.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganicaragua.entity.*

@Dao
abstract class VegaNicaraguaForwardPODao {

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaVendor where purchaseOrgType =:purchaseOrgType")
    abstract fun getSuppliers(purchaseOrgType: String): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaMaterial where materialType ='W'")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaNicaraguaGrnPriceDetails ")
    abstract fun getGrnPriceDetails(): LiveData<List<VegaNicaraguaGrnPriceDetails>>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Query("SELECT * FROM VegaNicaraguaExchangeRate where `key` = :key")
    abstract fun getExchangeRateOffline(key: String): LiveData<VegaNicaraguaExchangeRate>

    @Query("SELECT * FROM VegaQualitative WHERE materialCode = :materialId and nameChar = :nameChar")
    abstract fun getQualityGrades(materialId: String, nameChar: String): LiveData<List<VegaQualitative>>

    @Query("SELECT * FROM VegaNicaraguaMaterialQualitGrades WHERE materialCode = :materialId ")
    abstract fun getMaterialQualityGrades(materialId: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>

    @Query("SELECT * FROM VegaNicaraguaGrnCharDetails WHERE materialNumber = :materialCode and grade = :grade")
    abstract fun getGrnCharDetails(grade: String, materialCode: String): LiveData<List<VegaNicaraguaGrnCharDetails>>

    @Query("SELECT * FROM VegaNicaraguaGrnCharDetails WHERE materialNumber = :materialCode and grade = :grade")
    abstract fun getGrnCharDetailsCount(grade: String, materialCode: String): List<VegaNicaraguaGrnCharDetails>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveForwardPOData(receivingData: VegaNicaraguaForwardPODetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveForwardPOPriceDetails(receivingData: List<VegaNicaraguaForwardPOPriceDetails>)

    @Query("SELECT * FROM VegaNicaraguaForwardPODetails")
    abstract fun getForwardPODetails(): LiveData<List<VegaNicaraguaForwardPODetails>>

    @Query("SELECT * FROM VegaNicaraguaForwardPOPriceDetails  WHERE tempId = :tempId")
    abstract fun getForwardPOPriceDetails(tempId: String): LiveData<List<VegaNicaraguaForwardPOPriceDetails>>

    @Query("SELECT * FROM VegaNicaraguaForwardPODetails  WHERE tempId = :tempId")
    abstract fun getForwardPODetails(tempId: String): VegaNicaraguaForwardPODetails

    @Query("SELECT * FROM VegaNicaraguaForwardPOPriceDetails  WHERE tempId = :tempId")
    abstract fun getForwardPOPriceDetailsByTempId(tempId: String): List<VegaNicaraguaForwardPOPriceDetails>

    @Query("DELETE FROM VegaNicaraguaForwardPODetails where tempId =:tmpWbId")
    abstract fun deleteForwardPoDetails(tmpWbId: String)

    @Query("DELETE FROM VegaNicaraguaForwardPOPriceDetails where tempId =:tmpWbId")
    abstract fun deleteForwardPoPriceDetails(tmpWbId: String)

    @Query("UPDATE VegaNicaraguaForwardPODetails SET syncStatusMsg =:msg WHERE tempId = :tmpWbId")
    abstract fun updateErrorForwardData(tmpWbId: String, msg: String)

    @Query("SELECT * FROM VegaNicaraguaForwardPODetails WHERE syncStatus=0")
    abstract fun getOfflineForwardPODetails(): LiveData<List<VegaNicaraguaForwardPODetails>>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
}
