package com.olam.warehouse.vegax.qualityofanylot.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.qualityofanylot.R
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListData
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.di.injectAnyLotQualityFeature
import com.olam.warehouse.vegax.qualityofanylot.ui.params.VegaAnyLotQualityParamsFragment
import com.olam.warehouse.vegax.qualityofanylot.utils.ANY_LOT_QUALITY_PARAM
import com.olam.warehouse.vegax.qualityofanylot.utils.CREATE_QUALITY_LOT


class VegaAnyLotQualityActivity : HomeBaseActivity(),
    VegaAnyLotQualityLotListFragment.CallBack,VegaAnyLotQualitySelectFragment.CallBack,
    VegaAnyLotTransactionListFragment.CallBack,VegaAddAnyLotFragment.CallBack,VegaAnyLotQualityParamsFragment.CallBack{

    private val mTAG = VegaAnyLotQualityActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_any_lot_quality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectAnyLotQualityFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaAnyLotQualitySelectFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flLotQuality, allowBackStack = flag)
    }

    override fun replaceQualityFragment(
        id: String,
        item: VegaAnyLotQualityPostRequest,
       type: String
    ) {
        displayFragment(VegaAnyLotQualityParamsFragment.newInstance(item,id,type), true)
    }

    override fun replaceLotListToTransactionFragment(type: String, list: List<VegaAnyLotListData>,pos:Int) {
        displayFragment(VegaAnyLotTransactionListFragment.newInstance(type, list,pos),true)
    }


    override fun replaceFragment(fragment: String, type: String) {
        if(type == CREATE_QUALITY_LOT) {
            displayFragment(VegaAddAnyLotFragment.newInstance(type), true)
        }else{
            displayFragment(VegaAnyLotQualityLotListFragment.newInstance(type), true)
        }
    }

    override fun replaceQualityParamsFragment(id: String, item: VegaAnyLotQualityPostRequest, type: String) {
        displayFragment(VegaAnyLotQualityParamsFragment.newInstance(item,id,type), true)
    }


    override fun replaceLotListFragment(type: String, batchNo: String) {
        displayFragment(VegaAnyLotQualityLotListFragment.newInstance(type), true)
    }


    /*for jira ticket id : DWALL : 5048*/
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
        when (supportFragmentManager.findFragmentById(R.id.flLotQuality)) {
            is VegaAnyLotTransactionListFragment -> {
                supportFragmentManager.popBackStackImmediate()
                when(val fragment = supportFragmentManager.findFragmentById(R.id.flLotQuality)){
                    is VegaAnyLotQualityLotListFragment ->fragment.updateFragment()
                }
            }
            else -> {
                super.onBackPressed()
            }

        }
    }

    /*for jira ticket id : DWALL : 5048*/
    override fun onBackPressed() {
       backNavigation()
    }

    override fun replaceFragment(from: String, data: Any, typeSelect: String) {
        when(from){
            ANY_LOT_QUALITY_PARAM-> {
                displayFragment(VegaAnyQualitySummaryFragment.newInstance(data as VegaAnyLotQualityPostRequest,typeSelect),true)
            }
        }
    }

}
