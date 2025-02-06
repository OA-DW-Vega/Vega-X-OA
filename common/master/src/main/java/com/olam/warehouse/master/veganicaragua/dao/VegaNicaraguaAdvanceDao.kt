package com.olam.warehouse.master.veganicaragua.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaExchangeRate

@Dao
abstract class VegaNicaraguaAdvanceDao
{
    @Query("SELECT * FROM VegaVendor where purchaseOrgType =:purchaseOrgType")
    abstract fun getSuppliers(purchaseOrgType: String): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaNicaraguaExchangeRate where `key` = :key")
    abstract fun getExchangeRateOffline(key: String): LiveData<VegaNicaraguaExchangeRate>

    @Query("SELECT * FROM VegaNicaraguaAdvanceDetails where `vendor` = :vendorCode")
    abstract fun getAdvanceDetails(vendorCode: String): LiveData<VegaNicaraguaAdvanceDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savAdvanceDetails(grnDetails: List<VegaNicaraguaAdvanceDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveAdvanceDetailsData(postData: VegaNicaraguaAdvanceTransactionDetails)

    @Query("SELECT * FROM VegaNicaraguaAdvanceTransactionDetails")
    abstract fun geAdvanceDetailsData(): LiveData<List<VegaNicaraguaAdvanceTransactionDetails>>

    @Query("SELECT * FROM VegaNicaraguaAdvanceTransactionDetails  WHERE tempId = :tempId")
    abstract fun getAdvanceDetailsByTempId(tempId: String): VegaNicaraguaAdvanceTransactionDetails

    @Query("DELETE FROM VegaNicaraguaAdvanceTransactionDetails where tempId =:tmpWbId")
    abstract fun deleteAdvanceDetails(tmpWbId: String)

    @Query("DELETE FROM VegaNicaraguaAdvanceDetails where vendor =:vendor")
    abstract fun deleteAdvanceDetailsForCredit(vendor: String)

    @Query("UPDATE VegaNicaraguaAdvanceTransactionDetails SET syncStatusMsg =:msg WHERE tempId = :tmpWbId")
    abstract fun updateErrorAdvanceDetails(tmpWbId: String, msg: String)

    @Query("SELECT * FROM VegaNicaraguaAdvanceTransactionDetails WHERE syncStatus=0")
    abstract fun getOfflineAdvanceODetails(): LiveData<List<VegaNicaraguaAdvanceTransactionDetails>>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

}
