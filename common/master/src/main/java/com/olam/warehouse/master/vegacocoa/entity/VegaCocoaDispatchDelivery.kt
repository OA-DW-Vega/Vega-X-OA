package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 2/21/2020.
 */
@Entity(primaryKeys = ["deliveryNo", "deliveryItem"])
@Parcelize
data class VegaCocoaDispatchDelivery(
    var deliveryNo: String = "",
    var deliveryItem: String = "",
    var deliveryUOM: String? = "",            //Vrkme
    var stockQty: String? = "",               //Lgmng
    var deliveryQty: String? = "",            //Lfimg
    var denominator: String? = "",            //Umvkz
    var numerator: String? = "",              //Umvkn
    var stockUOM: String? = "",               //Meins
    var storageLocationCode: String? = ""    //Lgort
) : Parcelable
