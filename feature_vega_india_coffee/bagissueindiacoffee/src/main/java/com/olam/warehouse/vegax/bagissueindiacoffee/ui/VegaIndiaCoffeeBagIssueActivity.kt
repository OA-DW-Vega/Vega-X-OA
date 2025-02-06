package com.olam.warehouse.vegax.bagissueindiacoffee.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.bagissueindiacoffee.R
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssue
import com.olam.warehouse.vegax.bagissueindiacoffee.di.injectIndiaCoffeeBagIssueFeature
import com.olam.warehouse.vegax.bagissueindiacoffee.utils.ADD_CONTAINER
import com.olam.warehouse.vegax.bagissueindiacoffee.utils.CONTAINER_INVENTORY
import com.olam.warehouse.vegax.bagissueindiacoffee.utils.SUMMARY_FRAG

class VegaIndiaCoffeeBagIssueActivity : HomeBaseActivity(),VegaIndiaCoffeeBagIssueFragment.CallBack
{

    private val mTAG = VegaIndiaCoffeeBagIssueActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_india_coffee_bag_issue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndiaCoffeeBagIssueFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaIndiaCoffeeBagIssueFragment.newInstance(), false)
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

    override fun replaceFragment(paramsListFrag: String, data: VegaIndiaCoffeeBagIssue) {
        if (paramsListFrag == SUMMARY_FRAG) {
            displayFragment(VegaIndiaCoffeeBagIssueSummaryFragment.newInstance(data), true)
        }
    }


}

