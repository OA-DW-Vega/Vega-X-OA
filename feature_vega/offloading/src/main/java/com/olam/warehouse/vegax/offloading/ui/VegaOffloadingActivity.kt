package com.olam.warehouse.vegax.offloading.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloading.R
import com.olam.warehouse.vegax.offloading.di.injectOffloadingFeature
import com.olam.warehouse.vegax.offloading.ui.presampling.VegaOffloadingPreSamplingFragment
import com.olam.warehouse.vegax.offloading.ui.receivingtype.VegaReceivingTypeFragments
import com.olam.warehouse.vegax.offloading.ui.truck.VegaOffloadingTruckListFragment
import com.olam.warehouse.vegax.offloading.utils.MTNR
import com.olam.warehouse.vegax.offloading.utils.PARAMS_LIST_FRAG
import com.olam.warehouse.vegax.offloading.utils.SUMMARY_FRAG
import com.olam.warehouse.vegax.offloading.utils.SUPPLIER

/**
 * Created by Baskaran Kannan on 1/27/2020.
 */
class VegaOffloadingActivity : HomeBaseActivity(),
    VegaReceivingTypeFragments.CallBack,
    VegaOffloadingTruckListFragment.CallBack,
    VegaOffloadingPreSamplingFragment.CallBack {

    private val mTAG = VegaOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_offloading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaReceivingTypeFragments.newInstance(), false)
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
            displayFragment(VegaOffloadingTruckListFragment.newInstance(offloadingData), true)
        } else if (receivingType == PARAMS_LIST_FRAG) {
            displayFragment(VegaOffloadingPreSamplingFragment.newInstance(offloadingData), true)
        }
    }

    override fun replaceFragment(
        paramsListFrag: String,
        item: VegaOffloadingTrucks,
        offloadingParamList: ArrayList<VegaQualityParameter>
    ) {
        if (paramsListFrag == SUMMARY_FRAG) {
            displayFragment(VegaOffloadingBagSummaryFragment.newInstance(item, offloadingParamList), true)
        }

    }
}
