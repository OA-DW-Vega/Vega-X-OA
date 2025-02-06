package com.olam.warehouse.vegax.localsalesecuador.ui

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
import com.olam.warehouse.vegax.localsalesecuador.R
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalesecuador.di.injectCoffeeSalesFeature
import com.olam.warehouse.vegax.localsalesecuador.ui.weighbridge.VegaEcuadorCocoaSalesWeighbridgeAddLotFragment
import com.olam.warehouse.vegax.localsalesecuador.ui.weighbridge.VegaEcuadorCocoaSalesWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.localsalesecuador.utils.*

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */
class VegaEcuadorCocoaSalesActivity : HomeBaseActivity(),
    VegaEcuadorCocoaSalesSelectSaleTypeFragment.CallBack,
    VegaEcuadorCocoaSalesAddLotFragment.CallBack,
    VegaEcuadorCocoaDispatchLotListFragment.CallBack,
    VegaEcuadorCocoaSalesPalletFragment.CallBack,
    VegaEcuadorCocoaSalesPendingFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet,
    VegaEcuadorCocoaSalesSummaryFragment.CallBack, VegaEcuadorCocoaSalesWeighbridgeTruckListFragment.CallBack,
    VegaEcuadorCocoaSalesWeighbridgeAddLotFragment.CallBack {

    override val layoutResourceId = R.layout.activity_vega_ecuador_cocoa_sales
    private val mTAG = VegaEcuadorCocoaSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeSalesFeature()
        initNavigationView()
        displayFragment(VegaEcuadorCocoaSalesSelectSaleTypeFragment.newInstance(), false)
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
                displayFragment(VegaEcuadorCocoaSalesWeighbridgeTruckListFragment.newInstance(), true)
            }
            SALES_TYPE_WEIGHSCALE -> {
                when (currentFrag) {
                    is VegaEcuadorCocoaSalesSelectSaleTypeFragment -> {
                        displayFragment(
                            VegaEcuadorCocoaSalesAddLotFragment.newInstance(
                                VegaCoffeeSalesOrder(),
                                SALES_TYPE_WEIGHSCALE
                            ), true
                        )
                    }
                }

            }
            SALES_TYPE_ANTICIPATED -> {
                displayFragment(
                    VegaEcuadorCocoaSalesAddLotFragment.newInstance(
                        VegaCoffeeSalesOrder(),
                        SALES_TYPE_ANTICIPATED
                    ), true
                )
            }
            SALES_PENDING -> {
                when (currentFrag) {
                    is VegaEcuadorCocoaSalesSelectSaleTypeFragment -> {
                        displayFragment(VegaEcuadorCocoaSalesPendingFragment.newInstance(SALES_TYPE_WEIGHSCALE), true)
                    }
                }
            }
        }

    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, salesType: String) {
        when (fragment) {
            ADD_LOT -> {
                displayFragment(
                    VegaEcuadorCocoaSalesAddLotFragment.newInstance(
                        model,
                        salesType
                    ), true
                )
            }
            WEIGHBRIDGE_ADD_LOT -> {
                displayFragment(VegaEcuadorCocoaSalesWeighbridgeAddLotFragment.newInstance(model, salesType), true)
            }

        }
    }

    override fun replaceFragment(fragment: String, lot: VegaCoffeeSalesLots) {
        displayFragment(VegaEcuadorCocoaSalesPalletFragment.newInstance(lot), true)
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, materialList: ArrayList<String>) {
        when (fragment) {
            INVENTORY_FRAG -> displayFragment(VegaEcuadorCocoaDispatchLotListFragment.newInstance(model, materialList), true)
        }
    }

    override fun replaceFragmentSummary(
        fragment: String,
        model: VegaCoffeeSalesOrder,
        materialItems: ArrayList<materialList>
    ) {
        when (fragment) {
            SUMMARY -> displayFragment(VegaEcuadorCocoaSalesSummaryFragment.newInstance(model, materialItems), true)
        }
    }

    override fun replaceFragmentLotList(
        fragment: String,
        model: VegaCoffeeSalesOrder,
        materialList: ArrayList<String>,
        isMultiple: Boolean
    ) {
        displayFragment(VegaEcuadorCocoaDispatchLotListFragment.newInstance(model, materialList, true), true)
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaEcuadorCocoaSalesAddLotFragment)
            lotAddFragment.updateLotList(lots)
        else if (lotAddFragment is VegaEcuadorCocoaSalesWeighbridgeAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        displayFragment(VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false), true)
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaEcuadorCocoaSalesPalletFragment -> {
                fragment.updateBagWeight(prepareCoffeBagMaterial(bagMaterial))
            }
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaEcuadorCocoaSalesPalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaEcuadorCocoaSalesAddLotFragment)
            lotAddFragment.editLotDetails(model, lotId)
    }

    override fun onBackPressed() {
        backNaviagation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
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
            is VegaEcuadorCocoaSalesAddLotFragment -> {
                when (fragment1) {
                    is VegaEcuadorCocoaSalesSummaryFragment -> fragment.getBack()
                }
            }
            is VegaEcuadorCocoaSalesSelectSaleTypeFragment -> {
                when (fragment1) {
                    is VegaEcuadorCocoaSalesAddLotFragment -> {
                    }
                    is VegaEcuadorCocoaSalesPendingFragment -> {
                    }
                    is VegaEcuadorCocoaSalesWeighbridgeTruckListFragment -> {
                    }
                    else -> super.onBackPressed()
                }
            }
        }
    }
}
