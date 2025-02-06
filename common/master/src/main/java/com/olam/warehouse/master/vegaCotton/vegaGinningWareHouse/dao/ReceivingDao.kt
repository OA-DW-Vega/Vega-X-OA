package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.*

@Dao
interface ReceivingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMtns(mtns: List<Mtn>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMtn(mtn: Mtn)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
      fun insertOrReplaceMtnBales(mtnBales: List<MtnBales>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertOrReplaceMtnBale(mtnBale: MtnBales)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertOrReplaceMtnGrade(mtnGrade: MtnGrades)

    @Query("SELECT * FROM Mtn WHERE isOfflineData=0")
     fun getMtns(): LiveData<List<MtnWithGrades>>

    @Query("SELECT * FROM Mtn")
      fun getMtnsAsList(): LiveData<List<Mtn>>

    @Query("DELETE FROM Mtn")
     fun deleteAllMtns()

    @Query("SELECT * FROM MtnBales WHERE mtnNumber=:mtnNumber AND baleId=:baleId AND isVerified=0")
     fun getBale(mtnNumber: String, baleId: String):LiveData<MtnBales>

    @Query("SELECT * FROM MtnBales WHERE mtnNumber=:mtnNumber AND isVerified = 1 ORDER BY dateTime DESC")
     fun getVerifiedBales(mtnNumber: String): LiveData<List<MtnBales>>

    @Update
     fun updateMtnBale(mtnBale: MtnBales)

    @Update
      fun updateMtn(mtn: Mtn)

    @Query("SELECT * FROM MtnBales WHERE baleId=:baleId")
      fun getBaleByBaleId(baleId: String): LiveData<MtnBales>

    @Query("SELECT * FROM Mtn WHERE mtnNumber=:mtnNumber")
    fun getMtnWithBales(mtnNumber: String): LiveData<MtnWithBales>

    @Query("SELECT * FROM Mtn WHERE mtnNumber=:mtnNumber")
    fun getMtnWithBalesOffline(mtnNumber: String): MtnWithBales


    @Query("SELECT * FROM Mtn WHERE isOfflineData = 1")
      fun getOfflineMtnWithBales():LiveData<List<MtnWithBales>>

    @Query("SELECT * FROM MtnBales WHERE isOfflineData = 1 and mtnNumber = :mtnNumber")
      fun getOfflineBales(mtnNumber: String): List<MtnBales>

    @Query("SELECT * FROM Mtn WHERE isOfflineData=0")
      fun getAllMtnWithBales(): List<MtnWithGrades>

    @Query("UPDATE Mtn SET isOfflineData =1 WHERE mtnNumber=:mtnNumber")
      fun updateMtnStatus(mtnNumber: String)

    @Query("UPDATE MtnBales SET isOfflineData =1, isVerified = 1, toSloc ='1001', baleStatus = 'Good' WHERE mtnNumber=:mtnNumber")
      fun updateMtnBalesStatus(mtnNumber: String)

    @Query("UPDATE MtnBales SET isVerified = 1, toSloc ='1001', baleStatus = 'Good' WHERE mtnNumber=:mtnNumber")
      fun updateMtnBalesStatusVerified(mtnNumber: String)

    @Query("UPDATE MtnBales SET isOfflineData =1 WHERE baleId=:baleId")
      fun updateVerifiedMtnBalesStatus(baleId: String)

    @Query("UPDATE Mtn SET isOfflineData =0 WHERE mtnNumber=:mtnNumber")
      fun updateMtnRevertStatus(mtnNumber: String)

    @Query("UPDATE MtnBales SET isOfflineData =0, isVerified = 0, toSloc ='', baleStatus = '' WHERE mtnNumber=:mtnNumber")
      fun updateMtnBalesRevertStatus(mtnNumber: String)

    @Query("DELETE FROM MtnBales")
      fun deleteAllMtnBales()

    @Query("DELETE FROM Mtn WHERE mtnNumber=:mtnNumber")
      fun deleteMtnsByMtnId(mtnNumber: String)

    @Query("DELETE FROM MtnBales WHERE mtnNumber=:mtnNumber")
      fun deleteMtnBalesByMtnId(mtnNumber: String)

    @Query("SELECT * FROM MtnGrades WHERE mtnNumber=:mtnNumber")
      fun getGrades(mtnNumber: String):LiveData<List<MtnGrades>>

}
