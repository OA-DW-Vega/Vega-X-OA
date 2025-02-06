package com.olam.warehouse.vegax.localsalescameroon.ui

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
import com.olam.warehouse.vegax.localsalescameroon.R
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonLocalSalesAssignLot
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalescameroon.di.injectCameroonSalesFeature
import com.olam.warehouse.vegax.localsalescameroon.utils.*

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */
class VegaCameroonSalesActivity : HomeBaseActivity(),
    VegaCameroonSalesSelectSaleTypeFragment.CallBack,
    VegaCameroonSalesAddLotFragment.CallBack,
    VegaCameroonDispatchLotListFragment.CallBack,
    VegaCameroonSalesPalletFragment.CallBack,
    VegaCameroonSalesPendingFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet,
    VegaCameroonLocalSalesAssignLotFragment.VegaCameroonAsignLotCallback,
    VegaCameroonSalesSummaryFragment.CallBack {

    override val layoutResourceId = R.layout.activity_vega_cameroon_sales
    private val mTAG = VegaCameroonSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonSalesFeature()
        initNavigationView()
        displayFragment(
            VegaCameroonSalesAddLotFragment.newInstance(
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
                //Comment for Sonar Fix
            }
            SALES_TYPE_WEIGHSCALE -> {
                when (currentFrag) {
                    is VegaCameroonSalesSelectSaleTypeFragment -> {
                        displayFragment(
                            VegaCameroonSalesAddLotFragment.newInstance(
                                VegaCoffeeSalesOrder(),
                                SALES_TYPE_WEIGHSCALE
                            ), true
                        )
                    }
                }

            }
            SALES_TYPE_ANTICIPATED -> {
                displayFragment(
                    VegaCameroonSalesAddLotFragment.newInstance(
                        VegaCoffeeSalesOrder(),
                        SALES_TYPE_ANTICIPATED
                    ), true
                )
            }
            SALES_PENDING -> {
                when (currentFrag) {
                    is VegaCameroonSalesSelectSaleTypeFragment -> {
                        displayFragment(VegaCameroonSalesPendingFragment.newInstance(SALES_TYPE_WEIGHSCALE), true)
                    }
                }
            }
        }

    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, salesType: String) {
        when (fragment) {
            ADD_LOT -> {
                displayFragment(
                    VegaCameroonSalesAddLotFragment.newInstance(
                        model,
                        salesType
                    ), true
                )
            }

        }
    }

    
    override fun replaceFragment(fragment: String, lot: VegaCoffeeSalesLots) {
        displayFragment(VegaCameroonSalesPalletFragment.newInstance(lot), true)
        initBt(object : BtObserve {
            override fun valueObserve(btValue: String) {
                val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
                when (fragment) {
                    is VegaCameroonSalesPalletFragment -> {
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
            INVENTORY_FRAG -> displayFragment(VegaCameroonDispatchLotListFragment.newInstance(model, materialList), true)
        }
    }

    override fun replaceFragmentSummary(
        fragment: String,
        model: VegaCoffeeSalesOrder,
        materialItems: ArrayList<materialList>
    ) {
        when (fragment) {
            SUMMARY -> displayFragment(VegaCameroonSalesSummaryFragment.newInstance(model, materialItems), true)
        }
    }


    override fun addedLots(lots: ArrayList<VegaCoffeeSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaCameroonSalesAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        displayFragment(VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false), true)
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaCameroonSalesPalletFragment -> {
                fragment.updateBagWeight(prepareCoffeBagMaterial(bagMaterial))
            }
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaCameroonSalesPalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSales)
        if (lotAddFragment is VegaCameroonSalesAddLotFragment)
            lotAddFragment.editLotDetails(model, lotId)
    }


    override fun navigateToAssignLot(
        salesOrder: VegaCoffeeSalesOrder,
        materialList: ArrayList<materialList>
    ) {
        displayFragment(
            VegaCameroonLocalSalesAssignLotFragment.newInstance(salesOrder, materialList),
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
            is VegaCameroonSalesAddLotFragment -> {
                when (fragment1) {
                    is VegaCameroonSalesSummaryFragment -> fragment.getBack()
                    is VegaCameroonSalesAddLotFragment -> super.onBackPressed()
                }
            }
            is VegaCameroonSalesSelectSaleTypeFragment -> {
                when (fragment1) {
                    is VegaCameroonSalesAddLotFragment -> {
                    }
                    is VegaCameroonSalesPendingFragment -> {
                    }
                    else -> super.onBackPressed()
                }
            }
        }
    }

    override fun updateAssignLotText(
        mergedLotIds: List<String?>,
        list: ArrayList<VegaCameroonLocalSalesAssignLot>,
        selectedStorageLocation: String
    ) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flSales)
        when (fragment) {
            is VegaCameroonSalesSummaryFragment -> {
                fragment.updateAssignLotText(mergedLotIds, list, selectedStorageLocation)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
