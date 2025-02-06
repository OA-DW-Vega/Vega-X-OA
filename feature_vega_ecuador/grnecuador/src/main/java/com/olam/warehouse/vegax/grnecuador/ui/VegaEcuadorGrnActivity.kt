package com.olam.warehouse.vegax.grnecuador.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnecuador.R
import com.olam.warehouse.vegax.grnecuador.di.injectEcuadorGrnFeature
import com.olam.warehouse.vegax.grnecuador.ui.offline.VegaEcuadorGrnOfflineSummary
import com.olam.warehouse.vegax.grnecuador.ui.weighbridge.VegaEcuadorGrnSummaryFragment
import com.olam.warehouse.vegax.grnecuador.ui.weighbridge.VegaEcuadorGrnWeighbridgeListFragment
import com.olam.warehouse.vegax.grnecuador.ui.weighbridge.VegaEcuadorWeighbridgeGrnDetailsFragment
import com.olam.warehouse.vegax.grnecuador.utils.*
import com.olam.warehouse.vegax.grnecuador.utils.GRN_OFFLINE_FRAG

/**
 * Created by Roshna Parambil on 1/13/2022.
 */
class VegaEcuadorGrnActivity : HomeBaseActivity(), VegaEcuadorGrnWBListFragment.CallBack,
    VegaEcuadorGrnOfflineSummary.CallBack, VegaEcuadorWeighmentTypeFragment.CallBack,
    VegaEcuadorWeighbridgeGrnDetailsFragment.CallBack {

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
        displayFragment(VegaEcuadorWeighmentTypeFragment(), false)
//        displayFragment(VegaEcuadorGrnWBListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId) {
        when (moveFrag) {
            GRN_FRAG -> displayFragment(VegaEcuadorGrnDetailsFragment.newInstance(item), true)
            GRN_WB_FRAG -> displayFragment(VegaEcuadorWeighbridgeGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaEcuadorGrnOfflineSummary.newInstance(), true)
            GRN_SUMMARY_FRAG -> displayFragment(VegaEcuadorGrnSummaryFragment.newInstance(item,item.unitPrice.toString()), true)
        }
    }

    override fun replaceFragment(weighmentType: String) {
        displayFragment(VegaEcuadorGrnWBListFragment.newInstance(weighmentType), false)
//        when (weighmentType) {
//            WEIGHSCALE -> displayFragment(VegaEcuadorGrnWBListFragment.newInstance(weighmentType), true)
//            WEIGHBRIDGE -> displayFragment(VegaEcuadorGrnWBListFragment.newInstance(weighmentType), true)
//        }
    }
}
