package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 4/15/2020.
 */
@Parcelize
data class BaleMark(
    var baleMark: String = "",
    var isChecked: Boolean = false
) : Parcelable
