package com.olam.warehouse.vegax.gateentrycameroon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaTrackTraceListener
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.gateentrycameroon.R
import com.olam.warehouse.vegax.gateentrycameroon.di.injectGateEntryCameroonFeature
import com.olam.warehouse.vegax.gateentrycameroon.utils.*

/**
 * Created by Baskaran Kannan on 3/4/2020.
 */
class VegaGateEntryCameroonActivity : HomeBaseActivity(), VegaGateEntryCameroonTypeFragment.CallBack,
    VegaCameroonWaitingTruckListFragment.CallBack, VegaCameroonAddNewTruckFragment.CallBack, VegaTrackTraceListener {
    private val mTAG = VegaGateEntryCameroonActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_gate_entry_cameroon
    var procurementType = ""
    var complaintType = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGateEntryCameroonFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        procurementType = intent.getStringExtra(Constants.PROCUREMENT_TYPE)?:""
        complaintType = intent.getStringExtra(Constants.COMPLAINT_TYPE)?:""
        displayFragment(VegaGateEntryCameroonTypeFragment.newInstance(), false)
        var gateEntryData = VegaGateEntry()
        gateEntryData.weighBridgeType = PROCURE
        displayFragment(VegaCameroonWaitingTruckListFragment.newInstance(gateEntryData), false)
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
            displayFragment(VegaCameroonWaitingTruckListFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaCameroonAddNewTruckFragment.newInstance(gateEntryData, procurementType, complaintType), true)
        }

    }

    override fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry, plantDetails: Plant) {
        if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntryCameroonSummaryFragment.newInstance(gateEntryData, plantDetails), true)
        }
    }

    override fun isVendor(flag: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flGateentry)
        when (fragment) {
            is VegaCameroonAddNewTruckFragment -> {
//                fragment.isVendor(flag)
            }
        }
    }

    override fun isComplaint(status: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flGateentry)
        when (fragment) {
            is VegaCameroonAddNewTruckFragment -> {
//                fragment.isComplaint(status)
            }
        }
    }

    override fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flGateentry)
        when (fragment) {
            is VegaCameroonAddNewTruckFragment -> {
//                fragment.updateSourceLotDetails(sourceLotDetails)
            }
        }
    }

    override fun updateTransactionIdDetails(transactionIdDetails: TrackTraceTransactionIdDetails) {

    }

    override fun updateFarmerDetails(farmerListDetails: ArrayList<TrackTraceFarmerModel>) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flGateentry)
        when (fragment) {
            is VegaCameroonAddNewTruckFragment -> {
//                fragment.updateFarmerListDetails(farmerListDetails)
            }
        }
    }

}
