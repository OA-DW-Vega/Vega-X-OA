package com.olam.warehouse.vegax.mtntghanacocoa.ui

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
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntLotListModel
import com.olam.warehouse.vegax.mtntghanacocoa.di.injectGhanaMtntDispatchFeature
import com.olam.warehouse.vegax.mtntghanacocoa.ui.offline.VegaGhanaCocoaMtntOfflineSummaryFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.offline.VegaGhanaMtntOfflineSummaryFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.weighbridge.VegaGhanaCocoaCocoaMtntSummaryFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.weighbridge.VegaGhanaCocoaCocoaMtntWeighBridgeAddLotFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.weighbridge.VegaGhanaCocoaPortMtntConsignmentFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.weighscale.VegaGhanaCocoaCocoaMtntConsignmentFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.weighscale.VegaGhanaCocoaCocoaMtntWeighScaleSummaryFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.weighscale.VegaGhanaCocoaMtntWeighScaleAddLotFragment
import com.olam.warehouse.vegax.mtntghanacocoa.ui.weighscale.VegaGhanaCocoaMtntWeighScalePalletFragment
import com.olam.warehouse.vegax.mtntghanacocoa.utils.*

class VegaGhanaCocoaCocoaDispatchMtntActivity : HomeBaseActivity(), VegaGhanaCocoaReplaceFragmentCallback, VegaGhanaCocoaAddLotListener,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaGhanaCocoaMtntOfflineSummaryFragment.CallBack {
    private val mTAG = VegaGhanaCocoaCocoaDispatchMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_ghana_cocoa_mtnt_dispatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaMtntDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaGhanaCocoaMtntSelectDispatchTypeFragment.newInstance(), false)
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
            WEIGHBRIDGE -> displayFragment(
                VegaGhanaCocoaPortMtntConsignmentFragment.newInstance(),
                true
            )
            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaGhanaCocoaCocoaMtntWeighBridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            //LOT_LIST -> displayFragment(VegaGhanaCocoaLotListFragment.newInstance(data as VegaGhanaMtntLotListModel), true)
            WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaGhanaCocoaCocoaMtntSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            MTNT_WEIGHSCALE -> displayFragment(
                VegaGhanaCocoaCocoaMtntConsignmentFragment.newInstance(),
                true
            )
            WEIGHSCALE_ADD_LOT -> displayFragment(
                VegaGhanaCocoaMtntWeighScaleAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            ADD_WEIGHT -> displayFragment(
                VegaGhanaCocoaMtntWeighScalePalletFragment.newInstance(data as VegaGhanaCocoaDispatchLots),
                true
            )
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaGhanaCocoaMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(data as String)
                }
            }
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaGhanaCocoaCocoaMtntWeighScaleSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            DISPATCH_OFFLINE_SUMMARY -> displayFragment(
                VegaGhanaCocoaMtntOfflineSummaryFragment.newInstance(),
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
            is VegaGhanaCocoaCocoaMtntWeighBridgeAddLotFragment -> {
                super.onBackPressed()
                fragment.saveLotDetails()

            }
            is VegaGhanaCocoaMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaGhanaCocoaCocoaMtntWeighScaleSummaryFragment -> {
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
            is VegaGhanaCocoaCocoaMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaGhanaCocoaMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
//            is VegaGhanaCocoaPortMtntConsignmentFragment -> {
//                lotAddFragment.updateLotList(lots)
//            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaMtntWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun replaceFragment(moveFrag: String, dispatchData: VegaCocoaDispatchWB, data: Any) {
        when (moveFrag) {
            ADD_LOT -> displayFragment(VegaGhanaMtntOfflineSummaryFragment.newInstance(dispatchData), true)
        }
    }

    override fun replaceFragment(receivingType: String, data: Any, list: VegaCocoaDispatchWB) {
        when (receivingType) {
            //LOT_LIST -> displayFragment(VegaGhanaCocoaLotListFragment.newInstance(data as VegaGhanaMtntLotListModel,list), true)
            LOT_LIST -> displayFragment(
                VegaGhanaCocoaLotListFragment.newInstance(
                    data as VegaGhanaMtntLotListModel,
                    list
                ), true
            )
        }
    }
}
