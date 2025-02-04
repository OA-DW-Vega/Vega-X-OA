package com.olam.warehouse.vegax.pilesesame.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.pilesesame.R
import com.olam.warehouse.vegax.pilesesame.di.injectSesameProcessingFeature
import com.olam.warehouse.vegax.pilesesame.utils.FRAG_FILTER
import com.olam.warehouse.vegax.pilesesame.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.pilesesame.utils.PILE_MANAGEMENT_SELECTION
import com.olam.warehouse.vegax.pilesesame.utils.PILE_MANAGEMENT_SUMMARY

class VegaSesamePileManagementActivity : HomeBaseActivity(),
    VegaSesamePileManagementFragment.CallBack,
    VegaSesamePileSelectionFragment.CallBack,
    VegaSesamePileManagementSummaryFragment.CallBack,
    VegaSesamePileFilterFragment.CallBack,
    VegaSesamePileManagementAddLotListener {

    private val mTAG = VegaSesamePileManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_sesame_pile_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectSesameProcessingFeature()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaSesamePileManagementFragment(), false)
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
        fragment: String, model: ArrayList<VegaCocoaDispatchLots>, materialArrayList: ArrayList<String>,
        fromVendorList: String
    ) {
        when (fragment) {

            PILE_MANAGEMENT_SELECTION ->

                displayFragment(
                    VegaSesamePileSelectionFragment.newInstance(
                        model,
                        materialArrayList,
                        fromVendorList
                    ), true
                )

            INVENTORY_FRAG -> displayFragment(
                VegaSesamePileManagementLotListFragment.newInstance(
                    model,
                    materialArrayList,
                    fromVendorList,
                    ""
                ), true
            )
        }
    }

    override fun replaceFragment(lots: ArrayList<VegaCocoaDispatchLots>) {
        TODO("Not yet implemented")
    }

    override fun replaceFragment(
        fragment: String,
        model: ArrayList<VegaCocoaDispatchLots>,
        materialArrayList: ArrayList<String>,
        fromVendorList: String,
        fromGradeList: String
    ) {
        when (fragment) {

            INVENTORY_FRAG -> displayFragment(
                VegaSesamePileManagementLotListFragment.newInstance(
                    model,
                    materialArrayList,
                    fromVendorList,
                    fromGradeList
                ), true
            )
        }

    }


    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flPpq)
        if (lotAddFragment is VegaSesamePileManagementFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(
        fragFilter: String,
        bundle: Bundle,
        fullFilter: ArrayList<String>
    ) {
        when (fragFilter) {
            FRAG_FILTER -> displayFragment(VegaSesamePileFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flPpq)
        when (fragment) {
            is VegaSesamePileSelectionFragment -> fragment.applyFilter(bundle)

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
                VegaSesamePileManagementSummaryFragment.newInstance(
                    materialArrayList,
                    alreadySelected,
                    pileSelectionList
                ), true
            )
        }
    }
}
