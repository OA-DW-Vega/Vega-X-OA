package com.olam.warehouse.vegax.gateentrycoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentrycoffee.R
import com.olam.warehouse.vegax.gateentrycoffee.di.injectCoffeeGateEntryFeature
import com.olam.warehouse.vegax.gateentrycoffee.utils.*


class VegaCoffeeGateEntryActivity : HomeBaseActivity(), VegaCoffeeReplaceCallback {
    private val mTAG = VegaCoffeeGateEntryActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_coffee_gate_entry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeGateEntryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCoffeeGateEntryTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flGateentry,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(gateEntryType: String, gateEntryData: Any) {
        if (gateEntryType == SUPPLIER || gateEntryType == MTNR) {
            displayFragment(VegaCoffeeWaitingTruckListFragment.newInstance(gateEntryData as VegaGateEntry), true)
        } else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaCoffeeAddNewTruckFragment.newInstance(gateEntryData as VegaGateEntry), true)
        } else if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaCoffeeGateEntrySummaryFragment.newInstance(gateEntryData as VegaGateEntry), true)
        } else if (gateEntryType == WEIGHMENT_TYPE) {
            displayFragment(VegaCoffeeMtnRTypeFragment.newInstance(gateEntryData as String), true)
        }
    }
}
