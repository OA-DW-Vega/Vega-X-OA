package com.olam.wharhouse.vegax.transhistoryghanacashew.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.grn.VegaHistoryGhanaCashewFgrnConsignmentFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.grn.VegaHistoryGhanaCashewFgrnListFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.grn.VegaHistoryGhanaCashewGrnConsignmentFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.grn.VegaHistoryGhanaCashewGrnListFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.mtnt.VegaHistoryGhanaCashewMtntConsignmentFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.mtnt.VegaHistoryGhanaCashewMtntListFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.mtnt.VegaHistoryGhanaCashewRminConsignmentFragment
import com.olam.warehouse.vegax.transhistoryghanacashew.ui.rmin.VegaHistoryGhanaCashewRminListFragment
import com.olam.wharhouse.vegax.transhistoryghanacashew.R
import com.olam.wharhouse.vegax.transhistoryghanacashew.di.injectGhanaCashewTransHisFeature
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.grn.VegaGhanaCashewGRNQualityFragment
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.mtnr.VegaHistoryGhanaCashewMtnrConsignmentFragment
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.mtnr.VegaHistoryGhanaCashewMtnrListFragment
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
class VegaGhanaCashewTransHistoryActivity : HomeBaseActivity(),VegaHistoryTransactionsReplaceFragmentCallback {

    private val mTAG = VegaGhanaCashewTransHistoryActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_ghana_cashew_trans_hisatory
    private val vm: VegaGhanaCashewTransHisViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaCashewTransHisFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaHistoryTransactionsGhanaCashewTypeFragment.newInstance(), false)
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
        item: VegaGhanaCashewMtnrHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_MTNR -> {
                displayFragment(
                    VegaHistoryGhanaCashewMtnrConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_MTNR -> {
                displayFragment(
                    VegaHistoryGhanaCashewMtnrListFragment.newInstance(
                        item
                    ) , true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewGRNHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_GRN -> {
                displayFragment(
                    VegaHistoryGhanaCashewGrnConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_GRN -> {
                displayFragment(
                    VegaHistoryGhanaCashewGrnListFragment.newInstance(
                        item
                    ), true
                )
            }
            HISTORY_TRANSACTIONS_QUALITY_PARAM-> {
                displayFragment(
                    VegaGhanaCashewGRNQualityFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewFGRNHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_FGRN -> {
                displayFragment(
                    VegaHistoryGhanaCashewFgrnConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_FGRN -> {
                displayFragment(
                    VegaHistoryGhanaCashewFgrnListFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewRMINHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_RMIN -> {
                displayFragment(
                    VegaHistoryGhanaCashewRminConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_RMIN -> {
                displayFragment(
                    VegaHistoryGhanaCashewRminListFragment.newInstance(
                        item
                    ), true
                )
            }
        }
    }



    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaGhanaCashewMtntHistoryTransactions
    ) {
        when(paramsListFrag){
            HISTORY_TRANSACTIONS_LOT_MTNT -> {
                displayFragment(
                    VegaHistoryGhanaCashewMtntConsignmentFragment.newInstance(
                        item
                    ) , true
                )
            }
            HISTORY_TRANSACTIONS_MTNT -> {
                displayFragment(
                    VegaHistoryGhanaCashewMtntListFragment.newInstance(
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
