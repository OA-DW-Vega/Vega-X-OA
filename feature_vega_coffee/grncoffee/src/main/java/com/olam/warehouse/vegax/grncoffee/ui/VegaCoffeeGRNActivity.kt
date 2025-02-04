package com.olam.warehouse.vegax.grncoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grncoffee.R
import com.olam.warehouse.vegax.grncoffee.di.injectCoffeeGrnFeature
import com.olam.warehouse.vegax.grncoffee.ui.offline.VegaCoffeeGrnOfflineSummary
import com.olam.warehouse.vegax.grncoffee.utils.GRN_FRAG
import com.olam.warehouse.vegax.grncoffee.utils.GRN_OFFLINE_FRAG
import com.olam.warehouse.vegax.grncoffee.utils.QUALITY_DETAILS

class VegaCoffeeGRNActivity : HomeBaseActivity(), VegaCoffeeGrnWBListFragment.CallBack,
    VegaCoffeeGrnOfflineSummary.CallBack, VegaCoffeeGrnDetailsFragment.CallBack {

    private val mTAG = VegaCoffeeGRNActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_coffee_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCoffeeGrnWBListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flCoffeeGrn, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId) {
        when (moveFrag) {
            GRN_FRAG -> displayFragment(VegaCoffeeGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaCoffeeGrnOfflineSummary.newInstance(), true)
            QUALITY_DETAILS -> displayFragment(VegaCoffeeGRNLotQualityFragment.newInstance(item), true)
        }
    }
}
