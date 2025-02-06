package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Grade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithGrades

/**
 * Created by Baskaran Kannan on 3/20/2020.
 */

@Dao
interface VegaCottonGinningDispatchDao {
    /*Dispatch OT*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceDeliverys(ot: List<VegaCottonGinningDispatchDelivery>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceDelivery(ot: VegaCottonGinningDispatchDelivery)

    @Query("Select *from VegaCottonGinningDispatchDelivery where isReadyForDispatch = 0")
    fun getDeliverys(): LiveData<List<VegaCottonGinningDispatchDelivery>>

    @Query("Select *from VegaCottonGinningDispatchDelivery where isReadyForDispatch = 0")
    fun getDeliverysInOffine():LiveData< List<VegaCottonGinningDispatchDelivery>>

    @Query("Select *from VegaCottonGinningDispatchDelivery where isReadyForDispatch = 1")
    fun fetchOfflineDeliveryDetails(): LiveData<List<VegaCottonGinningDispatchDelivery>>

    @Query("UPDATE VegaCottonGinningDispatchDelivery SET isReadyForDispatch= 0 WHERE deliveryNumber=:deliveryNo")
    fun updateDeliveryStatusToEdit(deliveryNo: String)

    @Query("UPDATE VegaCottonGinningDispatchDelivery SET isReadyForDispatch= 1 WHERE deliveryNumber=:deliveryNo")
    fun updateDeliverySaveOffline(deliveryNo: String)

    @Query("SELECT * FROM VegaCottonGinningDispatchDelivery WHERE deliveryNumber = :deliveryNo and isReadyForDispatch = 1")
    fun isDeliveryExist(deliveryNo: String): LiveData<List<VegaCottonGinningDispatchDelivery>>

    @Query("SELECT * FROM VegaCottonGinningDispatchDelivery WHERE deliveryNumber = :deliveryNo and isReadyForDispatch = 1")
    fun isDeliveryExistOffline(deliveryNo: String): List<VegaCottonGinningDispatchDelivery>


    @Query("SELECT * FROM VegaCottonGinningDispatchDelivery WHERE isReadyForDispatch = 1")
    fun fetchExistedDeliveries(): LiveData<List<VegaCottonGinningDispatchDelivery>>


    //Grades
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceGrades(grade: List<Grade>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceGrade(grade: Grade)

    //Relation
    @Transaction
    @Query("SELECT * FROM VegaCottonGinningDispatchDelivery where deliveryNumber = :deliveryNo")
    fun getDeliveryWithGrades(deliveryNo: String): LiveData<DeliveryWithGrades>

    @Transaction
    @Query("SELECT * FROM VegaCottonGinningDispatchDelivery where deliveryNumber = :deliveryNo")
    fun getDeliveryWithBales(deliveryNo: String): LiveData<DeliveryWithBales>

    @Transaction
    @Query("SELECT * FROM VegaCottonGinningDispatchDelivery ")
    fun getAllDeliveryWithBales(): LiveData<List<DeliveryWithBales>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveBaleDetails(bale: Bale)

    @Query("UPDATE Bale SET deliveryNumber = '', isUsed = 0 WHERE deliveryNumber=:deliveryNo")
    fun deleteBales(deliveryNo: String)

    @Query("Delete from Bale where deliveryNumber = :deliveryNumber")
    fun deleteBalesFromDB(deliveryNumber: String)

    @Query("Delete from VegaCottonGinningDispatchDelivery where deliveryNumber = :deliveryNumber")
    fun deleteDelivery(deliveryNumber: String)

    @Query("UPDATE VegaCottonGinningDispatchDelivery SET message = :msg, isErrorStatus = 0 WHERE deliveryNumber = :deliveryNumber")
    fun updateErrorMessage(msg: String, deliveryNumber: String)

    // Bale Details

    @Query("SELECT * FROM Bale WHERE baleID = :baleId and isUsed = 1")
    fun isBaleExist(baleId: String): List<Bale>

    @Query("SELECT * FROM Bale WHERE baleID = :baleId")
    fun getBaleDetails(baleId: String): LiveData<Bale>

    @Query("SELECT * FROM Bale WHERE baleID = :baleId")
    fun getBaleDetailsOffline(baleId: String): Bale

    @Transaction
    @Query("SELECT * FROM VegaCottonGinningDispatchDelivery where deliveryNumber = :deliveryNo")
    fun getDeliveryWithBalesOffline(deliveryNo: String): DeliveryWithBales

    @Query("SELECT * FROM Bale")
    fun fetchBaleDetails(): LiveData<List<Bale>>


}
