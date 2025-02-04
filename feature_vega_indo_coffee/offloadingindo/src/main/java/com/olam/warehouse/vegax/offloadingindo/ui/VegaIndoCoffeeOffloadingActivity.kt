package com.olam.warehouse.vegax.offloadingindo.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingindo.R
import com.olam.warehouse.vegax.offloadingindo.di.injectVegaIndoCoffeeOffloadingFeature
import com.olam.warehouse.vegax.offloadingindo.ui.callback.VegaIndoCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingindo.ui.mtnr.VegaIndoCoffeeMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingindo.ui.mtnr.VegaIndoCoffeeMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingindo.ui.supplier.VegaIndoCoffeeSupplierConsignmentFragment
import com.olam.warehouse.vegax.offloadingindo.ui.transaction.VegaIndoCoffeeTransactionDetailsFragment
import com.olam.warehouse.vegax.offloadingindo.utils.*

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeOffloadingActivity : HomeBaseActivity(), VegaIndoCoffeeOffloadReplaceFragmentCallback,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaIndoCoffeeSupplierConsignmentFragment.Callback,
    VegaCocoaAddPalletFragment.CallBackPallet, VegaIndoCoffeeTransactionDetailsFragment.CallBack {
    private val mTAG = VegaIndoCoffeeOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_indo_coffee_offloading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaIndoCoffeeOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(UIUtils.TRANS_OFFLOADING))
            displayFragment(VegaIndoCoffeeTransactionDetailsFragment(), false)
        else
            displayFragment(VegaIndoCoffeeOffloadTypeSelectFragment.newInstance(), false)
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
            /*WEIGHSCALE_LIST -> displayFragment(
                VegaCoffeeOffloadWeighscaleListFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )*/
            MTNR -> displayFragment(
                VegaIndoCoffeeMtnrConsignmentFragment.newInstance(
                    VegaCoffeeReceiving(
                        weighBridgeType = data as String
                    )
                ), true
            )
            SUPPLIER -> displayFragment(
                VegaIndoCoffeeSupplierConsignmentFragment.newInstance(
                    VegaCoffeeReceiving(
                        weighBridgeType = data as String
                    )
                ), true
            )
            WEIGHSCALE -> displayFragment(
                VegaIndoCoffeeMtnrConsignmentFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            WEIGHSCALE_SUPPLIER -> displayFragment(
                VegaIndoCoffeeSupplierConsignmentFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )

            ADD_WEIGHT -> displayFragment(
                VegaIndoCoffeeMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaIndoCoffeeMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                    //is VegaCoffeeSupplierConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT -> {
                val bundle = data as Bundle
                val isLotHide = bundle.getBoolean("isLotHide", false)
                displayFragment(
                    VegaSweepingWeightEntryFragment.newInstance(data, false, isLotHide),
                    true
                )
            }
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaIndoCoffeeMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaIndoCoffeeMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                    is VegaIndoCoffeeSupplierConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }
            }
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
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainer)
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaIndoCoffeeMtnrWeighScaleSummaryFragment -> {
            }
            is VegaIndoCoffeeTransactionDetailsFragment -> {
                when (fragment1) {
                    is VegaIndoCoffeeTransactionDetailsFragment -> super.onBackPressed()
                }
            }
            is VegaIndoCoffeeSupplierConsignmentFragment -> {

            }
            is VegaIndoCoffeeOffloadTypeSelectFragment -> {
                when (fragment1) {
                    is VegaIndoCoffeeOffloadTypeSelectFragment -> super.onBackPressed()
                }

            }
            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> {
            }
            is VegaIndoCoffeeMtnrConsignmentFragment -> {
                when (fragment1) {
                    is VegaIndoCoffeeMtnrWeighScaleSummaryFragment -> fragment.getBack()
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
            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun callBack(type: String, receiveLots: VegaCoffeeReceiveLots, receiving: VegaCoffeeReceiving) {
        when (type) {
            ADD_WEIGHT -> displayFragment(
                VegaIndoCoffeeMtnrWeighScalePalletFragment.newInstance(receiveLots, receiving),
                true
            )
        }
    }

    override fun replaceFragment(receivingType: String, data: Any, vegaCoffeeReceiving: Any) {
        when (receivingType) {
            MTNR -> displayFragment(
                VegaIndoCoffeeMtnrConsignmentFragment.newInstance(vegaCoffeeReceiving as VegaCoffeeReceiving), true
            )
            SUPPLIER -> displayFragment(
                VegaIndoCoffeeSupplierConsignmentFragment.newInstance(vegaCoffeeReceiving as VegaCoffeeReceiving), true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaIndoCoffeeMtnrWeighScaleSummaryFragment.newInstance(vegaCoffeeReceiving as VegaCoffeeReceiving),
                true
            )
        }
    }
}
