package com.olam.warehouse.vegax.exportsalescameroon.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.exportsalescameroon.R
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.VegaCameroonExportSalesAssignLot
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.VegaCameroonExportSalesTextDetail
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.VegaCameroonExportSalesWSBagModel
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.materialList
import com.olam.warehouse.vegax.exportsalescameroon.di.injectCoffeeExportSalesFeature
import com.olam.warehouse.vegax.exportsalescameroon.utils.*

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */
class VegaCameroonExportSalesActivity : HomeBaseActivity(),
    VegaCameroonExportAddContainerFragment.CallBack,
    VegaCameroonExportAddLotFragment.CallBack,
    VegaCameroonExportStocksFragment.CallBack,
    VegaCameroonExportSummaryFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet,
    VegaCameroonExportSaleAssignLotFragment.VegaCameroonAsignLotCallback,
VegaCameroonExportSalesWeighScalePalletFragment.VegaCameroonExportSalesReplaceFragmentCallback
{

    override val layoutResourceId = R.layout.activity_vega_cameroon_export_sales
    private val mTAG = VegaCameroonExportSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeExportSalesFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCameroonExportAddContainerFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flExportSales, allowBackStack = flag)
    }

    override fun replaceFragment(
        fragment: String,
        data: Any,
        materialList: ArrayList<String>
    ) {
        when (fragment) {
            MOVE_ADD_LOT -> displayFragment(
                VegaCameroonExportAddLotFragment.newInstance(
                    data as VegaCoffeeExportSalesContainer,
                    materialList
                ), true
            )
        }
    }

    override fun replaceSummaryFragment(
        fragment: String,
        data: Any,
        materialList: ArrayList<materialList>,
        textdetailList: ArrayList<VegaCameroonExportSalesTextDetail>
    ) {
        when (fragment) {
            MOVE_SUMMARY -> displayFragment(
                VegaCameroonExportSummaryFragment.newInstance(
                    data as String,
                    materialList,
                    textdetailList
                ),
                true
            )
        }
    }

    override fun replaceFragment(fragment: String, selectedList: ArrayList<String>, materialList: ArrayList<String>) {
        when (fragment) {
            INVENTORY_FRAG -> displayFragment(
                VegaCameroonExportStocksFragment.newInstance(materialList, selectedList),
                true
            )
        }
    }

    override fun replaceFragment(flag: String, data: Any) {
        when(flag){
            ADD_WEIGHT -> {
                displayFragment(
                    VegaCameroonExportSalesWeighScalePalletFragment.newInstance(data as VegaCoffeeExportSalesLots),
                    true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
                        when (fragment) {
                            is VegaCameroonExportSalesWeighScalePalletFragment -> {
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
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
                when (fragment) {
                    is VegaCameroonExportAddLotFragment -> fragment.updateAddWeight(data as VegaCameroonExportSalesWSBagModel)
                }
            }

        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaCameroonExportAddLotFragment)
            lotAddFragment.updateLotList(lots)
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
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flExportSales)
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)) {
            is VegaCameroonExportAddContainerFragment -> {
                when (fragment1) {
                    is VegaCameroonExportSummaryFragment -> fragment.getBack()
                    is VegaCameroonExportAddLotFragment -> {
                    }
                    else -> {
                        fragment.onbackpressed()
                        super.onBackPressed()
                    }
                }
            }
            is VegaCameroonExportAddLotFragment -> {
            }
            is VegaCameroonExportSalesWeighScalePalletFragment -> {}
            is VegaCameroonExportSummaryFragment ->{

            }
            else -> super.onBackPressed()
        }
    }

    override fun editLotDetails(container: VegaCoffeeExportSalesContainer) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaCameroonExportAddContainerFragment)
            lotAddFragment.editLotDetails(container)
    }

    override fun navigateToAssignLot(
        containerList: ArrayList<ContainerWithLots>,
        materialList: ArrayList<materialList>
    ) {
        displayFragment(
            VegaCameroonExportSaleAssignLotFragment.newInstance(containerList,materialList),
            true
        )
    }


    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        when (fragment) {
            is VegaCameroonExportSalesWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }


    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        when (fragment) {
            is VegaCameroonExportSalesWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateAssignLotText(
        mergedLotIds: List<String?>,
        list: ArrayList<VegaCameroonExportSalesAssignLot>,
        selectedStorageLocation: String
    ) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        when (fragment) {
            is VegaCameroonExportSummaryFragment -> {
                fragment.updateAssignLotText(mergedLotIds, list, selectedStorageLocation)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
