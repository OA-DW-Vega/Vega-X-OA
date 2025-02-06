package com.olam.wharhouse.vegax.transactionhistory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtnrHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTransactions
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.fgrn.VegaTransHistoryFgrnConsignmentFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.fgrn.VegaTransHistoryFgrnListFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.grn.VegaTransHistoryGrnConsignmentFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.grn.VegaHistoryGrnListFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.mtnt.VegaHistoryMtntConsignmentFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.mtnt.VegaHistoryMtntListFragment
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.di.injectTransHisFeature
import com.olam.wharhouse.vegax.transactionhistory.ui.rmin.VegaHistoryRminConsignmentFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.rmin.VegaHistoryRminListFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.grn.VegaTransHistoryGRNQualityFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.mtnr.VegaHistoryMtnrListFragment
import com.olam.wharhouse.vegax.transactionhistory.ui.mtnr.VegaHistoryMtnrConsignmentFragment
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_FGRN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_GRN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_LOT_FGRN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_LOT_GRN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_LOT_MTNR
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_LOT_MTNT
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_LOT_RMIN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_MTNR
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_MTNT
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_QUALITY_PARAM
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_RMIN
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
class VegaTransHistoryActivity : HomeBaseActivity(),VegaHistoryTransactionsReplaceFragmentCallback {

    private val mTAG = VegaTransHistoryActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_transaction_history
    private val vm: VegaTransHisViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectTransHisFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaHistoryTransactionsTypeFragment.newInstance(), false)
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
        item: VegaMtnrHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_MTNR -> {
                displayFragment(
                    VegaHistoryMtnrConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_MTNR -> {
                displayFragment(
                    VegaHistoryMtnrListFragment.newInstance(
                        item
                    ) , true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGRNHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_GRN -> {
                displayFragment(
                    VegaTransHistoryGrnConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_GRN -> {
                displayFragment(
                    VegaHistoryGrnListFragment.newInstance(
                        item
                    ), true
                )
            }
            HISTORY_TRANSACTIONS_QUALITY_PARAM-> {
                displayFragment(
                    VegaTransHistoryGRNQualityFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaFGRNHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_FGRN -> {
                displayFragment(
                    VegaTransHistoryFgrnConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_FGRN -> {
                displayFragment(
                    VegaTransHistoryFgrnListFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaRMINHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_RMIN -> {
                displayFragment(
                    VegaHistoryRminConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_RMIN -> {
                displayFragment(
                    VegaHistoryRminListFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }



    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaMtntHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_MTNT -> {
                displayFragment(
                    VegaHistoryMtntConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_MTNT -> {
                displayFragment(
                    VegaHistoryMtntListFragment.newInstance(
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
        /*when (val fragment = supportFragmentManager.findFragmentById(R.id.flHistoryTransactions)) {
            is VegaHistoryTransactionsGhanaCashewTypeFragment -> {
                *//*No Implementation*//*
            }
            is VegaHistoryGhanaCashewMtntListFragment -> {
                *//*No Implementation*//*
            }
            is VegaHistoryGhanaCashewGrnListFragment -> {
                *//*No Implementation*//*
            }
            is VegaHistoryGhanaCashewMtnrListFragment -> {
                *//*No Implementation*//*
            }
            is VegaHistoryGhanaCashewFgrnListFragment -> {
                *//*No Implementation*//*
            }

            is VegaHistoryGhanaCashewMtntConsignmentFragment -> {
                *//*No Implementation*//*
            }
            is VegaHistoryGhanaCashewGrnConsignmentFragment -> {
                *//*No Implementation*//*
           }
            is VegaHistoryGhanaCashewFgrnConsignmentFragment -> {
                *//*No Implementation*//*
            }

            is VegaHistoryGhanaCashewMtnrConsignmentFragment -> {
                *//*No Implementation*//*
            }
            else -> super.onBackPressed()
        }*/
        super.onBackPressed()
    }

}
