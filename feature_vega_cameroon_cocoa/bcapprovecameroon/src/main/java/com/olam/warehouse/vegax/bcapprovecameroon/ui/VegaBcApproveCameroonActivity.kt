package com.olam.warehouse.vegax.bcapprovecameroon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.bcapprovecameroon.R
import com.olam.warehouse.vegax.bcapprovecameroon.di.injectQualityApproveCameroonFeature
import com.olam.warehouse.vegax.bcapprovecameroon.ui.quality.VegaBcApproveCameroonFragment
import com.olam.warehouse.vegax.bcapprovecameroon.ui.weighbridge.VegaBcApproveCameroonWeighbridgeListFragment

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

class VegaBcApproveCameroonActivity : HomeBaseActivity(), OnFragmentBcApproveCameroonInteractionListener,
    VegaBcApproveCameroonFragment.OnParamsListener {
    override fun onFragmentInteraction(fragment: Fragment) {
        displayFragment(fragment, true)
    }


    private val mTAG = VegaBcApproveCameroonActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_bc_approve_cameroon //To change initializer of created properties use File | Settings | File Templates.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectQualityApproveCameroonFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaBcApproveCameroonWeighbridgeListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flApprove, allowBackStack = flag)
    }

    override fun onParamsProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {

    }




}
