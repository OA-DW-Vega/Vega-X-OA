package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale

@Dao
interface GinningPileDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(users: List<PileBale>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveListItem(ot: List<PileBale>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveItem(ot: PileBale)
    @Delete
    fun remove(user: PileBale)

    @Query("Select * from PileBale where storageLocationTo = :location")
    fun getBaleByStorageLocationId(location: String): List<PileBale>


    @Query("SELECT COUNT(*) FROM PileBale WHERE baleID = :bale")
    fun isBaleAlreadyExist(bale: String): Int


    @Query("SELECT * FROM PileBale")
    fun getExistedBaleList(): LiveData<List<PileBale>>


    @Query("Delete from PileBale where storageLocationTo = :pileId")
    fun deleteBalesDB(pileId: String)
}
