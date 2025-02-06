package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.olam.warehouse.master.common.model.CustomStLocation
import com.olam.warehouse.master.dorigin.entity.DOPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaStorageLocation

@Dao
abstract class VegaStockReconDao {

    @Query("SELECT * FROM VegaStorageLocation where plant =:plantId")
    abstract fun getStorageLocations(plantId: String): LiveData<List<VegaStorageLocation>>

    @Query("SELECT * FROM VegaDispatchLots WHERE batchNumber = :batchNumber")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaDispatchLots

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT materialName FROM VegaMaterial where materialCode =:materialCode")
    abstract fun getMaterialName(materialCode: String): String

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getVegaMaterials(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
}
