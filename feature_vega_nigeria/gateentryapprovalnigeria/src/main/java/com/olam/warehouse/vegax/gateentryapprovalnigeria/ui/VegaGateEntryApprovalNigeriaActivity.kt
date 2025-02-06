package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaGateEntryDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentryapprovalnigeria.R
import com.olam.warehouse.vegax.gateentryapprovalnigeria.di.injectGateEntryApprovalNigeriaFeature
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.*

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
class VegaGateEntryApprovalNigeriaActivity : HomeBaseActivity(), VegaNigeriaApprovalReplaceFragmentCallback,VegaGateEntryApprovalNigeriaTypeFragment.CallBack,
    VegaApprovalNigeriaWaitingTruckListFragment.CallBack, VegaApprovalNigeriaAddNewTruckFragment.CallBack {
    private val mTAG = VegaGateEntryApprovalNigeriaActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_gate_entry_approval_nigeria
    var gateEntryData = VegaGateEntryDetails()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGateEntryApprovalNigeriaFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGateEntryApprovalNigeriaTypeFragment.newInstance(), false)
        /*var gateEntryData = VegaGateEntryDetails()*/
        gateEntryData.wtype = PROCURE
        /*displayFragment(
            VegaApprovalNigeriaWaitingTruckListFragment.newInstance(gateEntryData),
            false
        )*/
        displayFragment(VegaGateEntryNigeriaApprovalSelectDispatchTypeFragment.newInstance(), false)
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
        plantDetails: Plant, materialName: String,
        uom: String, vendorName: String
    ) {
        if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(
                VegaGateEntryApprovalNigeriaSummaryFragment.newInstance(
                    gateEntryData,
                    plantDetails, materialName, uom, vendorName
                ), true
            )
        }
    }

    override fun replaceFragment(paramsListFrag: String, item: VegaGateEntry, plantDetails: Plant) {
        TODO("Not yet implemented")
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHBRIDGE -> {
                gateEntryData.wsgate = WB01
               // gateEntryData.wtype = WB
                displayFragment(
                    VegaApprovalNigeriaWaitingTruckListFragment.newInstance(gateEntryData),
                    false
                )
            }
            MTNT_WEIGHSCALE -> {
                gateEntryData.wsgate = WS01
              //  gateEntryData.wtype = WS
                displayFragment(
                    VegaApprovalNigeriaWaitingTruckListFragment.newInstance(gateEntryData),
                    false
                )
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
