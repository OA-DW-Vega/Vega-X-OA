package com.olam.warehouse.vegax.inventory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventory.R
import com.olam.warehouse.vegax.inventory.di.injectInventoryFeature
import com.olam.warehouse.vegax.inventory.ui.details.VegaInventoryDetailsFragment
import com.olam.warehouse.vegax.inventory.ui.filter.VegInventoryFilterFragment

class VegaInventoryActivity : HomeBaseActivity(), CallBack {

    private val mTAG = VegaInventoryActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_inventory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectInventoryFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaInventoryListFragment(), false, Bundle())
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean, bundle: Bundle) {
        fragment.arguments = bundle
        replaceFragment(fragment, mTAG, true, R.id.flInventory, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if ("details".equals(moveFrag)) {
            displayFragment(VegaInventoryDetailsFragment(), true, bundle)
        } else if ("filterList".equals(moveFrag)) {
            applyFilters(bundle)
        } else {
            displayFragment(VegInventoryFilterFragment(), true, bundle)
        }
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        if ("details".equals(moveFrag)) {
            bundle.putStringArrayList("FULL_FILTER", fullFilter)
            displayFragment(VegaInventoryDetailsFragment(), true, bundle)
        } else {
            bundle.putStringArrayList("FULL_FILTER", fullFilter)
            displayFragment(VegInventoryFilterFragment(), true, bundle)
        }
    }

    private fun applyFilters(bundle: Bundle) {
        val kors = bundle.getStringArrayList("KOR") ?: ArrayList()
        val origins = bundle.getStringArrayList("Origin") ?: ArrayList()
        val fragment = supportFragmentManager.findFragmentById(R.id.flInventory)
        when (fragment) {
            is VegaInventoryListFragment -> {
                fragment.applyFilterValues(kors, origins)
            }
            is VegaInventoryDetailsFragment -> {
                fragment.applyFilterValues(kors, origins)
            }
        }
    }

    override fun filterList(list: ArrayList<String>) {
    }
}
