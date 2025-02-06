package com.olam.warehouse.vegax.historytransactionsghanacocoa.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.historytransactionsghanacocoa.R
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtnrHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtntHistoryTransactions
import com.olam.warehouse.vegax.historytransactionsghanacocoa.di.injectGhanaCocoaHistoryTransactionFeature
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.grn.VegaHistoryGhanaCocoaGrnConsignmentFragment
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.grn.VegaHistoryGhanaCocoaGrnListFragment
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.mtnr.VegaHistoryGhanaCocoaMtnrConsignmentFragment
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.mtnr.VegaHistoryGhanaCocoaMtnrListFragment
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.mtnt.VegaHistoryGhanaCocoaMtntConsignmentFragment
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.mtnt.VegaHistoryGhanaCocoaMtntListFragment
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaHistoryTransactionsGhanaCocoaActivity : HomeBaseActivity(),
    VegaHistoryTransactionsReplaceFragmentCallback{

    private val mTAG = VegaHistoryTransactionsGhanaCocoaActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_history_transactions_ghana_cocoa
    private val vm: VegaHistoryTransactionGhanaCocoaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaCocoaHistoryTransactionFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaHistoryTransactionsGhanaCocoaTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flHistoryTransactions,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCocoaMtnrHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_MTNR -> {
                displayFragment(
                    VegaHistoryGhanaCocoaMtnrConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_MTNR -> {
                displayFragment(
                    VegaHistoryGhanaCocoaMtnrListFragment.newInstance(
                        item
                    ) , true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCocoaGRNHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_GRN -> {
                displayFragment(
                    VegaHistoryGhanaCocoaGrnConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_GRN -> {
                displayFragment(
                    VegaHistoryGhanaCocoaGrnListFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCocoaMtntHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_MTNT -> {
                displayFragment(
                    VegaHistoryGhanaCocoaMtntConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_MTNT -> {
                displayFragment(
                    VegaHistoryGhanaCocoaMtntListFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }

    override fun onBackPressed() {
        backNavigation()
    }

    private fun backNavigation() {
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flHistoryTransactions)) {
            is VegaHistoryTransactionsGhanaCocoaTypeFragment -> {
                /*No Implementation*/
            }
            is VegaHistoryGhanaCocoaMtntListFragment -> {
                /*No Implementation*/
            }
            is VegaHistoryGhanaCocoaGrnListFragment -> {
                /*No Implementation*/
            }
            is VegaHistoryGhanaCocoaMtnrListFragment -> {
                /*No Implementation*/
            }
            is VegaHistoryGhanaCocoaMtntConsignmentFragment -> {
                /*No Implementation*/
            }
            is VegaHistoryGhanaCocoaGrnConsignmentFragment -> {
                /*No Implementation*/
            }
            is VegaHistoryGhanaCocoaMtnrConsignmentFragment -> {
                /*No Implementation*/
            }
            else -> super.onBackPressed()
        }
    }

}
