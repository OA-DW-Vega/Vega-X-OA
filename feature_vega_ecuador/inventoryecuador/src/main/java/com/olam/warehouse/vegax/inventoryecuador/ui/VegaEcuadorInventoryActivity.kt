package com.olam.warehouse.vegax.inventoryecuador.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventoryecuador.R
import com.olam.warehouse.vegax.inventoryecuador.di.injectVegaEcuadorInventoryFeature
import com.olam.warehouse.vegax.inventoryecuador.utils.FILTER_LIST
import com.olam.warehouse.vegax.inventoryecuador.utils.INVENTORY_DETAILS

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaEcuadorInventoryActivity : HomeBaseActivity(), VegaEcuadorInventoryFilterFragment.CallBack,
    VegaEcuadorInventoryListFragment.CallBack,
    VegaEcuadorInventoryDetailsFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_ecuador_inventory
    private val mTAG = VegaEcuadorInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaEcuadorInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaEcuadorInventoryListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        //fragment.arguments = bundle
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorInventory, allowBackStack = flag)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaEcuadorInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if (INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaEcuadorInventoryDetailsFragment.newInstance(bundle), true)
        } else if (FILTER_LIST.equals(moveFrag)) {
            applyFilter(bundle)
        }
    }

//    override fun replaceFragment(moveFrag: String, bundle: Bundle, fullFilter: ArrayList<String>) {
//        if (INVENTORY_DETAILS.equals(moveFrag)) {
//            bundle.putStringArrayList(FULL_FILTER, fullFilter)
//            displayFragment(VegaEcuadorInventoryDetailsFragment.newInstance(bundle), true)
//        } else {
//            bundle.putStringArrayList(FULL_FILTER, fullFilter)
//            displayFragment(VegaEcuadorInventoryFilterFragment.newInstance(bundle, fullFilter), true)
//        }
//    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorInventory)
        when (fragment) {
            is VegaEcuadorInventoryDetailsFragment -> {
                fragment.applyFilter(bundle)
            }
        }
    }

}
