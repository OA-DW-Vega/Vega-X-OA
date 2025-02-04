package com.olam.warehouse.vegax.pileindiacoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.pileindiacoffee.R
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaCoffeeThirdPartyPileLotListModel
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaPileSelectionModel
import com.olam.warehouse.vegax.pileindiacoffee.ui.di.injectIndiaCoffeeProcessingFeature
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.FRAG_FILTER
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.PILE_MANAGEMENT_SELECTION
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.PILE_MANAGEMENT_SUMMARY

class VegaIndiaCoffeePileManagementActivity : HomeBaseActivity(),
    VegaIndiaCoffeePileManagementFragment.CallBack,
    VegaIndiaCoffeePileSelectionFragment.CallBack,
    VegaIndiaCoffeePileManagementSummaryFragment.CallBack,
    VegaIndiaCoffeePileFilterFragment.CallBack,
    VegaIndiaCoffeePileManagementAddLotListener {

    private val mTAG = VegaIndiaCoffeePileManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_india_coffee_pile_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectIndiaCoffeeProcessingFeature()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaIndiaCoffeePileManagementFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPpq, allowBackStack = flag)
    }


//    override fun replaceFragment(fragment: String, model: VegaCocoaDispatchLots, materialArrayList: ArrayList<String>) {
//        when (fragment) {
//            INVENTORY_FRAG -> displayFragment(
//                VegaCoffeePileManagementLotListFragment.newInstance(
//                    model,
//                    materialArrayList
//                ), true
//            )
//        }
//    }

    override fun replaceFragment(
        fragment: String, model: ArrayList<VegaCocoaDispatchLots>, materialArrayList: String,
        fromVendorList: String
    ) {
        when (fragment) {

            PILE_MANAGEMENT_SELECTION -> displayFragment(
                VegaIndiaCoffeePileSelectionFragment.newInstance(
                    model,
                    materialArrayList,
                    fromVendorList
                ), true
            )
        }
    }

    override fun replaceFragment(receivingType: String, data: Any) {

        when (receivingType) {
            INVENTORY_FRAG -> displayFragment(
                VegaIndiaCoffeePileManagementLotListFragment.newInstance(data as VegaCoffeeThirdPartyPileLotListModel),
                true
            )


        }
    }

    override fun replaceFragment(fragment: String, model: VegaCocoaDispatchLots, materialArrayList: ArrayList<String>) {
        TODO("Not yet implemented")
    }


    override fun replaceFragment(lots: ArrayList<VegaCocoaDispatchLots>) {
        TODO("Not yet implemented")
    }


    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flPpq)
        if (lotAddFragment is VegaIndiaCoffeePileManagementFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(fragFilter: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragFilter) {
            FRAG_FILTER -> displayFragment(VegaIndiaCoffeePileFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flPpq)
        when (fragment) {
            is VegaIndiaCoffeePileSelectionFragment -> fragment.applyFilter(bundle)

        }
    }

    override fun replaceFragment(
        fragment: String, materialArrayList: ArrayList<String>,
        alreadySelected: ArrayList<VegaCocoaDispatchLots>, pileSelectionList: VegaPileSelectionModel
    ) {
        when (fragment) {
            PILE_MANAGEMENT_SUMMARY -> displayFragment(
                VegaIndiaCoffeePileManagementSummaryFragment.newInstance(
                    materialArrayList,
                    alreadySelected,
                    pileSelectionList
                ), true
            )
        }
    }

    override fun editLot(paramsFrag: String, isEdit: Boolean, alreadySelected: ArrayList<VegaCocoaDispatchLots>) {
        displayFragment(VegaIndiaCoffeePileManagementFragment.newInstance(isEdit, alreadySelected), true)
    }
}
