package com.olam.warehouse.vegax.invoicenicaragua.ui

import com.olam.warehouse.master.veganicaragua.entity.GrnDetails

/**
 * Created by Baskaran Kannan on 1/7/2021.
 */
interface MultiSelectCallBackListener {
    fun updateSelectedGrn(selectedList: List<GrnDetails>)
}
