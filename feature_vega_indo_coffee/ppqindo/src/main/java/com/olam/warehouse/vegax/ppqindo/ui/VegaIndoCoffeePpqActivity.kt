package com.olam.warehouse.vegax.ppqindo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.ppqindo.R
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindo.di.injectIndoCoffeePpqFeature1

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeePpqActivity : HomeBaseActivity(),
    VegaIndoCoffeePpqLotListFragment.CallBack {

    private val mTAG = VegaIndoCoffeePpqActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_indo_coffee_ppq

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndoCoffeePpqFeature1()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaIndoCoffeePpqLotListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPpq, allowBackStack = flag)
    }

    override fun replaceQualityFragment(paramsFrag: String, item: VegaIndoCoffeePpqInspectionLots) {
        displayFragment(VegaIndoCoffeePpqQualityParamsFragment.newInstance(item), true)
    }
}

