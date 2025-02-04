package com.olam.warehouse.master.vegacocoa.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial

@Dao
abstract class VegaCocoaSweepingDao {

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial WHERE id = :id and batchNumber = :batchNumber")
    abstract fun deleteBagDetails(id: Int, batchNumber: String)

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0")
    abstract fun getBagItems(batchNumber: String): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("UPDATE VegaCocoaSweepingBagMaterial set message = :msg, status = :status, isSyncStatus = :syncStatus where batchNumber = :batchNumber")
    abstract fun updateDataToDB(msg: String, status: Int, batchNumber: String, syncStatus: Boolean)
}
