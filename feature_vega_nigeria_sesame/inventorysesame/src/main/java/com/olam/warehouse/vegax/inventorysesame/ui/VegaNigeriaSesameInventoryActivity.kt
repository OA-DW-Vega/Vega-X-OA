package com.olam.warehouse.vegax.inventorysesame.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventorysesame.R
import com.olam.warehouse.vegax.inventorysesame.di.injectVegaNigeriaSesameInventoryFeature
import com.olam.warehouse.vegax.inventorysesame.utils.FILTER_LIST
import com.olam.warehouse.vegax.inventorysesame.utils.INVENTORY_DETAILS

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaNigeriaSesameInventoryActivity : HomeBaseActivity(), VegaNigeriaSesameInventoryFilterFragment.CallBack,
    VegaNigeriaSesameInventoryListFragment.CallBack,
    VegaNigeriaSesameInventoryDetailsFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_nigeria_sesame_inventory
    private val mTAG = VegaNigeriaSesameInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaNigeriaSesameInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)
        when (lotId?.isNotEmpty()) {
            true -> {
                val bundle = Bundle()
                bundle.putString("LOTID", lotId)
                displayFragment(VegaNigeriaSesameInventoryDetailsFragment.newInstance(bundle), false)

            }
            else ->displayFragment(VegaNigeriaSesameInventoryListFragment.newInstance(), false)
        }

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorInventory, allowBackStack = flag)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaNigeriaSesameInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if (INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaNigeriaSesameInventoryDetailsFragment.newInstance(bundle), true)
        } else if (FILTER_LIST.equals(moveFrag)) {
            applyFilter(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorInventory)
        when (fragment) {
            is VegaNigeriaSesameInventoryDetailsFragment -> {
                fragment.applyFilter(bundle)
            }
        }
    }

}
