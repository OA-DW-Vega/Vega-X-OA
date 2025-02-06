package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class BaleGrade(
    var grade:String,
    var netWeight:BigDecimal,
    var count: Long? = 0,
    var isChecked:Boolean = false
):Parcelable
