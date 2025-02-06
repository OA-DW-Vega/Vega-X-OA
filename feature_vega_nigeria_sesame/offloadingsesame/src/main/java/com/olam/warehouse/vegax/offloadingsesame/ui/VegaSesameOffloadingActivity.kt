package com.olam.warehouse.vegax.offloadingsesame.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingsesame.R
import com.olam.warehouse.vegax.offloadingsesame.di.injectSesameOffloadingFeature
import com.olam.warehouse.vegax.offloadingsesame.ui.mtnr.VegaSesameMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingsesame.ui.mtnr.VegaSesameMtnrTypeSelectFragment
import com.olam.warehouse.vegax.offloadingsesame.ui.mtnr.VegaSesameMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingsesame.ui.offline.VegaSesameOffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadingsesame.ui.supplier.VegaSesameOffloadingSupplierFragment
import com.olam.warehouse.vegax.offloadingsesame.ui.supplier.VegaSesameOffloadingSupplierSummaryFragment
import com.olam.warehouse.vegax.offloadingsesame.ui.supplier.VegaSesameOffloadingWeightEntryFragment
import com.olam.warehouse.vegax.offloadingsesame.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaSesameOffloadingActivity : HomeBaseActivity(), VegaSesameOffloadingSupplierFragment.CallBack,
    VegaSesameOffloadingWeightEntryFragment.CallBackAddBags, VegaSesameOffloadingOfflineSummary.CallBack,VegaSesameOffloadReplaceFragmentCallback,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet {
    private val mTAG = VegaSesameOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_sesame_offloading
    private val vm: VegaSesameOffloadingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectSesameOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaSesameOffloadTypeSelectFragment.newInstance(), true)
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
            MTNR -> displayFragment(VegaSesameMtnrConsignmentFragment.newInstance(), true)
            SUPPLIER -> displayFragment(
                VegaSesameOffloadingSupplierFragment.newInstance(
                    VegaReceiving(),
                    ArrayList<VegaEcuadorOffloadingBagMaterial>()
                ), true
            )
            FRAG_ADD_BAG_WEIGHT_SUPPLIER -> displayFragment(VegaSesameOffloadingWeightEntryFragment.newInstance(data as Bundle), true)

            ADD_WEIGHT -> displayFragment(
                VegaSesameMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaSesameMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaSesameMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaSesameMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }

            }        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            OFFLOADING_OFFLINE -> displayFragment(
                VegaSesameOffloadingOfflineSummary.newInstance(),
                true
            )

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
                VegaSesameOffloadingSupplierSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>,
                    bagList
                ), true
            )
        }
    }

    override fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaSesameOffloadingSupplierFragment -> {
                fragment.updateBagWeight(bagMaterial)
            }
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
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainer)
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaSesameOffloadingWeightEntryFragment -> {
                /*Nothing to implement*/
            }
            is VegaSesameOffloadingSupplierFragment -> {
                /*Nothing to implement*/
            }
            is VegaSesameMtnrWeighScaleSummaryFragment -> {
                /*Nothing to implement*/
            }
            is VegaSesameOffloadTypeSelectFragment -> {
                when (fragment1) {
                    is VegaSesameMtnrTypeSelectFragment -> {
                        /*Nothing to implement*/
                    }
                    is VegaSesameOffloadingSupplierFragment -> {
                        /*Nothing to implement*/
                    }
                    else -> finish()
                }
            }
            is VegaSesameMtnrWeighScalePalletFragment -> {
                /*Nothing to implement*/
            }
            is VegaSesameMtnrConsignmentFragment -> {
                when (fragment1) {
                    is VegaSesameMtnrWeighScaleSummaryFragment -> fragment.getBack()
                }
            }
            else -> super.onBackPressed()
        }
    }

    /*override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
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
    }*/

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaSesameMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }


    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaSesameMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }
}
