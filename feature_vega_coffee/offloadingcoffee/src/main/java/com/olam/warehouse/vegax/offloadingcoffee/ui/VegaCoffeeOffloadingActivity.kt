package com.olam.warehouse.vegax.offloadingcoffee.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingcoffee.R
import com.olam.warehouse.vegax.offloadingcoffee.di.injectVegaCoffeeOffloadingFeature
import com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr.VegaCoffeeMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr.VegaCoffeeMtnrTypeSelectFragment
import com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr.VegaCoffeeMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr.weighscale.VegaCoffeeOffloadWeighscaleListFragment
import com.olam.warehouse.vegax.offloadingcoffee.ui.supplier.VegaCoffeeSupplierConsignmentFragment
import com.olam.warehouse.vegax.offloadingcoffee.utils.*

class VegaCoffeeOffloadingActivity : HomeBaseActivity(), VegaCoffeeOffloadReplaceFragmentCallback,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCoffeeSupplierConsignmentFragment.Callback,
    VegaCocoaAddPalletFragment.CallBackPallet {
    private val mTAG = VegaCoffeeOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_coffee_offloading
    private var currentKey = getCurrentKey()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaCoffeeOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {

        if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF")) {
            //directly call the mtnrTypeSelect fragment
            displayFragment(VegaCoffeeMtnrTypeSelectFragment.newInstance(STO), true)
        } else displayFragment(VegaCoffeeOffloadTypeSelectFragment.newInstance(), false)
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
            WEIGHSCALE_LIST -> displayFragment(
                VegaCoffeeOffloadWeighscaleListFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            MTNR -> displayFragment(VegaCoffeeMtnrTypeSelectFragment.newInstance(data as String), true)
            SUPPLIER -> displayFragment(VegaCoffeeMtnrTypeSelectFragment.newInstance(data as String), true)
            WEIGHSCALE -> displayFragment(
                VegaCoffeeMtnrConsignmentFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            WEIGHSCALE_SUPPLIER -> displayFragment(
                VegaCoffeeSupplierConsignmentFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )

            ADD_WEIGHT -> displayFragment(
                VegaCoffeeMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCoffeeMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
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
                VegaCoffeeMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCoffeeMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                    is VegaCoffeeSupplierConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }
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
            is VegaCoffeeMtnrWeighScaleSummaryFragment -> {
            }
            is VegaCoffeeMtnrTypeSelectFragment -> {
            }
            is VegaCoffeeOffloadWeighscaleListFragment -> {

            }
            is VegaCoffeeSupplierConsignmentFragment -> {

            }
            is VegaCoffeeOffloadTypeSelectFragment -> {
                when (fragment1) {
                    is VegaCoffeeMtnrTypeSelectFragment -> {
                    }
                    else -> finish()
                }
            }
            is VegaCoffeeMtnrWeighScalePalletFragment -> {
            }
            is VegaCoffeeMtnrConsignmentFragment -> {
                when (fragment1) {
                    is VegaCoffeeMtnrWeighScaleSummaryFragment -> fragment.getBack()
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
            is VegaCoffeeMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoffeeMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun callBack(type: String, receiveLots: VegaCoffeeReceiveLots, receiving: VegaCoffeeReceiving) {
        when (type) {
            ADD_WEIGHT -> displayFragment(
                VegaCoffeeMtnrWeighScalePalletFragment.newInstance(receiveLots, receiving),
                true
            )
        }
    }
}
