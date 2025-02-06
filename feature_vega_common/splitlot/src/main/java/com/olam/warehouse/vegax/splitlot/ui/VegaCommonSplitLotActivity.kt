package com.olam.warehouse.vegax.splitlot.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.model.VegaCommonSplitLotModel
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.splitlot.R
import com.olam.warehouse.vegax.splitlot.di.injectCommonSplitLotFeature

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */
class VegaCommonSplitLotActivity : HomeBaseActivity(), VegaCommonSplitLotListFragment.Callback, VegaCommonSplitLotEnteredFragment.Callback {
    override val layoutResourceId = R.layout.activity_common_split_lot
    private val mTAG = VegaCommonSplitLotActivity::class.java.canonicalName
    private var isFirstBack: Boolean? = false
    private var splitLotList = arrayListOf<VegaCommonSplitLotModel>()
    private var receivingdata = VegaReceiving()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCommonSplitLotFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        var lotList = arrayListOf<VegaCoffeeLot>()

        if(intent?.hasExtra(UIUtils.LOT_DETAIL) == true) {
            val bundle = intent?.extras
            lotList = bundle?.getParcelableArrayList<VegaCoffeeLot>(UIUtils.LOT_DETAIL)?: ArrayList()
            receivingdata = bundle?.getParcelable<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
        }
        displayFragment(VegaCommonSplitLotListFragment.newInstance(lotList), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flSplitLot, allowBackStack = flag)
    }

    override fun replaceFragment(SPLIT_LOT_ENTERED: String, lot: VegaCoffeeLot) {
        if(!splitLotList.any { it.parentBatchNumber.equals(lot.batchNumber) }) splitLotList.clear()
        displayFragment(VegaCommonSplitLotEnteredFragment.newInstance(lot, splitLotList,receivingdata), true)
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
        val fragment = supportFragmentManager.findFragmentById(R.id.flSplitLot)
        when (fragment) {
            is VegaCommonSplitLotEnteredFragment -> fragment.getBack()
            is VegaCommonSplitLotListFragment -> {
                if(isFirstBack == true) {
                    isFirstBack = false
                    fragment.updateStatus(splitLotList)
                } else fragment.getBack()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFragment(splitLotList: ArrayList<VegaCommonSplitLotModel>) {
        this.splitLotList = splitLotList
        supportFragmentManager.popBackStackImmediate()
        val fragment = supportFragmentManager.findFragmentById(R.id.flSplitLot)
        when (fragment) {
            is VegaCommonSplitLotListFragment -> {
                fragment.updateStatus(splitLotList)
            }
            is VegaCommonSplitLotEnteredFragment -> {
                isFirstBack = true

            }
        }
    }
}
