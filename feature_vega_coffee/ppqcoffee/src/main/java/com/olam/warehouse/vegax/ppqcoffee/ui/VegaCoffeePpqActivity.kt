package com.olam.warehouse.vegax.ppqcoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.ppqcoffee.R
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqcoffee.di.injectCoffeePpqFeature

class VegaCoffeePpqActivity : HomeBaseActivity(),
    VegaCoffeePpqLotListFragment.CallBack {

    private val mTAG = VegaCoffeePpqActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_coffee_ppq

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeePpqFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaCoffeePpqLotListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPpq, allowBackStack = flag)
    }

    override fun replaceQualityFragment(paramsFrag: String, item: VegaCoffeePpqInspectionLots) {
        displayFragment(VegaCoffeePpqQualityParamsFragment.newInstance(item), true)
    }
}
