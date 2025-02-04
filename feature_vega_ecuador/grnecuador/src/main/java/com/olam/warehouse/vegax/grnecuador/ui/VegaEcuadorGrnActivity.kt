package com.olam.warehouse.vegax.grnecuador.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnecuador.R
import com.olam.warehouse.vegax.grnecuador.di.injectEcuadorGrnFeature
import com.olam.warehouse.vegax.grnecuador.ui.offline.VegaEcuadorGrnOfflineSummary
import com.olam.warehouse.vegax.grnecuador.utils.GRN_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.GRN_OFFLINE_FRAG

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaEcuadorGrnActivity : HomeBaseActivity(), VegaEcuadorGrnWBListFragment.CallBack,
    VegaEcuadorGrnOfflineSummary.CallBack {

    private val mTAG = VegaEcuadorGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_ecuador_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectEcuadorGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaEcuadorGrnWBListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId) {
        when(moveFrag) {
            GRN_FRAG -> displayFragment(VegaEcuadorGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaEcuadorGrnOfflineSummary.newInstance(), true)
        }
    }
}
