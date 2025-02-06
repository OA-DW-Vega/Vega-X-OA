package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaMtntWithLineItems

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
@Dao
abstract class VegaMtntDao {

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMtnt(item: VegaMtnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>)

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT * FROM VegaMtnt")
    abstract fun getMtntWithLineItem(): LiveData<List<VegaMtntWithLineItems>>

    @Transaction
    @Query("SELECT * FROM VegaMtnt where tmpWbId = :wbid")
    abstract fun getMtntWithLineItemSingle(wbid: String): VegaMtntWithLineItems

    @Transaction
    @Query("DELETE FROM VegaMtnt where tmpWbId = :wbid")
    abstract fun deleteMtnt(wbid: String)

    @Transaction
    @Query("DELETE FROM VegaMtntLineItem where tmpWbId = :wbid")
    abstract fun deleteMtntLineItem(wbid: String)

    fun deleteItemMtnt(wbid: String) {
        deleteMtnt(wbid)
        deleteMtntLineItem(wbid)
    }


}
