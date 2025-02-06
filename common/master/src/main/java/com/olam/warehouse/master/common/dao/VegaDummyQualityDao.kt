package com.olam.warehouse.master.common.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative

@Dao
abstract class VegaDummyQualityDao {


    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSupplierList(): LiveData<List<VegaVendor>>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>
}
