package com.olam.warehouse.vegax.pileecuador.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.pileecuador.R
import com.olam.warehouse.vegax.pileecuador.ui.data.domain.model.VegaEcuadorThirdPartyPileLotListModel
import com.olam.warehouse.vegax.pileecuador.ui.data.domain.model.VegaPileSelectionModel
import com.olam.warehouse.vegax.pileecuador.ui.di.injectIndiaCoffeeProcessingFeature
import com.olam.warehouse.vegax.pileecuador.ui.utils.FRAG_FILTER
import com.olam.warehouse.vegax.pileecuador.ui.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.pileecuador.ui.utils.PILE_MANAGEMENT_SELECTION
import com.olam.warehouse.vegax.pileecuador.ui.utils.PILE_MANAGEMENT_SUMMARY

class VegaEcuadorPileManagementActivity : HomeBaseActivity(),
    VegaEcuadorPileManagementFragment.CallBack,
    VegaEcuadorPileSelectionFragment.CallBack,
    VegaEcuadorPileManagementSummaryFragment.CallBack,
    VegaEcuadorPileFilterFragment.CallBack,
    VegaEcuadorPileManagementAddLotListener {

    private val mTAG = VegaEcuadorPileManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_ecuador_pile_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectIndiaCoffeeProcessingFeature()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaEcuadorPileManagementFragment(), false)
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
                VegaEcuadorPileSelectionFragment.newInstance(
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
                VegaEcuadorPileManagementLotListFragment.newInstance(data as VegaEcuadorThirdPartyPileLotListModel),
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
        if (lotAddFragment is VegaEcuadorPileManagementFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(fragFilter: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragFilter) {
            FRAG_FILTER -> displayFragment(VegaEcuadorPileFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flPpq)
        when (fragment) {
            is VegaEcuadorPileSelectionFragment -> fragment.applyFilter(bundle)

        }
    }

    override fun replaceFragment(
        fragment: String, materialArrayList: ArrayList<String>,
        alreadySelected: ArrayList<VegaCocoaDispatchLots>, pileSelectionList: VegaPileSelectionModel
    ) {
        when (fragment) {
            PILE_MANAGEMENT_SUMMARY -> displayFragment(
                VegaEcuadorPileManagementSummaryFragment.newInstance(
                    materialArrayList,
                    alreadySelected,
                    pileSelectionList
                ), true
            )
        }
    }

    override fun editLot(paramsFrag: String, isEdit: Boolean, alreadySelected: ArrayList<VegaCocoaDispatchLots>) {
        displayFragment(VegaEcuadorPileManagementFragment.newInstance(isEdit, alreadySelected), true)
    }
}
