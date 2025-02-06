package com.olam.warehouse.vegax.offloadingghana.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.di.injectGhanaOffloadingFeature
import com.olam.warehouse.vegax.offloadingghana.ui.mtnr.VegaGhanaMtnrTypeSelectFragment
import com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighbridge.VegaGhanaMtnrWBListFragment
import com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighbridge.VegaGhanaMtnrWbConsignmentFragment
import com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighbridge.VegaGhanaMtnrWbWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighscale.VegaGhanaMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighscale.VegaGhanaMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingghana.ui.offline.VegaGhanaMTNROffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadingghana.ui.offline.VegaGhanaOfflineMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingghana.ui.offline.VegaGhanaOffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadingghana.ui.supplier.VegaGhanaOffloadingSupplierFragment
import com.olam.warehouse.vegax.offloadingghana.ui.supplier.VegaGhanaOffloadingSupplierSummaryFragment
import com.olam.warehouse.vegax.offloadingghana.ui.supplier.VegaGhanaOffloadingWeightEntryFragment
import com.olam.warehouse.vegax.offloadingghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaGhanaOffloadingActivity : HomeBaseActivity(), VegaGhanaOffloadingSupplierFragment.CallBack,
    VegaGhanaMtnrWBListFragment.OnWeighBridgeListener,
    VegaGhanaOffloadingWeightEntryFragment.CallBackAddBags, VegaGhanaOffloadingOfflineSummary.CallBack,
    VegaGhanaOffloadReplaceFragmentCallback, VegaGhanaMtnrWbConsignmentFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaGhanaMTNROffloadingOfflineSummary.CallBack,
    VegaCocoaAddPalletFragment.CallBackPallet {
    private val mTAG = VegaGhanaOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_ghana_offloading
    private val vm: VegaGhanaOffloadingViewModel by viewModel()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if(intent?.hasExtra(Constants.TRANSACTIONID) == true)
            displayFragment(VegaGhanaMtnrConsignmentFragment.newInstance(intent?.getStringExtra(Constants.TRANSACTIONID)?:""), false)
        else
            displayFragment(VegaGhanaOffloadTypeSelectFragment.newInstance(), true)
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
            MTNR -> displayFragment(VegaGhanaMtnrTypeSelectFragment.newInstance(), true)
            SUPPLIER -> displayFragment(
                VegaGhanaOffloadingSupplierFragment.newInstance(
                    VegaReceiving(),
                    ArrayList<VegaEcuadorOffloadingBagMaterial>()
                ), true
            )
            FRAG_ADD_BAG_WEIGHT_SUPPLIER -> displayFragment(VegaGhanaOffloadingWeightEntryFragment.newInstance(data as Bundle), true)

            ADD_WEIGHT -> displayFragment(
                VegaGhanaMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaGhanaMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaGhanaMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            MTNT_WEIGHSCALE -> displayFragment(
                VegaGhanaMtnrConsignmentFragment.newInstance(""),
                true
            )
            WEIGHBRIDGE_WEIHSCALE -> displayFragment(VegaGhanaMtnrWBListFragment.newInstance(), true)
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaGhanaMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }

            }        }
    }

    override fun replaceFragment(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails) {
        when (fragment) {
            MTNR_WEIGHSCALE_OFFLINE_SUMMARY -> displayFragment(
                VegaGhanaMtnrWbWeighScaleSummaryFragment.newInstance(flag,wbDetails), true)
        }
    }

    override fun navigateToSummary(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails) {
        when (fragment) {
            MTNR_WEIGHSCALE_OFFLINE_SUMMARY -> displayFragment(
                VegaGhanaMtnrWbWeighScaleSummaryFragment.newInstance(flag,wbDetails), true)
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            OFFLOADING_OFFLINE -> displayFragment(VegaGhanaOffloadingOfflineSummary.newInstance(), true)
            MTNR_OFFLOADING_OFFLINE -> displayFragment(VegaGhanaMTNROffloadingOfflineSummary.newInstance(), true)
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
                VegaGhanaOffloadingSupplierSummaryFragment.newInstance(
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
            is VegaGhanaOffloadingSupplierFragment -> {
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
            is VegaGhanaOffloadingWeightEntryFragment -> {
            }
            is VegaGhanaOffloadingSupplierFragment -> {
            }
            is VegaGhanaMtnrWeighScaleSummaryFragment -> {
            }
            is VegaGhanaMtnrTypeSelectFragment -> {
                when(fragment1){
                    is VegaGhanaMtnrConsignmentFragment -> {
                    }
                }
            }
            is VegaGhanaMtnrWBListFragment -> {
                when(fragment1){
                    is VegaGhanaMtnrWbConsignmentFragment -> {
                    }
                }
            }
            is VegaGhanaOffloadTypeSelectFragment -> {
                when (fragment1) {
                    is VegaGhanaMtnrTypeSelectFragment -> {
                    }
                    is VegaGhanaOffloadingSupplierFragment -> {
                    }
                    else -> finish()
                }
            }
            is VegaGhanaMtnrWeighScalePalletFragment -> {
            }
            is VegaGhanaMtnrConsignmentFragment -> {
                when (fragment1) {
                    is VegaGhanaMtnrWeighScaleSummaryFragment -> fragment.getBack()
                    is VegaGhanaMtnrConsignmentFragment -> finish()
                }
            }
            is VegaGhanaMtnrWbConsignmentFragment -> {
                when (fragment1) {
                    is VegaGhanaMtnrWbWeighScaleSummaryFragment -> fragment.getBack()
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
            is VegaGhanaMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun onWeighBridgeClick(wbDetails: VegaQualityWBDetails?) {
        displayFragment(
            VegaGhanaMtnrWbConsignmentFragment.newInstance(
                wbDetails
            ), true
        )
    }

    override fun setQualityWBList(it: List<VegaQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaCoffeeReceivingMtnrWithLots) {
        when (moveFrag) {
            OFFLOADING_OFFLINE_SUMMARY_FRAG -> displayFragment(
                VegaGhanaOfflineMtnrWeighScaleSummaryFragment.newInstance(
                    receivingData
                ), true
            )
        }

    }

}
