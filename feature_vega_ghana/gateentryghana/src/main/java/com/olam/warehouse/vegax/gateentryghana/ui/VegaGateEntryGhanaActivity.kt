package com.olam.warehouse.vegax.gateentryghana.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentryghana.R
import com.olam.warehouse.vegax.gateentryghana.di.injectGateEntryGhanaFeature
import com.olam.warehouse.vegax.gateentryghana.utils.MTNR
import com.olam.warehouse.vegax.gateentryghana.utils.PARAMS_LIST_FRAG
import com.olam.warehouse.vegax.gateentryghana.utils.SUMMARY_FRAG
import com.olam.warehouse.vegax.gateentryghana.utils.SUPPLIER

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
class VegaGateEntryGhanaActivity : HomeBaseActivity(), VegaGateEntryGhanaTypeFragment.CallBack,
    VegaGhanaWaitingTruckListFragment.CallBack, VegaGhanaAddNewTruckFragment.CallBack {
    private val mTAG = VegaGateEntryGhanaActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_gate_entry_ghana

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGateEntryGhanaFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGateEntryGhanaTypeFragment.newInstance(), false)
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
            displayFragment(VegaGhanaWaitingTruckListFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaGhanaAddNewTruckFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntryGhanaSummaryFragment.newInstance(gateEntryData), true)
        }
    }

}
