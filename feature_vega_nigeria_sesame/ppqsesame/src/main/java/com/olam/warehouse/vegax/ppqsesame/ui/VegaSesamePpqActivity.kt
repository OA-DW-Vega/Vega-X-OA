package com.olam.warehouse.vegax.ppqsesame.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.ppqsesame.R
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLots
import com.olam.warehouse.vegax.ppqsesame.di.injectSesamePpqFeature

class VegaSesamePpqActivity : HomeBaseActivity(),
    VegaSesamePpqLotListFragment.CallBack {

    private val mTAG = VegaSesamePpqActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_sesame_ppq

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectSesamePpqFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaSesamePpqLotListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPpq, allowBackStack = flag)
    }

    override fun replaceQualityFragment(paramsFrag: String, item: VegaSesamePpqInspectionLots) {
        displayFragment(VegaSesamePpqQualityParamsFragment.newInstance(item), true)
    }
}
