package com.olam.warehouse.vegax.inventorycoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventorycoffee.ui.filter.VegaCoffeeInventoryFilterFragment
import com.olam.warehouse.vegax.inventorycoffee.R
import com.olam.warehouse.vegax.inventorycoffee.di.injectCoffeeInventoryFeature
import com.olam.warehouse.vegax.inventorycoffee.ui.details.VegaCoffeeInventoryDetailsFragment
import com.olam.warehouse.vegax.inventorycoffee.utils.CallBack

class VegaCoffeeInventoryActivity : HomeBaseActivity(), CallBack {

    private val mTAG = VegaCoffeeInventoryActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_coffee_inventory
    private var lotId: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeInventoryFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)
        when (lotId?.isNotEmpty()) {
            true -> {
                val bundle = Bundle()
                bundle.putString("LOTID", lotId)
                displayFragment(VegaCoffeeInventoryDetailsFragment(), false, bundle)
            }
            else -> displayFragment(VegaCoffeeInventoryListFragment(), false, Bundle())
        }

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean, bundle: Bundle) {
        fragment.arguments = bundle
        replaceFragment(fragment, mTAG, true, R.id.flInventory, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if ("details".equals(moveFrag)) {
            displayFragment(VegaCoffeeInventoryDetailsFragment(), true, bundle)
        } else if ("filterList".equals(moveFrag)) {
            applyFilters(bundle)
        } else {
            displayFragment(VegaCoffeeInventoryFilterFragment(), true, bundle)
        }
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        if ("details".equals(moveFrag)) {
            bundle.putStringArrayList("FULL_FILTER", fullFilter)
            displayFragment(VegaCoffeeInventoryDetailsFragment(), true, bundle)
        } else {
            bundle.putStringArrayList("FULL_FILTER", fullFilter)
            displayFragment(VegaCoffeeInventoryFilterFragment(), true, bundle)
        }
    }

    private fun applyFilters(bundle: Bundle) {
        val kors = bundle.getStringArrayList("KOR") ?: ArrayList()
        val origins = bundle.getStringArrayList("Origin") ?: ArrayList()
        val materials = bundle.getStringArrayList("Material") ?: ArrayList()
        val fragment = supportFragmentManager.findFragmentById(R.id.flInventory)
        when (fragment) {
            is VegaCoffeeInventoryListFragment -> {
                fragment.applyFilterValues(kors, origins)
            }
            is VegaCoffeeInventoryDetailsFragment -> {
                fragment.applyFilterValues(kors, origins, materials)
            }
        }
    }

    override fun filterList(list: ArrayList<String>) {
    }
}
