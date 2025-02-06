package com.olam.warehouse.vegax.qualityapprovecameroon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.reprint.VegaQualityApproveReprintReceiptFragment
import com.olam.warehouse.vegax.qualityapprovecameroon.R
import com.olam.warehouse.vegax.qualityapprovecameroon.di.injectQualityApproveCameroonFeature
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.quality.VegaQualityApproveCameroonFragment
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.weighbridge.VegaQualityApproveCameroonWeighbridgeListFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

class VegaQualityApproveCameroonActivity : HomeBaseActivity(), OnFragmentQualityApproveCameroonInteractionListener,
    VegaQualityApproveCameroonFragment.OnParamsListener {


    override fun onFragmentInteraction(fragment: Fragment) {
        displayFragment(fragment, true)
    }



    private var qualityParameter = ArrayList<VegaQualityParameter?>()
    private var finalApprovalStatus: String = ""
    private var wbDetails: VegaQualityWBDetails? = VegaQualityWBDetails()
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private val vm: VegaQualityApproveCameroonViewModel by viewModel()

    private val mTAG = VegaQualityApproveCameroonActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_qualty_approve_cameroon //To change initializer of created properties use File | Settings | File Templates.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectQualityApproveCameroonFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        if(intent.hasExtra(UIUtils.REPRINT_QA_RECEIPT)){
            displayFragment(VegaQualityApproveReprintReceiptFragment.newInstance("Receipt"), false)
        } else if(intent.hasExtra(UIUtils.REPRINT_QA_TICKET)){
            displayFragment(VegaQualityApproveReprintReceiptFragment.newInstance("Ticket"), false)
        }else {
            displayFragment(VegaQualityApproveCameroonWeighbridgeListFragment(), false)
        }
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



