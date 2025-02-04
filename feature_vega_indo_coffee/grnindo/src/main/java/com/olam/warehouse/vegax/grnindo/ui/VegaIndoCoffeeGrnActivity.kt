package com.olam.warehouse.vegax.grnindo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_GRN
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnindo.R
import com.olam.warehouse.vegax.grnindo.di.injectIndoCoffeeGrnFeature
import com.olam.warehouse.vegax.grnindo.ui.offline.VegaIndoCoffeeGrnOfflineSummary
import com.olam.warehouse.vegax.grnindo.ui.offline.VegaIndoCoffeeGrnTransactionDetailsFragment
import com.olam.warehouse.vegax.grnindo.utils.GRN_FRAG
import com.olam.warehouse.vegax.grnindo.utils.GRN_OFFLINE_FRAG
import com.olam.warehouse.vegax.grnindo.utils.QUALITY_DETAILS

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeGrnActivity : HomeBaseActivity(), VegaIndoCoffeeGrnWBListFragment.CallBack,
    VegaIndoCoffeeGrnOfflineSummary.CallBack, VegaIndoCoffeeGrnDetailsFragment.CallBack,
    VegaIndoCoffeeGrnTransactionDetailsFragment.CallBack {

    private val mTAG = VegaIndoCoffeeGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_indo_coffee_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndoCoffeeGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(TRANS_GRN))
            displayFragment(VegaIndoCoffeeGrnTransactionDetailsFragment(), false)
        else
            displayFragment(VegaIndoCoffeeGrnWBListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId) {
        when (moveFrag) {
            GRN_FRAG -> displayFragment(VegaIndoCoffeeGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaIndoCoffeeGrnOfflineSummary.newInstance(), true)
            QUALITY_DETAILS -> displayFragment(VegaIndoCoffeeGRNLotQualityFragment.newInstance(item), true)
        }
    }

    override fun replaceFragment(moveFrag: String, item: Any) {
        when (moveFrag) {
            GRN_FRAG -> displayFragment(
                VegaIndoCoffeeGrnDetailsFragment.newInstance(item as VegaGrnWeighBridgeId),
                true
            )
        }
    }
}

