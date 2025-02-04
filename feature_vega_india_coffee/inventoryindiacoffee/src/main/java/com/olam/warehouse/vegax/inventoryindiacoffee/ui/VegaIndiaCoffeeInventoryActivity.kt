package com.olam.warehouse.vegax.inventoryindiacoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventoryindiacoffee.R
import com.olam.warehouse.vegax.inventoryindiacoffee.di.injectVegaIndiaCoffeeInventoryFeature
import com.olam.warehouse.vegax.inventoryindiacoffee.utils.FILTER_LIST
import com.olam.warehouse.vegax.inventoryindiacoffee.utils.INVENTORY_DETAILS


class VegaIndiaCoffeeInventoryActivity : HomeBaseActivity(), VegaIndiaCoffeeInventoryFilterFragment.CallBack,
    VegaIndiaCoffeeInventoryListFragment.CallBack,
    VegaIndiaCoffeeInventoryDetailsFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_india_coffee_inventory
    private val mTAG = VegaIndiaCoffeeInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaIndiaCoffeeInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)
        when (lotId?.isNotEmpty()) {
            true -> {
                val bundle = Bundle()
                bundle.putString("LOTID", lotId)
                displayFragment(VegaIndiaCoffeeInventoryDetailsFragment.newInstance(bundle), false)

            }
            else -> displayFragment(VegaIndiaCoffeeInventoryListFragment.newInstance(), false)
        }

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorInventory, allowBackStack = flag)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaIndiaCoffeeInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if (INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaIndiaCoffeeInventoryDetailsFragment.newInstance(bundle), true)
        } else if (FILTER_LIST.equals(moveFrag)) {
            applyFilter(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorInventory)
        when (fragment) {
            is VegaIndiaCoffeeInventoryDetailsFragment -> {
                fragment.applyFilter(bundle)
            }
        }
    }

}
