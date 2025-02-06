package com.olam.warehouse.vegax.approveghana.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.Constants.TRANSACTIONID
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.approveghana.R
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGRNGhanaQuality
import com.olam.warehouse.vegax.approveghana.di.injectGhanaGrnFeature
import com.olam.warehouse.vegax.approveghana.ui.offline.VegaGhanaGrnOfflineSummary
import com.olam.warehouse.vegax.approveghana.utils.GRN_FRAG
import com.olam.warehouse.vegax.approveghana.utils.GRN_OFFLINE_FRAG
import com.olam.warehouse.vegax.approveghana.utils.GRN_QUALITY_DETAILS



/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaGhanaGrnActivity : HomeBaseActivity(), VegaGhanaGrnWBListFragment.CallBack,
    VegaGhanaGrnOfflineSummary.CallBack,VegaGhanaGrnDetailsFragment.QualityDetailsCallBack,VegaGhanaGRNQualityFragment.CallBack {

    private val mTAG = VegaGhanaGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nigeria_ghana_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaGrnFeature()
        initNavigationView()
        initUI()

    }

    private fun initUI() {
        if(intent?.hasExtra(TRANSACTIONID) == true)
            displayFragment(VegaGhanaGrnWBListFragment.newInstance(intent?.getStringExtra(TRANSACTIONID)?:""), false)
        else
            displayFragment(VegaGhanaGrnWBListFragment.newInstance(""), false)
        val data = arrayListOf<String>()
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }


    override fun replaceFragment(
        moveFrag: String,
        item: VegaGrnWeighBridgeId
    ) {
        when(moveFrag) {
            GRN_FRAG -> displayFragment(VegaGhanaGrnDetailsFragment.newInstance(item,false), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaGhanaGrnOfflineSummary.newInstance(), true)

        }
    }

    override fun replaceQualityDetailsFragment(
        moveFrag: String,
        wbDetails: VegaGrnWeighBridgeId,
        approveQualityList: ArrayList<VegaGRNGhanaQuality>,
         admixtureValue: String,
        unitHead: String
    ) {
        when(moveFrag) {
            GRN_QUALITY_DETAILS ->
                displayFragment(VegaGhanaGRNQualityFragment.newInstance(wbDetails,approveQualityList,admixtureValue,unitHead), true)
        }
    }



    override fun replaceQualityFragment(moveFrag: String, item: VegaGrnWeighBridgeId) {
        val manager: FragmentManager = supportFragmentManager
        if (manager.backStackEntryCount > 0) {
            val first: FragmentManager.BackStackEntry = manager.getBackStackEntryAt(0)
            manager.popBackStack(first.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        displayFragment(VegaGhanaGrnDetailsFragment.newInstance(item,true), false)

    }


}
