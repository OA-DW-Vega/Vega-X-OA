package com.olam.warehouse.master.vegacameroon.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine

@Dao
abstract class VegaCameroonContainerDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertShippingLineList(shippingLineList: List<VegaCameroonShippingLine>)
}
