package com.olam.warehouse.vegax.grnindiacoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnindiacoffee.R
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGRNQuality
import com.olam.warehouse.vegax.grnindiacoffee.di.injectNigeriaSesameGrnFeature
import com.olam.warehouse.vegax.grnindiacoffee.ui.offline.VegaIndiaCoffeeGrnOfflineSummary
import com.olam.warehouse.vegax.grnindiacoffee.utils.GRN_FRAG
import com.olam.warehouse.vegax.grnindiacoffee.utils.GRN_OFFLINE_FRAG
import com.olam.warehouse.vegax.grnindiacoffee.utils.GRN_QUALITY_DETAILS


class VegaIndiaCoffeeGrnActivity : HomeBaseActivity(), VegaIndiaCoffeeGrnWBListFragment.CallBack,
    VegaIndiaCoffeeGrnOfflineSummary.CallBack, VegaIndiaCoffeeGrnDetailsFragment.QualityDetailsCallBack {

    private val mTAG = VegaIndiaCoffeeGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_india_coffee_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaSesameGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaIndiaCoffeeGrnWBListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }


    override fun replaceFragment(
        moveFrag: String,
        item: VegaGrnWeighBridgeId
    ) {
        when (moveFrag) {
            GRN_FRAG -> displayFragment(VegaIndiaCoffeeGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaIndiaCoffeeGrnOfflineSummary.newInstance(), true)

        }
    }

    override fun replaceQualityDetailsFragment(
        moveFrag: String,
        wbDetails: VegaGrnWeighBridgeId,
        approveQualityList: ArrayList<VegaIndiaCoffeeGRNQuality>
    ) {
        when (moveFrag) {
            GRN_QUALITY_DETAILS ->
                displayFragment(VegaIndiaCoffeeGRNQualityFragment.newInstance(wbDetails, approveQualityList), true)
        }
    }

}
