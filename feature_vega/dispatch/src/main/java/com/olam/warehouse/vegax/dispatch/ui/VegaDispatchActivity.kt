package com.olam.warehouse.vegax.dispatch.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.dispatch.R
import com.olam.warehouse.vegax.dispatch.di.injectVegaDispatchFeature
import com.olam.warehouse.vegax.dispatch.ui.mtnt.VegaDispatchMtntFragment
import com.olam.warehouse.vegax.dispatch.ui.mtnt.VegaDispatchSummaryFragment
import com.olam.warehouse.vegax.dispatch.ui.truck.VegaDispatchTruckListFragment
import com.olam.warehouse.vegax.dispatch.utils.DISPATCH_PROCESS
import com.olam.warehouse.vegax.dispatch.utils.DISPATCH_SUMMARY_FRAG

/**
 * Created by Baskaran Kannan on 2/10/2020.
 */
class VegaDispatchActivity : HomeBaseActivity(), VegaDispatchTruckListFragment.CallBack,
    VegaDispatchMtntFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_dispatch
    private val mTAG = VegaDispatchActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(UIUtils.DISPATCH_DATA)) {
            var dispatchData = VegaDispatchTrucks()
            var postData = ArrayList<VegaDispatchLots>()
            dispatchData = intent.getParcelableExtra(UIUtils.DISPATCH_DATA)!!
            postData = intent.getParcelableArrayListExtra(UIUtils.DISPATCH_POST_DATA)!!
            when (dispatchData.status) {
                4 -> displayFragment(VegaDispatchSummaryFragment.newInstance(postData, dispatchData), false)
                else -> displayFragment(VegaDispatchMtntFragment.newInstance(dispatchData, postData), false)
            }

        } else
            displayFragment(VegaDispatchTruckListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, allowStateLoss = true, containerViewId = R.id.flDispatch, allowBackStack = flag)
    }

    override fun replaceFragment(
        moveFrag: String,
        dispatchLotsList: MutableList<VegaDispatchLots>,
        dispatchData: VegaDispatchTrucks
    ) {
        when (moveFrag) {
            DISPATCH_SUMMARY_FRAG -> displayFragment(
                VegaDispatchSummaryFragment.newInstance(
                    dispatchLotsList as ArrayList<VegaDispatchLots>,
                    dispatchData
                ), true
            )
        }
    }

    override fun replaceFragment(moveFrag: String, item: VegaDispatchTrucks) {
        when (moveFrag) {
            DISPATCH_PROCESS -> displayFragment(
                VegaDispatchMtntFragment.newInstance(
                    item,
                    arrayListOf<VegaDispatchLots>()
                ), true
            )
        }
    }
}
