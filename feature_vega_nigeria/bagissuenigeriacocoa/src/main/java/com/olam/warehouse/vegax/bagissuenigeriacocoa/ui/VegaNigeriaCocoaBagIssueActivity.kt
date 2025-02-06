package com.olam.warehouse.vegax.bagissuenigeriacocoa.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.bagissuenigeriacocoa.R

import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssue
import com.olam.warehouse.vegax.bagissuenigeriacocoa.di.injectNigeriaCocoaBagIssueFeature
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.BAG_ISSUE_FRAG
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.BAG_RETN_FRAG
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.SUMMARY_FRAG

class VegaNigeriaCocoaBagIssueActivity : HomeBaseActivity(), VegaNigeriaCocoaBagMgmtCallBackListener {


    private val mTAG = VegaNigeriaCocoaBagIssueActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nigeria_cocoa_bag_issue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaCocoaBagIssueFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaCocoaMenuSelectFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainerManagement,
            allowBackStack = flag
        )
    }

    /* override fun replaceFragment(paramsListFrag: String, data: VegaNigeriaCocoaBagIssue) {
         if (paramsListFrag == SUMMARY_FRAG) {
             displayFragment(VegaNigeriaCocoaBagIssueSummaryFragment.newInstance(data), true)
         } else if(paramsListFrag == BAG_ISSUE_FRAG){
             displayFragment(VegaNigeriaCocoaBagIssueFragment.newInstance(), true)
         } else if(paramsListFrag == BAG_RETN_FRAG){
             displayFragment(VegaNigeriaCocoaBagIssueFragment.newInstance(), true)
         }
     }*/

    override fun replaceFragment(paramsListFrag: String, data: Any) {
        if (paramsListFrag == SUMMARY_FRAG) {
            displayFragment(VegaNigeriaCocoaBagIssueSummaryFragment.newInstance(data as VegaNigeriaCocoaBagIssue), true)
        } else if (paramsListFrag == BAG_ISSUE_FRAG) {
            displayFragment(VegaNigeriaCocoaBagIssueFragment.newInstance(data as String), true)
        } else if (paramsListFrag == BAG_RETN_FRAG) {
            displayFragment(VegaNigeriaCocoaBagIssueFragment.newInstance(data as String), true)
        }
    }

}
