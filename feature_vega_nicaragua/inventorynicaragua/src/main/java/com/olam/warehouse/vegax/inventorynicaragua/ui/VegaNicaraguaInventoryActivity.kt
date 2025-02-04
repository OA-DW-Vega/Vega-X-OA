package com.olam.warehouse.vegax.inventorynicaragua.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.inventorynicaragua.R
import com.olam.warehouse.vegax.inventorynicaragua.di.injectNicInventoryFeature
import com.olam.warehouse.vegax.inventorynicaragua.ui.details.VegaNicInventoryDetailsFragment
import com.olam.warehouse.vegax.inventorynicaragua.ui.filter.VegaNicInventoryFilterFragment
import com.olam.warehouse.vegax.inventorynicaragua.ui.quality.VegaNicInventoryQualityFragment

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */
class VegaNicaraguaInventoryActivity : HomeBaseActivity(),
        VegaNicInventoryStorageListFragment.CallBack,
        VegaNicInventoryFilterFragment.CallBack,
        VegaNicInventoryDetailsFragment.CallBack {

    private val mTAG = VegaNicaraguaInventoryActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_nic_inventory
    private var lotId: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNicInventoryFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        if (intent.hasExtra(Constants.SCANNED_ID)) lotId = intent.getStringExtra(Constants.SCANNED_ID)
        when (lotId?.isNotEmpty()) {
            true -> {
                val bundle = Bundle()
                bundle.putString("LOTID", lotId)
                displayFragment(VegaNicInventoryDetailsFragment(), false, bundle)
            }
            else -> displayFragment(VegaNicInventoryStorageListFragment(), false, Bundle())
        }

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean, bundle: Bundle) {
        fragment.arguments = bundle
        replaceFragment(fragment, mTAG, true, R.id.flInventory, allowBackStack = flag)
    }

    override fun replaceFragment(fragment: String, data: Any) {
        when (fragment) {
            "details" -> displayFragment(VegaNicInventoryDetailsFragment(), true, data as Bundle)
            "Filter" -> displayFragment(VegaNicInventoryFilterFragment(), true, data as Bundle)
            "Quality_Params" -> displayFragment(VegaNicInventoryQualityFragment(), true, data as Bundle)
        }
    }

    override fun applyFilters(fragment: String, data: Any) {
        val filterItemBundle = data as Bundle
        val filterItems = filterItemBundle.getStringArrayList("Full_Filter")
        val fragment = supportFragmentManager.findFragmentById(R.id.flInventory)
        when (fragment) {
            is VegaNicInventoryStorageListFragment -> {
                filterItems?.let { fragment.applyFilterValues(it) }
            }
            is VegaNicInventoryDetailsFragment -> {
                fragment.applyFilterValues(data)
            }
        }
    }

    /* override fun replaceFragment(moveFrag: String, bundle: Bundle) {
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
     }*/
}
