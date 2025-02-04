package com.olam.warehouse.master.common.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * Created by Baskaran Kannan on 12/19/2019.
 */
abstract class BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(users: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(user: T)
}
