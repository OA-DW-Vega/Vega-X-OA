package com.olam.warehouse.vegax.dispatchindiacoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.dispatchindiacoffee.R
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaIndiaCoffeeMtntAssignLot
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaIndiaCoffeeMtntMaterial
import com.olam.warehouse.vegax.dispatchindiacoffee.di.injectVegaDispatchFeature
import com.olam.warehouse.vegax.dispatchindiacoffee.ui.mtnt.VegaDispatchMtntFragment
import com.olam.warehouse.vegax.dispatchindiacoffee.ui.mtnt.VegaDispatchSummaryFragment
import com.olam.warehouse.vegax.dispatchindiacoffee.ui.mtnt.VegaIndiaCoffeeMtntAssignLotFragment
import com.olam.warehouse.vegax.dispatchindiacoffee.ui.truck.VegaDispatchTruckListFragment
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.DISPATCH_PROCESS
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.DISPATCH_SUMMARY_FRAG

/**
 * Created by Baskaran Kannan on 2/10/2020.
 */
class VegaDispatchActivity : HomeBaseActivity(), VegaDispatchTruckListFragment.CallBack,
    VegaDispatchMtntFragment.CallBack,VegaDispatchSummaryFragment.CallBack,
    VegaIndiaCoffeeMtntAssignLotFragment.VegaIndiaCoffeeAsignLotCallback {
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

    override fun navigateToAssignLot(
        lotList: ArrayList<VegaDispatchLots>,
        materialList: ArrayList<VegaIndiaCoffeeMtntMaterial>
    ) {
        displayFragment(
            VegaIndiaCoffeeMtntAssignLotFragment.newInstance(lotList,materialList),
            true
        )
    }

    override fun updateAssignLotText(
        mergedLotIds: List<String?>,
        list: ArrayList<VegaIndiaCoffeeMtntAssignLot>,
        selectedStorageLocation: String,
        dispatchLotList: ArrayList<VegaDispatchLots>
    ) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flDispatch)
        when (fragment) {
            is VegaDispatchSummaryFragment -> {
                fragment.updateAssignLotText(mergedLotIds,list,selectedStorageLocation,dispatchLotList)
            }
        }
    }
}
