package com.olam.warehouse.vegax.shipment.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.shipment.R
class VegaShipmentActivity() : HomeBaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_shipment
    private val mTAG = VegaShipmentActivity::class.java.canonicalName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaShipmentApproveRejectListingFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flShipment, allowBackStack = flag)
    }

}
