package com.olam.warehouse.vegax.inventoryghana.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventoryghana.R
import com.olam.warehouse.vegax.inventoryghana.di.injectVegaGhanaInventoryFeature
import com.olam.warehouse.vegax.inventoryghana.utils.FILTER_LIST
import com.olam.warehouse.vegax.inventoryghana.utils.INVENTORY_DETAILS
import com.olam.warehouse.vegax.inventoryghana.utils.INVENTORY_LIST

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaGhanaInventoryActivity : HomeBaseActivity(), VegaGhanaInventoryFilterFragment.CallBack,
    VegaGhanaInventorySelectionFragment.CallBack,
    VegaGhanaInventoryListFragment.CallBack,
    VegaGhanaInventoryDetailsFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_ghana_inventory
    private val mTAG = VegaGhanaInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaGhanaInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)

        val bundle = Bundle()
        bundle.putString("LOTID", lotId)
        displayFragment(VegaGhanaInventorySelectionFragment.newInstance(bundle), false)

        /* when (lotId?.isNotEmpty()) {
             true -> {

                 displayFragment(VegaGhanaInventoryDetailsFragment.newInstance(bundle), false)

             }
             else ->displayFragment(VegaGhanaInventoryListFragment.newInstance(), false)
         }*/

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorInventory, allowBackStack = flag)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaGhanaInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if (INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaGhanaInventoryDetailsFragment.newInstance(bundle), true)
        } else if (FILTER_LIST.equals(moveFrag)) {
            applyFilter(bundle)
        } else if (INVENTORY_LIST.equals(moveFrag)) {
            displayFragment(VegaGhanaInventoryListFragment.newInstance(), false)
        } else {
            displayFragment(VegaGhanaInventoryBalanceFragment.newInstance(), true)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorInventory)
        when (fragment) {
            is VegaGhanaInventoryDetailsFragment -> {
                fragment.applyFilter(bundle)
            }
        }
    }

}
