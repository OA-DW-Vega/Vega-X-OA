package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 10/11/2021.
 */

@Parcelize
@Entity(
    primaryKeys = ["moduleName", "subModuleName", "featureName"],
    indices = [Index(value = ["moduleName", "subModuleName", "featureName"], unique = true)]
)
data class VegaFeatureMaster(
    var featureName: String = "",
    var moduleName: String = "",
    var subModuleName: String = "",
    var screenName: String? = "",
    var dataType: String? = "",
    var featureDesc: String? = "",
    var fieldType: String? = "",
    var fieldValue: String? = "",
    var featureValue: String? = "",
    // @SerializedName("isMandatory")
    var mandatory: Boolean? = false,
    //@SerializedName("isVisible")
    var visible: Boolean? = true,
    var maxLength: String? = "",
    var minLength: String? = "",
    var moduleDesc: String? = "",
    var screenDesc: String? = "",
    var subModuleDesc: String? = ""
) : Parcelable
