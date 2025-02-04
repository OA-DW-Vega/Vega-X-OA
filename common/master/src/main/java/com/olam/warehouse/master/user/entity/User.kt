package com.olam.warehouse.master.user.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.master.user.model.Key
import com.olam.warehouse.master.user.model.Plant

@Entity
data class User(
    @PrimaryKey
    var username: String = "",
    @Ignore
    var plant: Plant = Plant(),
    @Ignore
    var key: Key = Key(),
    @Ignore
    var systemPin: Boolean? = false,
    @Ignore
    var quickPin: String? = "",
    @Ignore
    var keycloakId: String? = "",
    @Ignore
    var securityEnabled: Boolean? = false,
    @Ignore
    var wareHouseLocationCode: String? = "",
    @Ignore
    var validEntity: Boolean? = true
)
