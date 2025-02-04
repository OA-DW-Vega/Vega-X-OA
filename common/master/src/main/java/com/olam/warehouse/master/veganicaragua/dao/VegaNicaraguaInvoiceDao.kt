package com.olam.warehouse.master.veganicaragua.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnWithInventoryDetails

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
@Dao
abstract class VegaNicaraguaInvoiceDao {
    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaVendor where purchaseOrgType =:purchaseOrgType")
    abstract fun getSuppliers(purchaseOrgType: String): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaMaterial where materialType ='W'")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaNicaraguaPriceConfigDetails where materialCode = :materialCode and qualityCode =:qualityCode")
    abstract fun getPriceConfigInfo(
        materialCode: String,
        qualityCode: String
    ): LiveData<VegaNicaraguaPriceConfigDetails>


    @Query("SELECT * FROM VegaNicaraguaExchangeRate where `key` = :key")
    abstract fun getExchangeRateOffline(key: String): LiveData<VegaNicaraguaExchangeRate>

    @Query("SELECT * FROM GrnDetails where supplierCode = :vendorCode")
    abstract fun getGrnDetailsOffline(vendorCode: String): LiveData<List<VegaNicaraguaGrnWithInventoryDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveInvoiceDetails(invoice: VegaNicaraguaInvoiceDetails)

    @Query("SELECT * FROM VegaNicaraguaInvoiceDetails where isReceiptData = 0")
    abstract fun getInvoiceOfflineData(): LiveData<List<VegaNicaraguaInvoiceDetails>>

    @Query("SELECT * FROM VegaNicaraguaInvoiceDetails where erdat BETWEEN :startTime AND :endTime and isReceiptData=0")
    abstract fun getInvoiceByDate(startTime: String, endTime: String): LiveData<List<VegaNicaraguaInvoiceDetails>>

    @Query("SELECT * FROM VegaNicaraguaAdvanceTransactionDetails where erdat BETWEEN :startTime AND :endTime")
    abstract fun getAdvanceByDate(startTime: String, endTime: String): LiveData<List<VegaNicaraguaAdvanceTransactionDetails>>

    @Query("DELETE  FROM VegaNicaraguaInvoiceDetails WHERE tempId = :tmpWbId")
    abstract fun deleteInvoiceItem(tmpWbId: String)

    @Query("SELECT * FROM VegaNicaraguaAdvanceLineItems where `vendor` = :vendor and deletedFlag = 0 ORDER BY documentDate DESC")
    abstract fun getAdvanceLineItems(vendor: String): LiveData<List<VegaNicaraguaAdvanceLineItems>>

    @Query("SELECT * FROM VegaNicaraguaAdvanceLineItemGrn WHERE tmpWbId = :tmpId")
    abstract fun getAdvanceItem(tmpId: String): LiveData<List<VegaNicaraguaAdvanceLineItemGrn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveAdvanceLineItem(advanceLineItem: List<VegaNicaraguaAdvanceLineItemGrn>)

    @Query("SELECT * FROM VegaNicaraguaGRNInventoryDetails where materialCode =:materialCode and lotId =:lotId ")
    abstract fun getInventoryDetails(
        lotId: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGRNInventoryDetails>>

    @Query("SELECT * FROM VegaReceiving")
    abstract fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>>

    @Query("SELECT * FROM VegaReceiving where erdat BETWEEN :startTime AND  :endTime")
    abstract fun getReceivingWithLineItemByDate(startTime: String, endTime: String): LiveData<List<VegaReceiving>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertReport(item: VegaNicaraguaReconcilCashMovement)

    @Query("SELECT * FROM VegaNicaraguaReconcilCashMovement WHERE edate = :currentDate")
    abstract fun getReconReport(currentDate: String): LiveData<VegaNicaraguaReconcilCashMovement>

    @Query("SELECT * FROM QualitativeParams WHERE paramName = :paramName")
    abstract fun getQualityDesc(paramName: String): LiveData<QualitativeParams>

    @Query("SELECT * FROM QualitativeParams" )
    abstract fun getQualityGradeDescList(): LiveData<List<QualitativeParams>>

    @Query("SELECT * FROM VegaQualitative WHERE materialCode = :materialId and nameChar = :nameChar")
    abstract fun getQualityGrades(materialId: String, nameChar: String): LiveData<List<VegaQualitative>>

    @Query("SELECT * FROM VegaNicaraguaMaterialQualitGrades WHERE materialCode = :materialId ")
    abstract fun getMaterialQualityGrades(materialId: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>

    @Query("SELECT * FROM VegaNicaraguaGrnCharDetails WHERE materialNumber = :materialCode and grade = :grade")
    abstract fun getGrnCharDetails(grade: String, materialCode: String): LiveData<List<VegaNicaraguaGrnCharDetails>>

    @Query("SELECT * FROM VegaNicaraguaGrnCharDetails WHERE materialNumber = :materialCode and grade = :grade")
    abstract fun getGrnCharDetailsCount(grade: String, materialCode: String): List<VegaNicaraguaGrnCharDetails>

    @Query("SELECT * FROM VegaNicaraguaGrnPriceDetails ")
    abstract fun getGrnPriceDetails(): LiveData<List<VegaNicaraguaGrnPriceDetails>>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Query("SELECT * FROM VegaNicaraguaInvoiceDetails WHERE tempId = :tmpWbId")
    abstract fun getInvoiceData(tmpWbId:String):VegaNicaraguaInvoiceDetails

    @Query("SELECT * FROM VegaNicaraguaExchangeRate where `key` = :key")
    abstract fun getExchangeRateOfflineWorker(key: String): VegaNicaraguaExchangeRate

    @Query("SELECT * FROM VegaNicaraguaPriceConfigDetails where materialCode = :materialCode and qualityCode =:qualityCode")
    abstract fun getPriceConfigInfoWorker(
        materialCode: String,
        qualityCode: String
    ): VegaNicaraguaPriceConfigDetails

    @Query("SELECT * FROM VegaNicaraguaAdvanceLineItemGrn WHERE tmpWbId = :tmpId")
    abstract fun getAdvanceItemById(tmpId: String): List<VegaNicaraguaAdvanceLineItemGrn>

    @Query("UPDATE VegaNicaraguaInvoiceDetails SET syncStatusMsg =:msg WHERE tempId = :tmpWbId")
    abstract fun updateErrorInvoiceData(tmpWbId: String, msg: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveInvoiceData(receivingData: VegaNicaraguaInvoiceDetails)

    @Query("SELECT * FROM VegaNicaraguaInvoiceDetails WHERE isReceiptData = 1")
    abstract fun getInvoiceReceiptOffline(): LiveData<List<VegaNicaraguaInvoiceDetails>>

    @Query("DELETE FROM VegaNicaraguaAdvanceLineItemGrn where documentNumber = :documentNumber and tmpWbId =:tmpWbId")
    abstract fun removeAdvanceLineItem(documentNumber: String?, tmpWbId: String)
}
