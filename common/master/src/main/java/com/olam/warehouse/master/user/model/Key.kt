package com.olam.warehouse.master.user.model

import androidx.room.Embedded
import androidx.room.Ignore
import com.olam.warehouse.master.user.entity.UserRole

/**
 * Created by Baskaran Kannan on 12/24/2019.
 */
data class Key(

    var backOffice: String? = "",
    var companyCode: String? = "",
    var product: String? = "",
    var receptionType: String? = "",
    @Embedded
    @Ignore
    var receptionTypes: List<String> = emptyList(),
    @Embedded
    @Ignore
    var companyCodes: List<String> = emptyList(),
    @Embedded
    @Ignore
    var products: List<String> = emptyList(),
    @Embedded
    @Ignore
    var backOffices: List<String> = emptyList(),
    @Embedded
    @Ignore
    var keys: List<String> = emptyList(),
    @Embedded
    @Ignore
    var userRoles: List<UserRole> = emptyList()
)
