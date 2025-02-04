package com.olam.warehouse.vegax.pilemanagementnigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.pilemanagementnigeria.R
import com.olam.warehouse.vegax.pilemanagementnigeria.di.injectNigeriaProcessingFeature
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.FRAG_FILTER
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.PILE_MANAGEMENT_SELECTION
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.PILE_MANAGEMENT_SUMMARY

class VegaNigeriaPileManagementActivity : HomeBaseActivity(),
    VegaNigeriaPileManagementFragment.CallBack,
    VegaNigeriaPileSelectionFragment.CallBack,
    VegaNigeriaPileManagementSummaryFragment.CallBack,
    VegaNigeriaPileFilterFragment.CallBack,
    VegaNigeriaPileManagementAddLotListener {

    private val mTAG = VegaNigeriaPileManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_nigeria_pile_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectNigeriaProcessingFeature()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaNigeriaPileManagementFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPpq, allowBackStack = flag)
    }


    override fun replaceFragment(
        fragment: String,
        model: VegaCocoaDispatchLots,
        materialArrayList: ArrayList<String>,
        fromVendorList: String
    ) {
        when (fragment) {
//            INVENTORY_FRAG -> displayFragment(
//                VegaSesamePileManagementLotListFragment.newInstance(
//                    model,
//                    materialArrayList,
//                    fromVendorList
//                ), true
//            )
        }
    }

    override fun replaceFragment(
        fragment: String,
        model: ArrayList<VegaCocoaDispatchLots>,
        materialArrayList: ArrayList<String>,
        fromVendorList: String
    ) {
        when (fragment) {

            PILE_MANAGEMENT_SELECTION -> displayFragment(
                VegaNigeriaPileSelectionFragment.newInstance(
                    model,
                    materialArrayList,
                    fromVendorList
                ), true
            )

            INVENTORY_FRAG -> displayFragment(
                VegaNigeriaPileManagementLotListFragment.newInstance(
                    model,
                    materialArrayList,
                    fromVendorList
                ), true
            )
        }
    }

    override fun replaceFragment(lots: ArrayList<VegaCocoaDispatchLots>) {
        TODO("Not yet implemented")
    }


    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flPpq)
        if (lotAddFragment is VegaNigeriaPileManagementFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(
        fragFilter: String,
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        when (fragFilter) {
            FRAG_FILTER -> displayFragment(
                VegaNigeriaPileFilterFragment.newInstance(
                    bundle,
                    fullFilter
                ), true
            )
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flPpq)
        when (fragment) {
            is VegaNigeriaPileSelectionFragment -> fragment.applyFilter(bundle)

        }
    }

//    override fun replaceFragment(Summary:String, bundle: Bundle) {
//        when (Summary) {
//            PILE_MANAGEMENT_SUMMARY-> VegaCoffeePileManagementSummaryFragment.newInstance(bundle)
//
//        }
//    }

    override fun replaceFragment(
        fragment: String, materialArrayList: ArrayList<String>,
        alreadySelected: ArrayList<VegaCocoaDispatchLots>, pileSelectionList: VegaCocoaDispatchLots
    ) {
        when (fragment) {
            PILE_MANAGEMENT_SUMMARY -> displayFragment(
                VegaNigeriaPileManagementSummaryFragment.newInstance(
                    materialArrayList,
                    alreadySelected,
                    pileSelectionList
                ), true
            )
        }
    }
}
