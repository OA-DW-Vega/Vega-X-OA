package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial

@Dao
abstract class VegaCoffeeInventoryDao {

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
}
