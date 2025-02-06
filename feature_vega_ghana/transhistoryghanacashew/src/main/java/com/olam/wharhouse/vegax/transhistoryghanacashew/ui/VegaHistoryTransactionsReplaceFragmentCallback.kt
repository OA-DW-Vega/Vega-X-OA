package com.olam.wharhouse.vegax.transhistoryghanacashew.ui

import com.olam.warehouse.master.vegaghana.entity.*

interface VegaHistoryTransactionsReplaceFragmentCallback {

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewMtntHistoryTransactions
    )


    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewFGRNHistoryTransactions
    )

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewRMINHistoryTransactions
    )


    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewGRNHistoryTransactions
    )

    fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewMtnrHistoryTransactions
    )




}
