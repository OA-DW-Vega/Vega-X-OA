package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale

@Dao
abstract class PortPileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveListItem(ot: List<PortPileBale>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveItem(ot: PortPileBale)

    @Query("Select *from PortPileBale where storageLocationTo = :location")
    abstract fun getBaleByStorageLocation(location: String): LiveData<List<PortPileBale>>

    @Query("Select *from PortPileBale where storageLocationTo = :location")
    abstract fun getBaleByStorageLocationId(location: String): LiveData<List<PortPileBale>>

    @Query("Select *from PortPileBale where storageLocationTo = :location")
    abstract fun getBaleByLocationIdOffline(location: String): List<PortPileBale>

    @Query("SELECT COUNT(*) FROM PortPileBale WHERE baleID = :bale")
    abstract fun isBaleAlreadyExist(bale: String): Int

    @Query("Delete from PortPileBale where storageLocationTo = :pileId")
    abstract fun deleteBalesDB(pileId: String)

    @Query("Delete from PortPileBale where baleID = :baleId")
    abstract fun deleteBaleDB(baleId: String)

}
