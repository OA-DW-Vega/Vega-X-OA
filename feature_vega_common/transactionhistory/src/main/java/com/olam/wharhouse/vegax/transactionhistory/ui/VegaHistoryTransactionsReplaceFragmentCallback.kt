package com.olam.wharhouse.vegax.transactionhistory.ui

import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtnrHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTransactions


interface VegaHistoryTransactionsReplaceFragmentCallback {

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaMtntHistoryTransactions
    )


    fun replaceFragment(
        paramsListFrag: String,
        item: VegaFGRNHistoryTransactions
    )

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaRMINHistoryTransactions
    )


    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGRNHistoryTransactions
    )

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaMtnrHistoryTransactions
    )




}
