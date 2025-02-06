package com.olam.warehouse.vegax.secretidcommon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.di.injectVegaCommonSecretIdFeature
import com.olam.warehouse.vegax.secretidcommon.utils.LOT_LIST
import com.olam.warehouse.vegax.secretidcommon.utils.MODULE_SELECT
import com.olam.warehouse.vegax.secretidcommon.utils.OFFLOADING_SELECT

class VegaSecretIdActivity : HomeBaseActivity(), VegaSecretIdModuleSelectFragment.CallBack,
    VegaSecretSelectLotFragment.CallBack, VegaCommonSecretIdLotListFragment.CallBack,
    VegaCommonSecretIdOffloadingFragment.OnWeighBridgeListener {

    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var wbDetails: VegaQualityWBDetails? = null


    private val mTAG = VegaSecretIdActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_common_secret

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaCommonSecretIdFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaSecretSelectLotFragment.newInstance(), false)

      //  displayFragment(VegaSecretIdModuleSelectFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flSecretId, allowBackStack = flag)
    }

    override fun replaceFragment(fragment: String) {
        when(fragment){
            MODULE_SELECT -> displayFragment(VegaSecretSelectLotFragment.newInstance(),false)
            OFFLOADING_SELECT-> displayFragment(VegaCommonSecretIdOffloadingFragment.newInstance(),false)
        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flSecretId)
        if (lotAddFragment is VegaSecretSelectLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun replaceFragment(fragment: String, list: ArrayList<VegaCocoaDispatchLots>) {
        when(fragment){
            LOT_LIST -> displayFragment(VegaCommonSecretIdLotListFragment.newInstance(list),true)
        }
    }

    override fun onWeighBridgeClick(wbDetails: VegaQualityWBDetails?) {
        this.wbDetails = wbDetails
        wbDetails?.let { VegaCommonOffloadingDetailsFragment.newInstance(it) }?.let { displayFragment(it,false) }
    }

    override fun setQualityWBList(it: List<VegaQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }


}
