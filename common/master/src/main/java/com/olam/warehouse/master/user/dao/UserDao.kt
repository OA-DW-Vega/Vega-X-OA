package com.olam.warehouse.master.user.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.dao.BaseDao
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.master.user.entity.UserRole

@Dao
abstract class UserDao : BaseDao<User>() {
    suspend fun save(item: User) {
        insert(item)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUserRole(userRole: UserRole)

    @Update
    abstract suspend fun updateUser(user: User)

    @Delete
    abstract suspend fun deleteUser(user: User)

    @Query("Select *from User")
    abstract suspend fun getUser(): User

    @Transaction
    @Query("SELECT * FROM UserRole WHERE roleKey = :currentKey")
    abstract fun getUserRoles(currentKey: String): LiveData<List<UserRole>>

    @Query("SELECT * FROM UserRole WHERE roleKey = :currentKey")
    abstract fun getUserRole(currentKey: String): LiveData<List<UserRole>>
}
