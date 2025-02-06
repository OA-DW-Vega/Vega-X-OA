package com.olam.warehouse.vegax.gateentry.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentry.R
import com.olam.warehouse.vegax.gateentry.di.injectGateEntryFeature
import com.olam.warehouse.vegax.gateentry.utils.MTNR
import com.olam.warehouse.vegax.gateentry.utils.PARAMS_LIST_FRAG
import com.olam.warehouse.vegax.gateentry.utils.SUMMARY_FRAG
import com.olam.warehouse.vegax.gateentry.utils.SUPPLIER

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
class VegaGateEntryActivity : HomeBaseActivity(), VegaGateEntryTypeFragment.CallBack,
    VegaWaitingTruckListFragment.CallBack, VegaAddNewTruckFragment.CallBack {
    private val mTAG = VegaGateEntryActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_gate_entry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGateEntryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGateEntryTypeFragment.newInstance(), false)
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

    override fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry) {
        if (gateEntryType == SUPPLIER || gateEntryType == MTNR) {
            displayFragment(VegaWaitingTruckListFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaAddNewTruckFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntrySummaryFragment.newInstance(gateEntryData), true)
        }
    }

}
