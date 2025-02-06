package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale


/**
 * Created by SangiliPandian C on 20-03-2020.
 */
@Dao
interface GinningInprogressDao  {
    @Transaction
    @Delete
    fun remove(bale: Bale)
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(bale: Bale)

    @Query("SELECT * FROM Bale WHERE lotNumber = :lotId")
    fun getBalesByLotNumber(lotId: String): LiveData<List<Bale>>

    @Query("DELETE FROM Bale WHERE lotNumber = :lotId")
    fun deleteBalesByLotId(lotId: String)
}
