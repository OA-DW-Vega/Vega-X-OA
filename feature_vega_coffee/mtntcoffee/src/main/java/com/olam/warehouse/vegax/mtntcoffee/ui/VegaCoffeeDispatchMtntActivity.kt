package com.olam.warehouse.vegax.mtntcoffee.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.mtntcoffee.R
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeLotListModel
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeWSBagModel
import com.olam.warehouse.vegax.mtntcoffee.di.injectCoffeeDispatchFeature
import com.olam.warehouse.vegax.mtntcoffee.ui.weighbridge.VegaCoffeeMtntSummaryFragment
import com.olam.warehouse.vegax.mtntcoffee.ui.weighbridge.VegaCoffeeMtntWeighBridgeAddLotFragment
import com.olam.warehouse.vegax.mtntcoffee.ui.weighbridge.VegaCoffeeMtntWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.mtntcoffee.ui.weighscale.VegaCoffeeMtntConsignmentFragment
import com.olam.warehouse.vegax.mtntcoffee.ui.weighscale.VegaCoffeeMtntWeighScaleAddLotFragment
import com.olam.warehouse.vegax.mtntcoffee.ui.weighscale.VegaCoffeeMtntWeighScalePalletFragment
import com.olam.warehouse.vegax.mtntcoffee.ui.weighscale.VegaCoffeeMtntWeighScaleSummaryFragment
import com.olam.warehouse.vegax.mtntcoffee.utils.*

class VegaCoffeeDispatchMtntActivity : HomeBaseActivity(), VegaCoffeeReplaceFragmentCallback, VegaCoffeeAddLotListener,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCocoaAddPalletFragment.CallBackPallet {
    private val mTAG = VegaCoffeeDispatchMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_coffee_mtnt_dispatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCoffeeMtntSelectDispatchTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainer,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHBRIDGE -> displayFragment(VegaCoffeeMtntWeighbridgeTruckListFragment.newInstance(), true)
            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaCoffeeMtntWeighBridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            LOT_LIST -> displayFragment(VegaCoffeeLotListFragment.newInstance(data as VegaCoffeeLotListModel), true)
            WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaCoffeeMtntSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            MTNT_WEIGHSCALE -> displayFragment(
                VegaCoffeeMtntConsignmentFragment.newInstance(),
                true
            )
            WEIGHSCALE_ADD_LOT -> displayFragment(
                VegaCoffeeMtntWeighScaleAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            ADD_WEIGHT -> displayFragment(
                VegaCoffeeMtntWeighScalePalletFragment.newInstance(data as VegaCocoaDispatchLots),
                true
            )
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCoffeeMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(data as VegaCoffeeWSBagModel)
                }
            }
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaCoffeeMtntWeighScaleSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
        }
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNavigation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backNavigation() {
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaCoffeeMtntWeighBridgeAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaCoffeeMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaCoffeeMtntWeighScaleSummaryFragment -> {
                if (fragment.isPostCreated)
                    fragment.backNav()
                else super.onBackPressed()
            }

            else -> super.onBackPressed()
        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {
            is VegaCoffeeMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaCoffeeMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoffeeMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoffeeMtntWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }
}
