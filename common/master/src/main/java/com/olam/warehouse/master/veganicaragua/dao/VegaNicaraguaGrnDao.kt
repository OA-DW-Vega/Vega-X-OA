package com.olam.warehouse.master.veganicaragua.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaOfflineRmin
import com.olam.warehouse.presentation.enums.Status

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
@Dao
abstract class VegaNicaraguaGrnDao {

    @Query("SELECT * FROM VegaVendor where purchaseOrgType =:purchaseOrgType")
    abstract fun getSuppliers(purchaseOrgType: String): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSupplierList(): List<VegaVendor>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getMaterials(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaMaterial where materialType ='W'")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaMaterial where materialType ='W'")
    abstract fun getProductsLocal(): List<VegaMaterial>

    @Query("DELETE FROM VegaCocoaDispatchLots WHERE batchNumber = :batchNumber")
    abstract suspend fun removeLots(batchNumber: String)

    @Query("SELECT * FROM VegaStorageLocation where plant =:plantId")
    abstract fun getStorageLocations(plantId: String): LiveData<List<VegaStorageLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial)

    @Query("DELETE FROM VegaNicaraguaWeighmentBagMaterial where id = :id and tmpWbId =:tmpWbId")
    abstract fun deleteBagDetails(id: Int, tmpWbId: String)

    @Query("SELECT * FROM VegaNicaraguaWeighmentBagMaterial where tmpWbId =:tmpWbId")
    abstract fun getBagItems(tmpWbId: String): LiveData<List<VegaNicaraguaWeighmentBagMaterial>>

    @Query("SELECT * FROM VegaNicaraguaWeighmentBagMaterial where tmpWbId =:tmpWbId")
    abstract fun getBagItemsWorker(/*
        materialCode: String?,
        supplierCode: String,*/
        tmpWbId: String
    ): List<VegaNicaraguaWeighmentBagMaterial>

    @Query("SELECT * FROM VegaNicaraguaPriceConfigDetails where materialCode = :materialCode and qualityCode =:qualityCode")
    abstract fun getPriceConfigInfo(
        materialCode: String,
        qualityCode: String
    ): LiveData<VegaNicaraguaPriceConfigDetails>

    @Query("SELECT * FROM VegaNicaraguaPriceConfigDetails where materialCode = :materialCode and qualityCode =:qualityCode")
    abstract fun getPriceConfigInfoWorker(
        materialCode: String,
        qualityCode: String
    ): VegaNicaraguaPriceConfigDetails

    @Query("SELECT * FROM VegaNicaraguaExchangeRate where `key` = :key")
    abstract fun getExchangeRateOffline(key: String): LiveData<VegaNicaraguaExchangeRate>

    @Query("SELECT * FROM VegaNicaraguaExchangeRate where `key` = :key")
    abstract fun getExchangeRateOfflineWorker(key: String): VegaNicaraguaExchangeRate

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameterOffline(materialId: String): List<VegaQualityParamsWithQualitative>

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(materialId: String, wbId: String?): List<VegaQualityWithQualitative>

    @Query("SELECT * FROM VegaNicaraguaGrnPriceDetails ")
    abstract fun getGrnPriceDetails(): LiveData<List<VegaNicaraguaGrnPriceDetails>>

    @Query("SELECT * FROM VegaNicaraguaGrnPriceDetails")
    abstract fun getGrnPriceDetailsWorker(): List<VegaNicaraguaGrnPriceDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveGrnCharDetails(item: List<VegaNicaraguaGrnCharDetails>)

    @Transaction
    @Query("DELETE FROM VegaNicaraguaGrnCharDetails")
    abstract fun deleteGrnCharDetails()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveAdvanceLineDetails(item: List<VegaNicaraguaAdvanceLineItems>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveGrnInventoryDetails(item: List<VegaNicaraguaGRNInventoryDetails>)

    @Transaction
    @Query("DELETE FROM VegaNicaraguaGRNInventoryDetails")
    abstract fun deleteGrnInventoryDetails()

    @Query("SELECT * FROM VegaQualitative WHERE materialCode = :materialId and nameChar = :nameChar")
    abstract fun getQualityGrades(
        materialId: String,
        nameChar: String
    ): LiveData<List<VegaQualitative>>

    @Query("SELECT * FROM VegaNicaraguaGrnCharDetails WHERE materialNumber = :materialCode and grade = :grade")
    abstract fun getGrnCharDetails(
        grade: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGrnCharDetails>>

    @Query("SELECT * FROM VegaNicaraguaGrnCharDetails WHERE materialNumber = :materialCode and grade = :grade")
    abstract fun getGrnCharDetailsCount(
        grade: String,
        materialCode: String
    ): List<VegaNicaraguaGrnCharDetails>

    @Query("SELECT * FROM VegaNicaraguaPositionGradeMappings WHERE code = :grade")
    abstract fun getGradeMapping(grade: String): LiveData<VegaNicaraguaPositionGradeMappings>

    @Query("SELECT * FROM VegaNicaraguaPositionGradeMappings WHERE code = :grade")
    abstract fun getGradeMappingWorker(grade: String): VegaNicaraguaPositionGradeMappings

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveQualityData(prepareQualityData: VegaQuality)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveGrnData(receivingData: VegaReceiving)

    @Query("SELECT * FROM VegaReceiving")
    abstract fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>>

    @Query("SELECT * FROM VegaReceiving where tmpWbId = :tmpWbId")
    abstract fun getReceivingData(tmpWbId: String): VegaReceiving

    @Query("SELECT * FROM VegaQuality where wbTempId = :tmpWbId")
    abstract fun getQuality(tmpWbId: String): LiveData<List<VegaQuality>>

    @Query("SELECT * FROM VegaQuality where wbTempId = :tmpWbId")
    abstract fun getQualityParams(tmpWbId: String): List<VegaQuality>

    @Query("UPDATE VegaReceiving SET weighBridgeId = :wbId,syncStatusMsg =:message, status = :status, wbFlag = :wbFlag, qcFlag = :qcFlag, grnFlag =:grnFlag, isSynced = :isSynced WHERE tmpWbId = :tmpWbId")
    abstract fun updateGrnSuccessData(
        tmpWbId: String,
        wbId: String,
        status: Status,
        wbFlag: Boolean,
        qcFlag: Boolean,
        grnFlag: Boolean,
        isSynced: Boolean,
        message: String
    )

    @Query("UPDATE VegaReceiving SET syncStatusMsg =:msg WHERE tmpWbId = :tmpWbId")
    abstract fun updateErrorGrnData(tmpWbId: String, msg: String)

    @Query("DELETE FROM VegaNicaraguaWeighmentBagMaterial where tmpWbId =:tmpWbId")
    abstract fun deleteAllBagDetails(tmpWbId: String)

    @Query("DELETE FROM VegaReceiving where tmpWbId =:tmpWbId")
    abstract fun deleteGrnDetail(tmpWbId: String)

    @Query("DELETE FROM VegaQuality where wbTempId =:tmpWbId")
    abstract fun deleteQualityDetail(tmpWbId: String)

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getGlDetails(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getGlDetailsWork(role: String): List<VegaCocoaMiscellaneous>

    @Query("SELECT * FROM VegaNicaraguaInvoiceDetails WHERE isSynced = 0 and isReceiptData = 0")
    abstract fun getInvoiceOfflineData(): LiveData<List<VegaNicaraguaInvoiceDetails>>

    @Query("SELECT * FROM VegaNicaraguaAdvanceLineItems where `vendor` = :vendor and deletedFlag = 0 ORDER BY documentDate DESC")
    abstract fun getAdvanceLineItems(vendor: String): LiveData<List<VegaNicaraguaAdvanceLineItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveAdvanceLineItem(advanceLineItem: List<VegaNicaraguaAdvanceLineItemGrn>)

    @Query("SELECT * FROM VegaNicaraguaAdvanceLineItemGrn WHERE tmpWbId = :tmpId")
    abstract fun getAdvanceItem(tmpId: String): LiveData<List<VegaNicaraguaAdvanceLineItemGrn>>

    @Query("SELECT * FROM VegaNicaraguaMaterialQualitGrades WHERE materialCode = :materialId ")
    abstract fun getMaterialQualityGrades(materialId: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>

    @Query("SELECT * FROM VegaNicaraguaMaterialQualitGrades")
    abstract fun getAllMaterialQualityGrades(): LiveData<List<VegaNicaraguaMaterialQualitGrades>>

    @Query("SELECT * FROM VegaNicaraguaMaterialQualitGrades")
    abstract fun getAllMaterialQualityGradesOffline(): List<VegaNicaraguaMaterialQualitGrades>

    @Query("SELECT COUNT(*) FROM VegaReceiving where status = :syncPending")
    abstract fun getOfflinePendingCount(syncPending: Status): Int

    @Query("UPDATE VegaReceiving SET syncStarted = 1 WHERE tmpWbId = :wbid")
    abstract fun updateSyncStartedStatus(wbid: String)

    @Query("SELECT * FROM VegaEcuadorPurchaseOrder")
    abstract fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>>

    @Query("SELECT * FROM VegaNicaraguaMtnt")
    abstract fun getListOfMtntWithLots(): LiveData<List<VegaMtntWithLotsWithBags>>

    @Query("SELECT * FROM VegaNicaraguaAdvanceLineItemGrn WHERE tmpWbId = :tmpId")
    abstract fun getAdvanceItemById(tmpId: String): List<VegaNicaraguaAdvanceLineItemGrn>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveInvoiceReceipt(prepareInvoiceReceiptData: List<VegaNicaraguaInvoiceDetails>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveGrnReceipt(prepareGrnReceiptData: List<VegaReceiving>)

    @Query("SELECT * FROM VegaReceiving WHERE status = :syncReprint")
    abstract fun getGrnPrintDetailsOffline(syncReprint: Status): LiveData<List<VegaReceiving>>

    @Query("DELETE FROM VegaNicaraguaAdvanceLineItemGrn where documentNumber = :documentNumber and tmpWbId =:tmpWbId")
    abstract fun removeAdvanceLineItem(documentNumber: String?, tmpWbId: String)

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT COUNT(*) FROM VegaNicaraguaAdvanceLineItems")
    abstract fun getAdvanceItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminDetails(rminItem: VegaNicOfflineRminData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminLotDetails(rminLotItem: VegaNicOfflineRminProcessLotDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminSelectedLots(rminItem: VegaNicOfflineRminLots)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminItem(rminItem: VegaNicOfflineRminItems)

    @Query("SELECT * FROM VegaNicOfflineRminData where rminTempId = :RminTempId")
    abstract fun getofflineRminData(
        RminTempId: String
    ): LiveData<List<VegaNicOfflineRminData>>

    @Query("SELECT * FROM VegaNicOfflineRminLots where poNo = :poNo")
    abstract fun getOfflineRminLots(poNo: String): LiveData<List<VegaNicOfflineRminLots>>

    @Query("SELECT * FROM VegaNicOfflineRminItems")
    abstract fun getOfflineRminItems(): LiveData<List<VegaNicOfflineRminItems>>

    @Query("DELETE FROM VegaNicOfflineRminLots where lotId = :batchNumber")
    abstract fun deleteOfflineRminLot(batchNumber: String)

    @Query("DELETE FROM VegaNicOfflineRminProcessLotDetails where rminTempId = :tmpWbId")
    abstract fun deleteOfflineRminLotDetails(tmpWbId: String)

    @Query("DELETE FROM VegaNicOfflineRminData where rminTempId = :tmpWbId")
    abstract fun deleteOfflineRminData(tmpWbId: String)

    @Query("SELECT * FROM VegaNicOfflineRminProcessLotDetails where rminTempId = :rmin")
    abstract fun getofflineRminPostItem(rmin: String): LiveData<List<VegaNicOfflineRminProcessLotDetails>>

    @Query("SELECT * FROM VegaNicOfflineRminProcessLotDetails")
    abstract fun getofflineRminItem(): LiveData<List<VegaNicaraguaOfflineRmin>>


}
