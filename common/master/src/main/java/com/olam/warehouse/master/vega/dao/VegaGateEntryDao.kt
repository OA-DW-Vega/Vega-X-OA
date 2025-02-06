package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
@Dao
abstract class VegaGateEntryDao {

    @Query("SELECT * FROM VegaGateEntry WHERE isSynced = 0")
    abstract suspend fun getWaitingTruckListDetails(): List<VegaGateEntry>

    @Query("SELECT * FROM VegaGateEntry WHERE weighBridgeId = :wbid")
    abstract fun isWBExist(wbid: String): List<VegaGateEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertQualityWbDetail(wbList: VegaGateEntry)

    fun save(item: List<VegaGateEntry>) {
        //deleteVegaWaitingTrucks()
        item.forEach {
            if (isWBExist(it.weighBridgeId).isNotEmpty()) return@forEach
            it.tmpWbId = it.weighBridgeId
            it.supplierCode = it.supplierCode
            insertQualityWbDetail(it)
        }
    }

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Transaction
    @Query("DELETE FROM VegaGateEntry WHERE isSynced = 0")
    abstract fun deleteVegaWaitingTrucks()

    @Query("DELETE FROM VegaGateEntry  where weighBridgeId = :wbid")
    abstract fun updateDB(wbid: String)

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaVendor where werks =:selectedPlantId")
    abstract fun getSuppliersPlant(selectedPlantId: String): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliersPlantNew(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaBcZoneMapping where sapuserId =:bcApprover")
    abstract fun getSupplierZone(bcApprover: String): LiveData<List<VegaBcZoneMapping>>

    @Query("SELECT * FROM VegaCustomStLocation where procureLocationCode = :code")
    abstract fun getStorageLocation(code: String): LiveData<VegaCustomStLocation>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VehicleDetails")
    abstract fun getSeasonDetailsOffline(): LiveData<List<VehicleDetails>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

/*    @Query("SELECT * FROM VegaCameroonMultiPlantDetails")
    abstract fun getMultiPlantList(): LiveData<List<Plant>>*/

    @Query("SELECT * FROM VegaGateEntry WHERE commonPrimaryId =:commonId and isSynced =0")
    abstract fun getGateEntryDetails(commonId: String): LiveData<VegaGateEntry>

    @Query("SELECT * FROM VegaTrackTraceFarmerData")
    abstract fun getFarmerList(): LiveData<List<VegaTrackTraceFarmerData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveTTFarmerData(items: List<TrackTraceFarmerModel>)

    @Query("SELECT * FROM TrackTraceFarmerModel where tmpWbId = :tmpWbId")
    abstract fun getTTFarmerData(tmpWbId: String): List<TrackTraceFarmerModel>
}
