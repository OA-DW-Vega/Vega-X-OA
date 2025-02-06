package com.olam.warehouse.vegax.inventoryindo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventoryindo.R
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.VegaIndoCoffeeInventoryLots
import com.olam.warehouse.vegax.inventoryindo.di.injectIndoCoffeeInventoryFeature

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeInventoryActivity : HomeBaseActivity(), VegaIndoCoffeeInventroyLotsFragment.CallBack,
    VegaIndoCoffeeInventoryFilterFragment.CallBack,
    VegaIndoCoffeeInventoryTypeFragment.CallBack {
    override val layoutResourceId = R.layout.activity_indo_coffee_inventory
    private val mTAG = VegaIndoCoffeeInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndoCoffeeInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)
//        displayFragment(VegaIndoCoffeeInventoryTypeFragment.newInstance(), false)
        displayFragment(VegaIndoCoffeeInventroyLotsFragment.newInstance(lotId, false), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flInventory,
            allowBackStack = flag
        )
    }

    override fun replaceQualityFragment(it: VegaIndoCoffeeInventoryLots) {
        displayFragment(VegaIndoCoffeeInventoryQualityFragment.newInstance(it), true)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaIndoCoffeeInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flInventory)
        when (fragment) {
            is VegaIndoCoffeeInventroyLotsFragment -> {
                fragment.applyFilter(bundle)
            }

        }
    }

    override fun replaceInventoryFragment(isThirdParty: Boolean) {
        displayFragment(VegaIndoCoffeeInventroyLotsFragment.newInstance(lotId, isThirdParty), true)
    }
}
