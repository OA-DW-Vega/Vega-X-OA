package com.olam.warehouse.master.user.entity

import androidx.room.Entity
import androidx.room.Ignore

@Entity(primaryKeys = ["roleKey", "roleName"])
data class UserRole(
    var roleKey: String = "",
    var roleName: String = "",
   // var defaultStorageTypes : String = ""
    @Ignore
    val defaultStorageTypes: List<DefaultStorage> = emptyList()
)
