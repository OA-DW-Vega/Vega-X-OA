package com.olam.warehouse.master.veganicaragua.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.VegaMaterial

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */

@Dao
abstract class VegaNicInventoryDao {
    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    /* @Query("SELECT * FROM VegaPackageMaterial")
     abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

     @Query("SELECT * FROM VegaVendor")
     abstract fun getSuppliers(): LiveData<List<VegaVendor>>

     @Query("SELECT * FROM VegaMaterial")
     abstract fun getAllProducts(): LiveData<List<VegaMaterial>>

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     abstract fun insertTruckInfo(mtnt: VegaNicaraguaMtnt)*/
}
