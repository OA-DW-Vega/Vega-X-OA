package com.olam.warehouse.vegax.gateentryghanacocoa.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentryghanacocoa.R
import com.olam.warehouse.vegax.gateentryghanacocoa.di.injectGateEntryGhanaFeature
import com.olam.warehouse.vegax.gateentryghanacocoa.utils.*


class VegaGateEntryGhanaCocoaActivity : HomeBaseActivity(), VegaGateEntryGhanaCocoaTypeFragment.CallBack,
    VegaGhanaCocoaWaitingTruckListFragment.CallBack, VegaGhanaCocoaAddNewTruckFragment.CallBack {
    private val mTAG = VegaGateEntryGhanaCocoaActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_gate_entry_ghana_cocoa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGateEntryGhanaFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
//        displayFragment(VegaGateEntryGhanaCocoaTypeFragment.newInstance(), false)
        var gateEntryData = VegaGateEntry()
        gateEntryData.weighBridgeType = STO
        displayFragment(VegaGhanaCocoaAddNewTruckFragment.newInstance(gateEntryData), false)
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
            displayFragment(VegaGhanaCocoaWaitingTruckListFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaGhanaCocoaAddNewTruckFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntryGhanaCocoaSummaryFragment.newInstance(gateEntryData), true)
        }
    }

}
