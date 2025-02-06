package com.olam.warehouse.vegax.mtntghana.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntLotListModel
import com.olam.warehouse.vegax.mtntghana.di.injectGhanaMtntDispatchFeature
import com.olam.warehouse.vegax.mtntghana.ui.offline.VegaGhanaCashewMtntOfflineSummaryFragment
import com.olam.warehouse.vegax.mtntghana.ui.offline.VegaGhanaMtntOfflineSummaryFragment
import com.olam.warehouse.vegax.mtntghana.ui.weighbridge.VegaGhanaMtntSummaryFragment
import com.olam.warehouse.vegax.mtntghana.ui.weighbridge.VegaGhanaMtntWeighBridgeAddLotFragment
import com.olam.warehouse.vegax.mtntghana.ui.weighbridge.VegaGhanaMtntWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.mtntghana.ui.weighscale.VegaGhanaMtntConsignmentFragment
import com.olam.warehouse.vegax.mtntghana.ui.weighscale.VegaGhanaMtntWeighScaleAddLotFragment
import com.olam.warehouse.vegax.mtntghana.ui.weighscale.VegaGhanaMtntWeighScalePalletFragment
import com.olam.warehouse.vegax.mtntghana.ui.weighscale.VegaGhanaMtntWeighScaleSummaryFragment
import com.olam.warehouse.vegax.mtntghana.utils.*

class VegaGhanaDispatchMtntActivity : HomeBaseActivity(), VegaGhanaReplaceFragmentCallback, VegaGhanaAddLotListener,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaGhanaCashewMtntOfflineSummaryFragment.CallBack {
    private val mTAG = VegaGhanaDispatchMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_ghana_mtnt_dispatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaMtntDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGhanaMtntConsignmentFragment.newInstance(), false)
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
            WEIGHBRIDGE -> displayFragment(VegaGhanaMtntWeighbridgeTruckListFragment.newInstance(), true)
            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaGhanaMtntWeighBridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            LOT_LIST -> displayFragment(VegaGhanaLotListFragment.newInstance(data as VegaGhanaMtntLotListModel), true)
            WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaGhanaMtntSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            MTNT_WEIGHSCALE -> displayFragment(
                VegaGhanaMtntConsignmentFragment.newInstance(),
                true
            )
            WEIGHSCALE_ADD_LOT -> displayFragment(
                VegaGhanaMtntWeighScaleAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            ADD_WEIGHT -> displayFragment(
                VegaGhanaMtntWeighScalePalletFragment.newInstance(data as VegaGhanaCocoaDispatchLots),
                true
            )
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaGhanaMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(data as String)
                }
            }
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaGhanaMtntWeighScaleSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            DISPATCH_OFFLINE_SUMMARY -> displayFragment(
                VegaGhanaCashewMtntOfflineSummaryFragment.newInstance(),
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
            is VegaGhanaMtntWeighBridgeAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaGhanaMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaGhanaMtntWeighScaleSummaryFragment -> {
                if (fragment.isPostCreated)
                    fragment.backNav()
                else super.onBackPressed()
            }

            else -> super.onBackPressed()
        }
    }

    override fun addedLots(lots: ArrayList<VegaGhanaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {
            is VegaGhanaMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaGhanaMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaMtntWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun replaceFragment(moveFrag: String, dispatchData: VegaCocoaDispatchWB, data: Any) {
        when (moveFrag) {
            ADD_LOT -> displayFragment(VegaGhanaMtntOfflineSummaryFragment.newInstance(dispatchData), true)
        }
    }
}
