package com.olam.warehouse.vegax.grncameroon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grncameroon.R
import com.olam.warehouse.vegax.grncameroon.di.injectCameroonGrnFeature
import com.olam.warehouse.vegax.grncameroon.ui.VegaCameroonGrnDetailsFragment
import com.olam.warehouse.vegax.grncameroon.ui.VegaCameroonGrnWBListFragment
import com.olam.warehouse.vegax.grncameroon.ui.offline.VegaCameroonGrnOfflineSummary
import com.olam.warehouse.vegax.grncameroon.utils.GRN_FRAG
import com.olam.warehouse.vegax.grncameroon.utils.GRN_OFFLINE_FRAG

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaCameroonGrnActivity : HomeBaseActivity(), VegaCameroonGrnWBListFragment.CallBack,
    VegaCameroonGrnOfflineSummary.CallBack {

    private val mTAG = VegaCameroonGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_cameroon_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCameroonGrnWBListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId) {
        when(moveFrag) {
            GRN_FRAG -> displayFragment(VegaCameroonGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaCameroonGrnOfflineSummary.newInstance(), true)
        }
    }
}
