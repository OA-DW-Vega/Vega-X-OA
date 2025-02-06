package com.olam.warehouse.vegax.exportsalesecuador.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.exportsalesecuador.R
import com.olam.warehouse.vegax.exportsalesecuador.data.domain.model.materialList
import com.olam.warehouse.vegax.exportsalesecuador.di.injectCoffeeExportSalesFeature
import com.olam.warehouse.vegax.exportsalesecuador.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.exportsalesecuador.utils.MOVE_ADD_LOT
import com.olam.warehouse.vegax.exportsalesecuador.utils.MOVE_SUMMARY

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */
class VegaEcuadorCocoaExportSalesActivity : HomeBaseActivity(),
    VegaEcuadorCocoaExportAddContainerFragment.CallBack,
    VegaEcuadorCocoaExportAddLotFragment.CallBack,
    VegaEcuadorCocoaExportStocksFragment.CallBack,
    VegaEcuadorCocoaExportSummaryFragment.CallBack {

    override val layoutResourceId = R.layout.activity_vega_ecuador_cocoa_export_sales
    private val mTAG = VegaEcuadorCocoaExportSalesActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeExportSalesFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaEcuadorCocoaExportAddContainerFragment(), false)
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
                VegaEcuadorCocoaExportAddLotFragment.newInstance(
                    data as VegaCoffeeExportSalesContainer,
                    materialList
                ), true
            )
        }
    }

    override fun replaceSummaryFragment(fragment: String, data: Any, materialList: ArrayList<materialList>) {
        when (fragment) {
            MOVE_SUMMARY -> displayFragment(
                VegaEcuadorCocoaExportSummaryFragment.newInstance(data as String, materialList),
                true
            )
        }
    }

    override fun replaceFragment(fragment: String, selectedList: ArrayList<String>, materialList: ArrayList<String>) {
        when (fragment) {
            INVENTORY_FRAG -> displayFragment(
                VegaEcuadorCocoaExportStocksFragment.newInstance(materialList, selectedList),
                true
            )
        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaEcuadorCocoaExportAddLotFragment)
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
            is VegaEcuadorCocoaExportAddContainerFragment -> {
                when (fragment1) {
                    is VegaEcuadorCocoaExportSummaryFragment -> fragment.getBack()
                    is VegaEcuadorCocoaExportAddLotFragment -> {
                    }
                    else -> super.onBackPressed()
                }
            }
            is VegaEcuadorCocoaExportAddLotFragment -> {
            }
            else -> super.onBackPressed()
        }
    }

    override fun editLotDetails(container: VegaCoffeeExportSalesContainer) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flExportSales)
        if (lotAddFragment is VegaEcuadorCocoaExportAddContainerFragment)
            lotAddFragment.editLotDetails(container)
    }

}
