package com.olam.warehouse.vegax.salescoffee.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.salescoffee.R
import com.olam.warehouse.vegax.salescoffee.data.domain.model.materialList
import com.olam.warehouse.vegax.salescoffee.di.injectCoffeeSalesFeature
import com.olam.warehouse.vegax.salescoffee.ui.weighbridge.VegaCoffeeSalesWeighbridgeAddLotFragment
import com.olam.warehouse.vegax.salescoffee.ui.weighbridge.VegaCoffeeSalesWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.salescoffee.utils.*

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */
class VegaCoffeeSalesActivity : HomeBaseActivity(),
    VegaCoffeeSalesSelectSaleTypeFragment.CallBack,
    VegaCoffeeSalesAddLotFragment.CallBack,
    VegaCoffeeDispatchLotListFragment.CallBack,
    VegaCoffeeSalesPalletFragment.CallBack,
    VegaCoffeeSalesPendingFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet,
    VegaCoffeeSalesSummaryFragment.CallBack, VegaCoffeeSalesWeighbridgeTruckListFragment.CallBack,
    VegaCoffeeSalesWeighbridgeAddLotFragment.CallBack {

    override val layoutResourceId = R.layout.activity_vega_coffee_sales
    private val mTAG = VegaCoffeeSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeSalesFeature()
        initNavigationView()
        displayFragment(VegaCoffeeSalesSelectSaleTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flSales,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(fragment: String) {
        val currentFrag = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            SALES_TYPE_WEIGHBRIDGE -> {
                displayFragment(VegaCoffeeSalesWeighbridgeTruckListFragment.newInstance(), true)
            }
            SALES_TYPE_WEIGHSCALE -> {
                when (currentFrag) {
                    is VegaCoffeeSalesSelectSaleTypeFragment -> {
                        displayFragment(
                            VegaCoffeeSalesAddLotFragment.newInstance(
                                VegaCoffeeSalesOrder(),
                                SALES_TYPE_WEIGHSCALE
                            ), true
                        )
                    }
                }

            }
            SALES_TYPE_ANTICIPATED -> {
                displayFragment(
                    VegaCoffeeSalesAddLotFragment.newInstance(
                        VegaCoffeeSalesOrder(),
                        SALES_TYPE_ANTICIPATED
                    ), true
                )
            }
            SALES_PENDING -> {
                when (currentFrag) {
                    is VegaCoffeeSalesSelectSaleTypeFragment -> {
                        displayFragment(VegaCoffeeSalesPendingFragment.newInstance(SALES_TYPE_WEIGHSCALE), true)
                    }
                }
            }
        }

    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, salesType: String) {
        when (fragment) {
            ADD_LOT -> {
                displayFragment(
                    VegaCoffeeSalesAddLotFragment.newInstance(
                        model,
                        salesType
                    ), true
                )
            }
            WEIGHBRIDGE_ADD_LOT -> {
                displayFragment(VegaCoffeeSalesWeighbridgeAddLotFragment.newInstance(model, salesType), true)
            }

        }
    }

    override fun replaceFragment(fragment: String, lot: VegaCoffeeSalesLots) {
        displayFragment(VegaCoffeeSalesPalletFragment.newInstance(lot), true)
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, materialList: ArrayList<String>) {
        when (fragment) {
            INVENTORY_FRAG -> displayFragment(VegaCoffeeDispatchLotListFragment.newInstance(model, materialList), true)
        }
    }

    override fun replaceFragmentSummary(
        fragment: String,
        model: VegaCoffeeSalesOrder,
        materialItems: ArrayList<materialList>
    ) {
        when (fragment) {
            SUMMARY -> displayFragment(VegaCoffeeSalesSummaryFragment.newInstance(model, materialItems), true)
        }
    }

    override fun replaceFragmentLotList(
        fragment: String,
        model: VegaCoffeeSalesOrder,
        materialList: ArrayList<String>,
        isMultiple: Boolean
    ) {
        displayFragment(VegaCoffeeDispatchLotListFragment.newInstance(model, materialList, false), true)
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaCoffeeSalesAddLotFragment)
            lotAddFragment.updateLotList(lots)
        else if (lotAddFragment is VegaCoffeeSalesWeighbridgeAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        displayFragment(VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false), true)
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaCoffeeSalesPalletFragment -> {
                fragment.updateBagWeight(prepareCoffeBagMaterial(bagMaterial))
            }
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaCoffeeSalesPalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaCoffeeSalesAddLotFragment)
            lotAddFragment.editLotDetails(model, lotId)
    }

    override fun onBackPressed() {
        backNaviagation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNaviagation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backNaviagation() {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flSales)
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flSales)) {
            is VegaCoffeeSalesAddLotFragment -> {
                when (fragment1) {
                    is VegaCoffeeSalesSummaryFragment -> fragment.getBack()
                }
            }
            is VegaCoffeeSalesSelectSaleTypeFragment -> {
                when (fragment1) {
                    is VegaCoffeeSalesAddLotFragment -> {
                    }
                    is VegaCoffeeSalesPendingFragment -> {
                    }
                    is VegaCoffeeSalesWeighbridgeTruckListFragment -> {
                    }
                    else -> super.onBackPressed()
                }
            }
        }
    }
}
