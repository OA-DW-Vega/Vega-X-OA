package com.olam.warehouse.vegax.mtntsesame.ui

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
import com.olam.warehouse.vegax.mtntsesame.R
import com.olam.warehouse.vegax.mtntsesame.data.domain.model.VegaNigeriaSesameMtntLotListModel
import com.olam.warehouse.vegax.mtntsesame.di.injectNigeriaSesameMtntDispatchFeature
import com.olam.warehouse.vegax.mtntsesame.ui.weighbridge.VegaNigeriaSesameMtntSummaryFragment
import com.olam.warehouse.vegax.mtntsesame.ui.weighbridge.VegaNigeriaSesameMtntWeighBridgeAddLotFragment
import com.olam.warehouse.vegax.mtntsesame.ui.weighbridge.VegaNigeriaSesameMtntWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.mtntsesame.ui.weighscale.VegaNigeriaSesameMtntConsignmentFragment
import com.olam.warehouse.vegax.mtntsesame.ui.weighscale.VegaNigeriaSesameMtntWeighScaleAddLotFragment
import com.olam.warehouse.vegax.mtntsesame.ui.weighscale.VegaNigeriaSesameMtntWeighScalePalletFragment
import com.olam.warehouse.vegax.mtntsesame.ui.weighscale.VegaNigeriaSesameMtntWeighScaleSummaryFragment
import com.olam.warehouse.vegax.mtntsesame.utils.*

class VegaNigeriaSesameDispatchMtntActivity : HomeBaseActivity(), VegaNigeriaSesameReplaceFragmentCallback, VegaNigeriaSesameAddLotListener,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCocoaAddPalletFragment.CallBackPallet {
    private val mTAG = VegaNigeriaSesameDispatchMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_nigeria_sesame_mtnt_dispatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaSesameMtntDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaSesameMtntConsignmentFragment.newInstance(), false)
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
            WEIGHBRIDGE -> displayFragment(VegaNigeriaSesameMtntWeighbridgeTruckListFragment.newInstance(), true)
            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaNigeriaSesameMtntWeighBridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            LOT_LIST -> displayFragment(VegaNigeriaSesameLotListFragment.newInstance(data as VegaNigeriaSesameMtntLotListModel), true)
            WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaNigeriaSesameMtntSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            MTNT_WEIGHSCALE -> displayFragment(
                VegaNigeriaSesameMtntConsignmentFragment.newInstance(),
                true
            )
            WEIGHSCALE_ADD_LOT -> displayFragment(
                VegaNigeriaSesameMtntWeighScaleAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            ADD_WEIGHT -> displayFragment(
                VegaNigeriaSesameMtntWeighScalePalletFragment.newInstance(data as VegaCocoaDispatchLots),
                true
            )
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaNigeriaSesameMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(data as String)
                }
            }
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaNigeriaSesameMtntWeighScaleSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
        }
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
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
            is VegaNigeriaSesameMtntWeighBridgeAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaNigeriaSesameMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaNigeriaSesameMtntWeighScaleSummaryFragment -> {
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
            is VegaNigeriaSesameMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaNigeriaSesameMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaNigeriaSesameMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaNigeriaSesameMtntWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }
}
