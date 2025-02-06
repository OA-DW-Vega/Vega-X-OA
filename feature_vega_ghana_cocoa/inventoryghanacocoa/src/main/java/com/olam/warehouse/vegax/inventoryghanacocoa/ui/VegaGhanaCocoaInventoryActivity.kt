package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventoryghanacocoa.R
import com.olam.warehouse.vegax.inventoryghanacocoa.di.injectVegaGhanaCocoaInventoryFeature
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.*
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.INVENTORY_DETAILS

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaGhanaCocoaInventoryActivity : HomeBaseActivity(), VegaGhanaCocoaInventoryFilterFragment.CallBack,
    VegaGhanaCocoaInventoryListFragment.CallBack,VegaGhanaCocoaInventoryTransitListFragment.CallBack,
    VegaGhanaCocoaInventoryDetailsFragment.CallBack, VegaGhanaCocoaReplaceFragmentCallback {
    override val layoutResourceId = R.layout.activity_vega_ghana_cocoa_inventory
    private val mTAG = VegaGhanaCocoaInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaGhanaCocoaInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGhanaCocoaMtntSelectInventoryFragment.newInstance(), false)
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            STORAGE -> callStorage()
            TRANSIT -> callTransit()
        }
    }

    override fun replaceFragment(receivingType: String) {
        TODO("Not yet implemented")
    }

    private fun callTransit(){
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)
        when (lotId?.isNotEmpty()) {
            true -> {
                val bundle = Bundle()
                bundle.putString("LOTID", lotId)
                displayFragment(VegaGhanaCocoaInventoryDetailsFragment.newInstance(bundle), false)

            }
           // else ->displayFragment(VegaGhanaCocoaInventoryTransitListFragment.newInstance(), false)
            else ->displayFragment(VegaGhanaCocoaTransitFragment.newInstance(), false)
        }
    }

    private fun callStorage(){
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)
        when (lotId?.isNotEmpty()) {
            true -> {
                val bundle = Bundle()
                bundle.putString("LOTID", lotId)
                displayFragment(VegaGhanaCocoaInventoryDetailsFragment.newInstance(bundle), false)

            }
            else ->displayFragment(VegaGhanaCocoaInventoryListFragment.newInstance(), false)
        }
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorInventory, allowBackStack = flag)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaGhanaCocoaInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if (INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaGhanaCocoaInventoryDetailsFragment.newInstance(bundle), true)
        } else if (TRANSIT_INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaGhanaCocoaInventoryTransitDetailsFragment.newInstance(bundle), true)
        } else if (FILTER_LIST.equals(moveFrag)) {
            applyFilter(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorInventory)
        when (fragment) {
            is VegaGhanaCocoaInventoryDetailsFragment -> {
                fragment.applyFilter(bundle)
            }
        }
    }

}
