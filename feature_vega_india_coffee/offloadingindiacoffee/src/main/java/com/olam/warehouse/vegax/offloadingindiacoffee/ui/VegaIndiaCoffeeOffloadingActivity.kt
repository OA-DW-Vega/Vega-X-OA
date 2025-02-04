package com.olam.warehouse.vegax.offloadingindiacoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingindiacoffee.R
import com.olam.warehouse.vegax.offloadingindiacoffee.di.injectOffloadingFeature
import com.olam.warehouse.vegax.offloadingindiacoffee.ui.presampling.VegaIndiaCoffeeOffloadingPreSamplingFragment
import com.olam.warehouse.vegax.offloadingindiacoffee.ui.receivingtype.VegaIndiaCoffeeReceivingTypeFragments
import com.olam.warehouse.vegax.offloadingindiacoffee.ui.truck.VegaIndiaCoffeeOffloadingTruckListFragment
import com.olam.warehouse.vegax.offloadingindiacoffee.utils.*

class VegaIndiaCoffeeOffloadingActivity : HomeBaseActivity(),
    VegaIndiaCoffeeReceivingTypeFragments.CallBack,
    VegaIndiaCoffeeOffloadingTruckListFragment.CallBack,
    VegaIndiaCoffeeOffloadingPreSamplingFragment.CallBack {

    private val mTAG = VegaIndiaCoffeeOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_india_coffee_offloading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaIndiaCoffeeReceivingTypeFragments.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flOffloading,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, offloadingData: VegaOffloadingTrucks) {
        if (receivingType == SUPPLIER || receivingType == MTNR) {
            displayFragment(VegaIndiaCoffeeOffloadingTruckListFragment.newInstance(offloadingData), true)
        } else if (receivingType == PARAMS_LIST_FRAG) {
            displayFragment(VegaIndiaCoffeeOffloadingPreSamplingFragment.newInstance(offloadingData), true)
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaOffloadingTrucks,
        offloadingParamList: ArrayList<VegaQualityParameter>
    ) {
        if (paramsListFrag == SUMMARY_FRAG) {
            displayFragment(VegaIndiaCoffeeOffloadingBagSummaryFragment.newInstance(item, offloadingParamList), true)
        }else if(paramsListFrag == OFFLOADING_SUMMARYT_FRAG) {
            displayFragment(VegaIndiaCoffeeOffloadingBagSummaryFragment.newInstance(item, offloadingParamList), true)
        }

    }
}
