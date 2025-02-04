package com.olam.warehouse.vegax.ppqindiacoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.ppqindiacoffee.R
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindiacoffee.di.injectIndiaCoffeePpqFeature

class VegaIndiaCoffeePpqActivity : HomeBaseActivity(),
    VegaIndiaCoffeePpqLotListFragment.CallBack {

    private val mTAG = VegaIndiaCoffeePpqActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_india_coffee_ppq

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndiaCoffeePpqFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaIndiaCoffeePpqLotListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPpq, allowBackStack = flag)
    }

    override fun replaceQualityFragment(paramsFrag: String, item: VegaIndiaCoffeePpqInspectionLots) {
        displayFragment(VegaIndiaCoffeePpqQualityParamsFragment.newInstance(item), true)
    }
}
