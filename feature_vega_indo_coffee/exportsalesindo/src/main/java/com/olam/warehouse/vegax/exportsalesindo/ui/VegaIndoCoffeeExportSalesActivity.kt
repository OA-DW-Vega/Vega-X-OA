package com.olam.warehouse.vegax.exportsalesindo.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_SALES
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.exportsalesindo.R
import com.olam.warehouse.vegax.exportsalesindo.di.injectIndoCoffeeExportSalesFeature
import com.olam.warehouse.vegax.exportsalesindo.ui.transaction.VegaIndoCoffeeExportSalesTransactionDetailsFragment
import com.olam.warehouse.vegax.exportsalesindo.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.exportsalesindo.utils.MOVE_ADD_LOT
import com.olam.warehouse.vegax.exportsalesindo.utils.MOVE_SUMMARY

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeExportSalesActivity : HomeBaseActivity(),
    VegaIndoCoffeeExportAddContainerFragment.CallBack,
    VegaIndoCoffeeExportAddLotFragment.CallBack,
    VegaIndoCoffeeExportStocksFragment.CallBack,
    VegaIndoCoffeeExportSalesTransactionDetailsFragment.CallBack,
    VegaIndoCoffeeExportSummaryFragment.CallBack {

    override val layoutResourceId = R.layout.activity_indo_coffee_export_sales
    private val mTAG = VegaIndoCoffeeExportSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndoCoffeeExportSalesFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent?.hasExtra(TRANS_SALES) == true)
            displayFragment(VegaIndoCoffeeExportSalesTransactionDetailsFragment.newInstance(), false)
        else
            displayFragment(VegaIndoCoffeeExportAddContainerFragment(), false)
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
                VegaIndoCoffeeExportAddLotFragment.newInstance(
                    data as VegaCoffeeExportSalesContainer,
                    materialList
                ), true
            )
        }
    }

    override fun replaceSummaryFragment(
        fragment: String,
        data: Any,
        data1: Any,
        materialList: ArrayList<IndoExporSalesMaterialList>
    ) {
        when (fragment) {
            MOVE_SUMMARY -> displayFragment(
                VegaIndoCoffeeExportSummaryFragment.newInstance(data as String, data1 as String, materialList),
                true
            )
        }
    }

    override fun replaceFragment(it: VegaIndoCoffeeExportSalesOrder) {
        displayFragment(VegaIndoCoffeeExportAddContainerFragment.newInstance(it), false)
    }

    override fun replaceFragment(fragment: String, selectedList: ArrayList<String>, materialList: ArrayList<String>) {
        when (fragment) {
            INVENTORY_FRAG -> displayFragment(
                VegaIndoCoffeeExportStocksFragment.newInstance(materialList, selectedList),
                true
            )
        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaIndoCoffeeExportAddLotFragment)
            lotAddFragment.updateLotList(lots)
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
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flExportSales)
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flExportSales)) {
            is VegaIndoCoffeeExportAddContainerFragment -> {
                when (fragment1) {
                    is VegaIndoCoffeeExportSummaryFragment -> fragment.getBack()
                    is VegaIndoCoffeeExportAddLotFragment -> {
                    }
                    else -> super.onBackPressed()
                }
            }
            is VegaIndoCoffeeExportAddLotFragment -> {
            }
            else -> super.onBackPressed()
        }
    }

    override fun editLotDetails(container: VegaCoffeeExportSalesContainer) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaIndoCoffeeExportAddContainerFragment)
            lotAddFragment.editLotDetails(container)
    }

}
