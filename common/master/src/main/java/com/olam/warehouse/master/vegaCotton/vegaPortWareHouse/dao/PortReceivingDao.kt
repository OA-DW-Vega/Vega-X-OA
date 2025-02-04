package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.*

@Dao
interface PortReceivingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMtns(mtns: List<PortMtn>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMtn(mtn: PortMtn)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMtnBales(mtnBales: List<PortMtnBales>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMtnBale(mtnBale: PortMtnBales)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMtnGrade(mtnGrade: PortMtnGrades)

    @Query("SELECT * FROM PortMtn WHERE isOfflineData=0")
    fun getMtns(): List<MtnWithGrades>

    @Query("SELECT * FROM PortMtn WHERE isOfflineData=0")
    fun getMtnss(): LiveData<List<MtnWithGrades>>

    @Query("SELECT * FROM PortMtn")
    fun getMtnsAsList(): List<PortMtn>

    @Query("SELECT * FROM PortMtn")
    fun getMtnsAsListOffline(): List<PortMtn>

    @Query("DELETE FROM PortMtn")
    fun deleteAllMtns()

    @Query("SELECT * FROM PortMtnBales WHERE mtnNumber=:mtnNumber AND baleId=:baleId AND isVerified=0")
    fun getBale(mtnNumber: String, baleId: String): PortMtnBales?

    @Query("SELECT * FROM PortMtnBales WHERE mtnNumber=:mtnNumber AND isVerified = 1 ORDER BY dateTime DESC")
    fun getVerifiedBales(mtnNumber: String): LiveData<List<PortMtnBales>>

    @Update
    fun updateMtnBale(mtnBale: PortMtnBales)

    @Update
    fun updateMtn(mtn: PortMtn)

    @Query("SELECT * FROM PortMtnBales WHERE baleId=:baleId")
    fun getBaleByBaleId(baleId: String): LiveData<PortMtnBales>

    @Query("SELECT * FROM PortMtn WHERE mtnNumber=:mtnNumber")
    fun getMtnWithBales(mtnNumber: String): MtnWithBales

    @Query("SELECT * FROM PortMtn WHERE isOfflineData = 1")
    fun getOfflineMtnWithBales(): List<MtnWithBales>

    @Query("SELECT * FROM PortMtnBales WHERE isOfflineData = 1 and mtnNumber = :mtnNumber")
    fun getOfflineBales(mtnNumber: String): List<PortMtnBales>

    @Query("SELECT * FROM PortMtn WHERE isOfflineData=0")
    fun getAllMtnWithBales(): List<MtnWithGrades>

    @Query("UPDATE PortMtn SET isOfflineData =1 WHERE mtnNumber=:mtnNumber")
    fun updateMtnStatus(mtnNumber: String)

    @Query("UPDATE PortMtnBales SET isOfflineData =1, isVerified = 1, toSloc ='1001', baleStatus = 'Good' WHERE mtnNumber=:mtnNumber")
    fun updateMtnBalesStatus(mtnNumber: String)

    @Query("UPDATE PortMtnBales SET isOfflineData =1 WHERE baleId=:baleId")
    fun updateVerifiedMtnBalesStatus(baleId: String)

    @Query("UPDATE PortMtn SET isOfflineData =0 WHERE mtnNumber=:mtnNumber")
    fun updateMtnRevertStatus(mtnNumber: String)

    @Query("UPDATE PortMtnBales SET isOfflineData =0, isVerified = 0, toSloc ='', baleStatus = '' WHERE mtnNumber=:mtnNumber")
    fun updateMtnBalesRevertStatus(mtnNumber: String)

    @Query("DELETE FROM PortMtnBales")
    fun deleteAllMtnBales()

    @Query("DELETE FROM PortMtn WHERE mtnNumber=:mtnNumber")
    fun deleteMtnsByMtnId(mtnNumber: String)

    @Query("DELETE FROM PortMtnBales WHERE mtnNumber=:mtnNumber")
    fun deleteMtnBalesByMtnId(mtnNumber: String)

    @Query("SELECT * FROM PortMtnGrades WHERE mtnNumber=:mtnNumber")
    fun getGrades(mtnNumber: String): List<PortMtnGrades>

}
