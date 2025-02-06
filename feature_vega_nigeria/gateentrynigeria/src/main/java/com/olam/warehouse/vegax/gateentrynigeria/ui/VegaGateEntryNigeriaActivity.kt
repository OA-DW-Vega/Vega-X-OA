package com.olam.warehouse.vegax.gateentrynigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentrynigeria.R
import com.olam.warehouse.vegax.gateentrynigeria.di.injectGateEntryNigeriaFeature
import com.olam.warehouse.vegax.gateentrynigeria.utils.*

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
class VegaGateEntryNigeriaActivity : HomeBaseActivity(), VegaNigeriaReplaceFragmentCallback,VegaGateEntryNigeriaTypeFragment.CallBack,
    VegaNigeriaWaitingTruckListFragment.CallBack, VegaNigeriaAddNewTruckFragment.CallBack {
    private val mTAG = VegaGateEntryNigeriaActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_gate_entry_nigeria
    var gateEntryData = VegaGateEntry()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGateEntryNigeriaFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGateEntryNigeriaTypeFragment.newInstance(), false)
        //var gateEntryData = VegaGateEntry()
        gateEntryData.weighBridgeType = PROCURE/*
        displayFragment(VegaNigeriaWaitingTruckListFragment.newInstance(gateEntryData), false)*/
        displayFragment(VegaGateEntryNigeriaSelectDispatchTypeFragment.newInstance(), false)
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
            displayFragment(VegaNigeriaWaitingTruckListFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaNigeriaAddNewTruckFragment.newInstance(gateEntryData), true)
        }
        /*else if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntryNigeriaSummaryFragment.newInstance(gateEntryData), true)
        }*/
    }

    override fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry, plantDetails: Plant) {
        if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntryNigeriaSummaryFragment.newInstance(gateEntryData, plantDetails), true)
        }
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHBRIDGE -> {
                gateEntryData.wsGate = WB01
                gateEntryData.wsType = WB
                displayFragment(VegaNigeriaWaitingTruckListFragment.newInstance(gateEntryData), false)
            }
            MTNT_WEIGHSCALE -> {
                gateEntryData.wsGate = WS01
                gateEntryData.wsType = WS
                displayFragment(VegaNigeriaWaitingTruckListFragment.newInstance(gateEntryData), false)
            }
        }
    }

    override fun replaceFragment(receivingType: String, data: Any, list: VegaCocoaDispatchWB) {
        TODO("Not yet implemented")
    }

    override fun replaceFragment(receivingType: String) {
        TODO("Not yet implemented")
    }

}
