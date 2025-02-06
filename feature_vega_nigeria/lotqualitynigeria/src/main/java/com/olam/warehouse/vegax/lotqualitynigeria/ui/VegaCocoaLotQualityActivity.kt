package com.olam.warehouse.vegax.lotqualitynigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.lotqualitynigeria.R
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityInspectionLots
import com.olam.warehouse.vegax.lotqualitynigeria.di.injectCocoaLotQualityFeature
import com.olam.warehouse.vegax.lotqualitynigeria.ui.params.VegaCocoaLotQualityParamsFragment


class VegaCocoaLotQualityActivity : HomeBaseActivity(),
    VegaCocoaLotQualityLotListFragment.CallBack {

    private val mTAG = VegaCocoaLotQualityActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_cocoa_lot_quality


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCocoaLotQualityFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaCocoaLotQualityLotListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flLotQuality, allowBackStack = flag)
    }

    override fun replaceQualityFragment(
        paramsFrag: String,
        item: VegaCocoaLotQualityInspectionLots
    ) {
        displayFragment(VegaCocoaLotQualityParamsFragment.newInstance(item), true)
    }

}
