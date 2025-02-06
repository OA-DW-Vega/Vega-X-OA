package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.TrackContainer

@Dao
interface TrackContainerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addContainer(container: TrackContainer)

    @Query("SELECT * FROM TrackContainer WHERE containerNumber=:containerNumber")
    fun isContainerExist(containerNumber:String):Boolean

    @Query("SELECT * FROM TrackContainer")
    fun getContainers():LiveData<List<TrackContainer>>

    @Query("SELECT * FROM TrackContainer")
    fun getContainersAsList():List<TrackContainer>

    @Query("DELETE FROM TrackContainer")
    fun deleteTrackContainers()
}
