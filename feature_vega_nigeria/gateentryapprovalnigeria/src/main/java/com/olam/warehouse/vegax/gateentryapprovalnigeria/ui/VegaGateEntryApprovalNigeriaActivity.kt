package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaGateEntryDetails
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentryapprovalnigeria.R
import com.olam.warehouse.vegax.gateentryapprovalnigeria.di.injectGateEntryApprovalNigeriaFeature
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.PROCURE
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.SUMMARY_FRAG

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
class VegaGateEntryApprovalNigeriaActivity : HomeBaseActivity(), VegaGateEntryApprovalNigeriaTypeFragment.CallBack,
    VegaApprovalNigeriaWaitingTruckListFragment.CallBack, VegaApprovalNigeriaAddNewTruckFragment.CallBack {
    private val mTAG = VegaGateEntryApprovalNigeriaActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_gate_entry_approval_nigeria

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGateEntryApprovalNigeriaFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGateEntryApprovalNigeriaTypeFragment.newInstance(), false)
        var gateEntryData = VegaGateEntryDetails()
        gateEntryData.wtype = PROCURE
        displayFragment(
            VegaApprovalNigeriaWaitingTruckListFragment.newInstance(gateEntryData),
            false
        )
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
        /*if (gateEntryType == SUPPLIER || gateEntryType == MTNR) {
            displayFragment(VegaApprovalNigeriaWaitingTruckListFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaApprovalNigeriaAddNewTruckFragment.newInstance(gateEntryData), true)
        }
        *//*else if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntryApprovalNigeriaSummaryFragment.newInstance(gateEntryData), true)
        }*/
    }

    override fun replaceFragment(
        gateEntryType: String,
        gateEntryData: VegaGateEntryDetails,
        plantDetails: Plant
    ) {
        if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(
                VegaGateEntryApprovalNigeriaSummaryFragment.newInstance(
                    gateEntryData,
                    plantDetails
                ), true
            )
        }
    }

    override fun replaceFragment(paramsListFrag: String, item: VegaGateEntry, plantDetails: Plant) {
        TODO("Not yet implemented")
    }

}
