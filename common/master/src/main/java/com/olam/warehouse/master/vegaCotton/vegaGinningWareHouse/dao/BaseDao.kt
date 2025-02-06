package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * Created by SangiliPandian C on 05-03-2020.
 */

abstract class BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(users: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(user: T)

    @Delete
    protected abstract suspend fun delete(user: T)
}
