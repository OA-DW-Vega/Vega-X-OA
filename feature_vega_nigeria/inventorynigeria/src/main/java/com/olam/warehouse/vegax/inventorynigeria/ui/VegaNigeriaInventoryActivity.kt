package com.olam.warehouse.vegax.inventorynigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventorynigeria.R
import com.olam.warehouse.vegax.inventorynigeria.di.injectVegaNigeriaInventoryFeature
import com.olam.warehouse.vegax.inventorynigeria.utils.FILTER_LIST
import com.olam.warehouse.vegax.inventorynigeria.utils.INVENTORY_DETAILS

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
class VegaNigeriaInventoryActivity : HomeBaseActivity(),
    VegaNigeriaInventoryFilterFragment.CallBack,
    VegaNigeriaCocoaInventoryListFragment.CallBack,
    VegaNigeriaCocoaInventoryDetailsFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_nigeria_inventory
    private val mTAG = VegaNigeriaInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaNigeriaInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(SCANNED_ID)) lotId = intent.getStringExtra(SCANNED_ID)
        when (lotId?.isNotEmpty()) {
            true -> {
                val bundle = Bundle()
                bundle.putString("LOTID", lotId)
                displayFragment(VegaNigeriaCocoaInventoryDetailsFragment.newInstance(bundle), false)

            }
            else -> displayFragment(VegaNigeriaCocoaInventoryListFragment.newInstance(), false)
        }
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

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        if (INVENTORY_DETAILS.equals(moveFrag)) {
            displayFragment(VegaNigeriaCocoaInventoryDetailsFragment.newInstance(bundle), true)
        } else if (FILTER_LIST.equals(moveFrag)) {
            applyFilter(bundle)
        }
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaNigeriaInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flInventory)
        when (fragment) {
            is VegaNigeriaCocoaInventoryDetailsFragment -> {
                fragment.applyFilter(bundle)
            }
        }
    }
}
