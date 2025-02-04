package com.olam.warehouse.vegax.approve.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.approve.R
import com.olam.warehouse.vegax.approve.di.injectApprovalFeature
import com.olam.warehouse.vegax.approve.ui.weighbridge.VegaApproveWeighbridgeListFragment

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

class VegaApproveActivity : HomeBaseActivity(), OnFragmentInteractionListener {
    override fun onFragmentInteraction(fragment: Fragment) {
        displayFragment(fragment, true)
    }

    private val mTAG = VegaApproveActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_approve //To change initializer of created properties use File | Settings | File Templates.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectApprovalFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaApproveWeighbridgeListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flApprove, allowBackStack = flag)
    }
}
