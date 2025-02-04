package com.olam.warehouse.vegax.inventorycocoa.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventorycocoa.R
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLots
import com.olam.warehouse.vegax.inventorycocoa.di.injectVegaCocoaInventoryFeature

/**
 * Created by Baskaran Kannan on 5/18/2020.
 */
class VegaCocoaInventoryActivity : HomeBaseActivity(), VegaCocoaInventroyLotsFragment.CallBack,
    VegaCocoaInventoryFilterFragment.CallBack,
    VegaCoCoaInventoryTypeFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_cocoa_inventory
    private val mTAG = VegaCocoaInventoryActivity::class.java.canonicalName
    private var lotId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaCocoaInventoryFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(SCANNED_ID)) lotId = intent.getStringExtra(SCANNED_ID)
        displayFragment(VegaCoCoaInventoryTypeFragment.newInstance(), false)
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

    override fun replaceQualityFragment(it: VegaCocoaInventoryLots) {
        displayFragment(VegaCocoaInventoryQualityFragment.newInstance(it), true)
    }

    override fun replaceFilterFragment(
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        displayFragment(VegaCocoaInventoryFilterFragment.newInstance(bundle, fullFilter), true)
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flInventory)
        when (fragment) {
            is VegaCocoaInventroyLotsFragment -> {
                fragment.applyFilter(bundle)
            }

        }
    }

    override fun replaceInventoryFragment(isThirdParty: Boolean) {
        displayFragment(VegaCocoaInventroyLotsFragment.newInstance(lotId, isThirdParty), true)
    }
}
