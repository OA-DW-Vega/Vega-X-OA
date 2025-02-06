package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.DeliveryWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDeliveryWithGrades

/**
 * Created by Baskaran Kannan on 3/31/2020.
 */
@Dao
abstract class MtnDispatchDao {
    /*Dispatch OT*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceDeliverys(ot: List<MtnDispatchDelivery>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceDelivery(ot: MtnDispatchDelivery)

    @Query("Select *from MtnDispatchDelivery where isReadyForDispatch = 0")
    abstract fun getDeliverys(): LiveData<List<MtnDispatchDelivery>>

    @Query("Select *from MtnDispatchDelivery where isReadyForDispatch = 0")
    abstract fun getDeliverysInOffine(): List<MtnDispatchDelivery>

    @Query("Select *from MtnDispatchDelivery where isReadyForDispatch = 1")
    abstract fun fetchOfflineDeliveryDetails(): List<MtnDispatchDelivery>

    /*@Query("UPDATE MtnDispatchDelivery SET isReadyForDispatch= 0 WHERE deliveryNumber=:deliveryNo")
    abstract fun updateDeliveryStatusToEdit(deliveryNo: String)*/

    @Query("UPDATE MtnDispatchDelivery SET isReadyForDispatch= 1 WHERE deliveryNumber=:deliveryNo")
    abstract fun updateDeliverySaveOffline(deliveryNo: String)

    @Query("SELECT * FROM MtnDispatchDelivery WHERE deliveryNumber = :deliveryNo and isReadyForDispatch = 1")
    abstract fun isDeliveryExist(deliveryNo: String): List<MtnDispatchDelivery>

    //Grades
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceGrades(grade: List<MtnGrade>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceGrade(grade: MtnGrade)

    //Relation
    @Transaction
    @Query("SELECT * FROM MtnDispatchDelivery where deliveryNumber = :deliveryNo")
    abstract fun getDeliveryWithGrades(deliveryNo: String): MtnDeliveryWithGrades

    @Transaction
    @Query("SELECT * FROM MtnDispatchDelivery where deliveryNumber = :deliveryNo")
    abstract fun getDeliveryWithBales(deliveryNo: String): DeliveryWithBales

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBaleDetails(bale: MtnBale)

    @Query("UPDATE MtnBale SET deliveryNumber = '', isUsed = 0 WHERE deliveryNumber=:deliveryNo")
    abstract fun deleteBales(deliveryNo: String)

    @Query("DELETE FROM MtnDispatchDelivery WHERE deliveryNumber=:deliveryNo")
    abstract fun deleteMtnDispatchDelivery(deliveryNo: String)
    /*@Query("Delete from MtnBale where deliveryNumber = :deliveryNumber")
    abstract fun deleteBalesFromDB(deliveryNumber: String)

    @Query("Delete from MtnDispatchDelivery where deliveryNumber = :deliveryNumber")
    abstract fun deleteDelivery(deliveryNumber: String)

    @Query("UPDATE MtnDispatchDelivery SET message = :msg, isErrorStatus = 0 WHERE deliveryNumber = :deliveryNumber")
    abstract fun updateErrorMessage(msg: String, deliveryNumber: String)

    // MtnBale Details

    @Query("SELECT * FROM MtnBale WHERE baleID = :baleId and isUsed = 1")
    abstract fun isBaleExist(baleId: String): List<MtnBale>*/

    @Query("SELECT * FROM MtnBale WHERE baleID = :baleId")
    abstract fun getBaleDetails(baleId: String): MtnBale

}
