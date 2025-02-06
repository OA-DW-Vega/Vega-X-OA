package com.olam.warehouse.vegax.qualityindo.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeQualitySupplierParamPost(
    val grnApplicable: Boolean,
    val grnFlag: Boolean,
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaCoffeeLot>? = emptyList(),
    val bcMessage: String,
    val charg: String,
    val currentWbid: String,
    val errorMessage: String,
    val grnNumber: String,
    val driverName: String,
    val imageString: String,
    val imageUploadMsg: String,
    val contactNumber: String,
    val transportVendorCode: String,
    val vehicleNumber: String
)
