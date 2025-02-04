package com.olam.warehouse.vegax.exportsalesnigeria.ui

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
import com.olam.warehouse.vegax.exportsalesnigeria.R
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesAssignLot
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesTextDetail
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesWSBagModel
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.materialList
import com.olam.warehouse.vegax.exportsalesnigeria.di.injectCoffeeExportSalesFeature
import com.olam.warehouse.vegax.exportsalesnigeria.utils.*

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */
class VegaNigeriaExportSalesActivity : HomeBaseActivity(),
    VegaNigeriaExportAddContainerFragment.CallBack,
    VegaNigeriaExportAddLotFragment.CallBack,
    VegaNigeriaExportStocksFragment.CallBack,
    VegaNigeriaExportSummaryFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet,
    VegaNigeriaExportSaleAssignLotFragment.VegaNigeriaAsignLotCallback,
VegaNigeriaExportSalesWeighScalePalletFragment.VegaNigeriaExportSalesReplaceFragmentCallback
{

    override val layoutResourceId = R.layout.activity_vega_nigeria_export_sales
    private val mTAG = VegaNigeriaExportSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeExportSalesFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaExportAddContainerFragment(), false)
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
                VegaNigeriaExportAddLotFragment.newInstance(
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
        textdetailList: ArrayList<VegaNigeriaExportSalesTextDetail>
    ) {
        when (fragment) {
            MOVE_SUMMARY -> displayFragment(
                VegaNigeriaExportSummaryFragment.newInstance(
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
                VegaNigeriaExportStocksFragment.newInstance(materialList, selectedList),
                true
            )
        }
    }

    override fun replaceFragment(flag: String, data: Any) {
        when(flag){
            ADD_WEIGHT ->
            {
                displayFragment(
                    VegaNigeriaExportSalesWeighScalePalletFragment.newInstance(data as VegaCoffeeExportSalesLots),
                    true
                )
            }
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
                when (fragment) {
                    is VegaNigeriaExportAddLotFragment -> fragment.updateAddWeight(data as VegaNigeriaExportSalesWSBagModel)
                }
            }

        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaNigeriaExportAddLotFragment)
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
            is VegaNigeriaExportAddContainerFragment -> {
                when (fragment1) {
                    is VegaNigeriaExportSummaryFragment -> fragment.getBack()
                    is VegaNigeriaExportAddLotFragment -> {
                    }
                    else -> {
                        fragment.onbackpressed()
                        super.onBackPressed()
                    }
                }
            }
            is VegaNigeriaExportAddLotFragment -> {
            }
            is VegaNigeriaExportSalesWeighScalePalletFragment -> {}
            is VegaNigeriaExportSummaryFragment ->{
               /* when(fragment1){
                    is VegaNigeriaExportSaleAssignLotFragment -> {
                        fragment.updateAssignLotText()
                    }
                }*/
            }
            else -> super.onBackPressed()
        }
    }

    override fun editLotDetails(container: VegaCoffeeExportSalesContainer) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaNigeriaExportAddContainerFragment)
            lotAddFragment.editLotDetails(container)
    }

    override fun navigateToAssignLot(
        containerList: ArrayList<ContainerWithLots>,
        materialList: ArrayList<materialList>
    ) {
        displayFragment(
            VegaNigeriaExportSaleAssignLotFragment.newInstance(containerList,materialList),
            true
        )
    }


    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        when (fragment) {
            is VegaNigeriaExportSalesWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }


    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        when (fragment) {
            is VegaNigeriaExportSalesWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateAssignLotText(
        mergedLotIds: List<String?>,
        list: ArrayList<VegaNigeriaExportSalesAssignLot>,
        selectedStorageLocation: String
    ) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        when (fragment) {
            is VegaNigeriaExportSummaryFragment -> {
                fragment.updateAssignLotText(mergedLotIds,list,selectedStorageLocation)
            }
        }
    }

}
