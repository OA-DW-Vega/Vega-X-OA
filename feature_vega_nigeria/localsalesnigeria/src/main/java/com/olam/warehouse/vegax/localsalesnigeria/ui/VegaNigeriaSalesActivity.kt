package com.olam.warehouse.vegax.localsalesnigeria.ui

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
import com.olam.warehouse.vegax.localsalesnigeria.R
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaLocalSalesAssignLot
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalesnigeria.di.injectNigeriaSalesFeature
import com.olam.warehouse.vegax.localsalesnigeria.utils.*

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */
class VegaNigeriaSalesActivity : HomeBaseActivity(),
    VegaNigeriaSalesSelectSaleTypeFragment.CallBack,
    VegaNigeriaSalesAddLotFragment.CallBack,
    VegaNigeriaDispatchLotListFragment.CallBack,
    VegaNigeriaSalesPalletFragment.CallBack,
    VegaNigeriaSalesPendingFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet,
    VegaNigeriaLocalSalesAssignLotFragment.VegaNigeriaAsignLotCallback,
    VegaNigeriaSalesSummaryFragment.CallBack {

    override val layoutResourceId = R.layout.activity_vega_nigeria_sales
    private val mTAG = VegaNigeriaSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaSalesFeature()
        initNavigationView()
//        displayFragment(VegaNigeriaSalesSelectSaleTypeFragment.newInstance(), false)
        displayFragment(
            VegaNigeriaSalesAddLotFragment.newInstance(
                VegaCoffeeSalesOrder(),
                SALES_TYPE_WEIGHSCALE
            ), false
        )
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
//                displayFragment(VegaNigeriaSalesWeighbridgeTruckListFragment.newInstance(), true)
            }
            SALES_TYPE_WEIGHSCALE -> {
                when (currentFrag) {
                    is VegaNigeriaSalesSelectSaleTypeFragment -> {
                        displayFragment(
                            VegaNigeriaSalesAddLotFragment.newInstance(
                                VegaCoffeeSalesOrder(),
                                SALES_TYPE_WEIGHSCALE
                            ), true
                        )
                    }
                }

            }
            SALES_TYPE_ANTICIPATED -> {
                displayFragment(
                    VegaNigeriaSalesAddLotFragment.newInstance(
                        VegaCoffeeSalesOrder(),
                        SALES_TYPE_ANTICIPATED
                    ), true
                )
            }
            SALES_PENDING -> {
                when (currentFrag) {
                    is VegaNigeriaSalesSelectSaleTypeFragment -> {
                        displayFragment(VegaNigeriaSalesPendingFragment.newInstance(SALES_TYPE_WEIGHSCALE), true)
                    }
                }
            }
        }

    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, salesType: String) {
        when (fragment) {
            ADD_LOT -> {
                displayFragment(
                    VegaNigeriaSalesAddLotFragment.newInstance(
                        model,
                        salesType
                    ), true
                )
            }

        }
    }

    
    override fun replaceFragment(fragment: String, lot: VegaCoffeeSalesLots) {
        displayFragment(VegaNigeriaSalesPalletFragment.newInstance(lot), true)
        initBt(object : BtObserve {
            override fun valueObserve(btValue: String) {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaNigeriaSalesPalletFragment -> {
                        val fragment1 = VegaCocoaAddPalletFragment()
                        fragment1.updateBtWeight(btValue)
                    }
                    is VegaSweepingWeightEntryFragment -> {
                        fragment.updateBtWeight(btValue)
                    }
                }
            }

        })
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, materialList: ArrayList<String>) {
        when (fragment) {
            INVENTORY_FRAG -> displayFragment(VegaNigeriaDispatchLotListFragment.newInstance(model, materialList), true)
        }
    }

    override fun replaceFragmentSummary(
        fragment: String,
        model: VegaCoffeeSalesOrder,
        materialItems: ArrayList<materialList>
    ) {
        when (fragment) {
            SUMMARY -> displayFragment(VegaNigeriaSalesSummaryFragment.newInstance(model, materialItems), true)
        }
    }
/*
    override fun replaceFragmentLotList(
        fragment: String,
        model: VegaCoffeeSalesOrder,
        materialList: ArrayList<String>,
        isMultiple: Boolean
    ) {
        displayFragment(VegaNigeriaDispatchLotListFragment.newInstance(model, materialList, false), true)
    }*/

    override fun addedLots(lots: ArrayList<VegaCoffeeSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaNigeriaSalesAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        displayFragment(VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false), true)
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaNigeriaSalesPalletFragment -> {
                fragment.updateBagWeight(prepareCoffeBagMaterial(bagMaterial))
            }
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaNigeriaSalesPalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaNigeriaSalesAddLotFragment)
            lotAddFragment.editLotDetails(model, lotId)
    }


    override fun navigateToAssignLot(
        salesOrder: VegaCoffeeSalesOrder,
        materialList: ArrayList<materialList>
    ) {
        println("Roshna => Navigate to Assign lot")
        displayFragment(
            VegaNigeriaLocalSalesAssignLotFragment.newInstance(salesOrder, materialList),
            true
        )
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
            is VegaNigeriaSalesAddLotFragment -> {
                when (fragment1) {
                    is VegaNigeriaSalesSummaryFragment -> fragment.getBack()
                    is VegaNigeriaSalesAddLotFragment -> super.onBackPressed()
                }
            }
            is VegaNigeriaSalesSelectSaleTypeFragment -> {
                when (fragment1) {
                    is VegaNigeriaSalesAddLotFragment -> {
                    }
                    is VegaNigeriaSalesPendingFragment -> {
                    }
                    else -> super.onBackPressed()
                }
            }
        }
    }

    override fun updateAssignLotText(
        mergedLotIds: List<String?>,
        list: ArrayList<VegaNigeriaLocalSalesAssignLot>,
        selectedStorageLocation: String
    ) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaNigeriaSalesSummaryFragment -> {
                fragment.updateAssignLotText(mergedLotIds,list,selectedStorageLocation)
            }
        }
    }
}
