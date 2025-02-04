package com.olam.warehouse.vegax.inventorycameroon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventorycameroon.R
import com.olam.warehouse.vegax.inventorycameroon.di.injectVegaCameroonInventoryFeature
import com.olam.warehouse.vegax.inventorycameroon.utils.FILTER_LIST
import com.olam.warehouse.vegax.inventorycameroon.utils.INVENTORY_DETAILS

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaCameroonInventoryActivity : HomeBaseActivity(), VegaCameroonInventoryFilterFragment.CallBack,
    VegaCameroonInventoryListFragment.CallBack,
    VegaCameroonInventoryDetailsFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_cameroon_inventory
    private val mTAG = VegaCameroonInventoryActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaCameroonInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCameroonInventoryListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorInventory, allowBackStack = flag)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaCameroonInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if (INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaCameroonInventoryDetailsFragment.newInstance(bundle), true)
        } else if (FILTER_LIST.equals(moveFrag)) {
            applyFilter(bundle)
        }
    }


    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorInventory)
        when (fragment) {
            is VegaCameroonInventoryDetailsFragment -> {
                fragment.applyFilter(bundle)
            }
        }
    }

}
