package com.olam.warehouse.vegax.offloadingnigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.di.injectNigeriaOffloadingFeature
import com.olam.warehouse.vegax.offloadingnigeria.ui.mtnr.VegaNigeriaMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingnigeria.ui.mtnr.VegaNigeriaMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingnigeria.ui.offline.VegaNigeriaOffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadingnigeria.ui.supplier.VegaNigeriaOffloadingSupplierFragment
import com.olam.warehouse.vegax.offloadingnigeria.ui.supplier.VegaNigeriaOffloadingSupplierSummaryFragment
import com.olam.warehouse.vegax.offloadingnigeria.ui.supplier.VegaNigeriaOffloadingWaitingTruckListFragment
import com.olam.warehouse.vegax.offloadingnigeria.ui.supplier.VegaNigeriaOffloadingWeightEntryFragment
import com.olam.warehouse.vegax.offloadingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaNigeriaOffloadingActivity : HomeBaseActivity(),
    VegaNigeriaOffloadingSupplierFragment.CallBack,
    VegaNigeriaOffloadingWeightEntryFragment.CallBackAddBags,
    VegaNigeriaOffloadingOfflineSummary.CallBack,
    VegaNigeriaOffloadingWaitingTruckListFragment.CallBack,
    VegaNigeriaOffloadReplaceFragmentCallback, VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet {
    private val mTAG = VegaNigeriaOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_nigeria_offloading
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()
    private var gateEntryData = VegaOffloadingTrucks()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        //vm.clearBagDetails()
//        displayFragment(
//            VegaNigeriaOffloadingSupplierFragment.newInstance(
//                VegaReceiving(),
//                ArrayList<VegaEcuadorOffloadingBagMaterial>()
//            ), false
//        )
        displayFragment(VegaNigeriaOffloadTypeSelectFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flEcuadorOffloading,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            MTNR -> displayFragment(VegaNigeriaMtnrConsignmentFragment.newInstance(), true)
            SUPPLIER -> {
                gateEntryData = VegaOffloadingTrucks()
                gateEntryData.weighBridgeType = PROCURE
                displayFragment(
                    VegaNigeriaOffloadingWaitingTruckListFragment.newInstance(
                        gateEntryData
                    ), true
                )
            }
//            PARAMS_LIST_FRAG -> displayFragment(VegaNigeriaOffloadingSupplierFragment.newInstance(data as Bundle), false)
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaNigeriaOffloadingWeightEntryFragment.newInstance(
                    data as Bundle
                ), true
            )
            ADD_WEIGHT -> displayFragment(
                VegaNigeriaMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaNigeriaMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT_NEW -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaNigeriaMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaNigeriaMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }

            }
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            OFFLOADING_OFFLINE -> displayFragment(VegaNigeriaOffloadingOfflineSummary.newInstance(), true)
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>,
        bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
    ) {
        when (moveFrag) {
            OFFLOADING_SUMMARY_FRAG -> displayFragment(
                VegaNigeriaOffloadingSupplierSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>,
                    bagList
                ), true
            )
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaNigeriaMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaNigeriaOffloadingSupplierFragment -> {
                fragment.updateBagWeight(bagMaterial)
            }
        }
    }

    override fun replaceFragment(paramsListFrag: String, item: VegaOffloadingTrucks) {
        when (paramsListFrag) {
            PARAMS_LIST_FRAG -> displayFragment(
                VegaNigeriaOffloadingSupplierFragment.newInstance(
                    item
                ), false
            )
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaNigeriaMtnrWeighScalePalletFragment -> fragment.updateBagWeight(
                prepareItem(
                    bagMaterial
                )
            )
        }
    }


}
