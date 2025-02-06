package com.olam.warehouse.vegax.historytransactionsghanacocoa.ui

import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtnrHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtntHistoryTransactions

interface VegaHistoryTransactionsReplaceFragmentCallback {

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCocoaMtntHistoryTransactions
    )

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCocoaMtnrHistoryTransactions
    )

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCocoaGRNHistoryTransactions
    )

}
