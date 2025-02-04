package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.*

@Dao
abstract class DispatchDao {
    /*Dispatch OT*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceOTs(ot: List<DispatchOT>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceOT(ot: DispatchOT)

    @Update
    abstract fun updateOT(ot: DispatchOT)

    @Delete
    abstract fun deleteOT(ot: DispatchOT)

    @Query("Select *from DispatchOT")
    abstract fun getOTs(): LiveData<List<DispatchOT>>

    @Query("Select *from DispatchOT")
    abstract fun getOTsLocal(): List<DispatchOT>

    @Query("Select *from DispatchOT where otNumber=:otId")
    abstract fun getOTById(otId: String): LiveData<DispatchOTWithContainers>

    @Query("Select *from DispatchOT where otNumber=:otId")
    abstract fun getOTByIdLocal(otId: String): DispatchOTWithContainers

    @Query("Select *from Container where containerNumber = :containerId and otNumber = :otNumber")
    abstract fun loadContainer(containerId: String, otNumber: String): Container

    @Query("Delete from DispatchOT")
    abstract fun deleteAllOTs()

    /*Bale Details*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceBale(bale: PortBale)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceBales(bales: List<PortBale>)

    @Update
    abstract fun updateBale(bale: PortBale)

    @Delete
    abstract fun deleteBale(bale: PortBale)

    @Query("Select *from PortBale")
    abstract fun getBale(): LiveData<PortBale>

    @Query("Select *from PortBale where baleId = :baleId")
    abstract fun getBaleDeatils(baleId: String): LiveData<PortBale>

    @Query("Delete from PortBale where containerNumber = :containerId")
    abstract fun deleteBaleByContainerId(containerId: String)

    @Query("Delete from PortBale where otNumber = :otNumber")
    abstract fun deleteAllBalesByOtId(otNumber: String)

    /*Container*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceContainer(container: Container)

    @Update
    abstract fun updateContainer(container: Container)

    @Delete
    abstract fun deleteContainer(container: Container)

    @Query("Select *from Container")
    abstract fun getContainer(): LiveData<Container>

    @Query("Select *from Container where containerNumber = :containerId")
    abstract fun getContainerById(containerId: String): LiveData<Container>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplaceContainers(data: List<Container>)

    @Query("Select *from Container where containerNumber = :containerId and otNumber = :otNumber")
    abstract fun loadContainerAndBales(containerId: String, otNumber: String): ContainerWithBales

    @Query("Select *from Container where containerNumber = :containerId and otNumber = :otNumber")
    abstract fun loadContainerAndBale(containerId: String, otNumber: String): ContainerWithBales

    @Query("Delete from Container where containerNumber = :containerId")
    abstract fun deleteContainerById(containerId: String)

    @Query("UPDATE Container SET sealNumber = :sealNumber where containerNumber = :containerNumber ")
    abstract fun updateSealNumber(sealNumber: String, containerNumber: String)

    @Query("Select *from Container where otNumber = :otNumber")
    abstract fun getContainersByOtId(otNumber: String): List<Container>

    @Query("Delete from Container where otNumber = :otNumber")
    abstract fun deleteAllContainerByOtId(otNumber: String)

    @Query("Select *from DispatchOT where otNumber = :otNumber")
    abstract fun getOT(otNumber: String): LiveData<DispatchOT>

    @Query("SELECT * FROM Mtn WHERE isOfflineData=0")
    abstract fun getAllMtnWithBales(): List<MtnWithGrades>

    @Query("UPDATE Container SET otNumber = :otNumber where containerNumber = :containerNumber")
    abstract fun updateOTNumber(otNumber: String, containerNumber: String)

    @Query("UPDATE Container SET isSelected = :isSelected where containerNumber = :containerNumber")
    abstract fun updateSplitContainer(isSelected: Boolean, containerNumber: String)

}
