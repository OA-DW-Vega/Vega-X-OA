package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.di.injectEcuadorBcApproveFeature
import com.olam.warehouse.vegax.bcapproveecuador.utils.*


/**
 * Created by Keerthi Santhanam on 7/12/2020.
 */

class VegaEcuadorBcApproveActivity : HomeBaseActivity(), VegaEcuadorBcApproveListFragment.CallBack,
    VegaEcuadorBcApproveListDetailsFragment.QualityDetailsCallBack, VegaEcuadorBCApproveWeighmentTypeFragment.CallBack,
    VegaEcuadorBCApproveWBListFragment.CallBack, VegaEcuadorBcApproveWBListDetailsFragment.Callback {
    override val layoutResourceId = R.layout.activity_vega_ecuador_bcapprove
    private val mTAG = VegaEcuadorBcApproveActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectEcuadorBcApproveFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
//        displayFragment(VegaEcuadorBcApproveListFragment.newInstance(), true)
        displayFragment(VegaEcuadorBCApproveWeighmentTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorDispatch, allowBackStack = flag)
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
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorDispatch)
        when (fragment) {
            is VegaEcuadorBcApproveListFragment -> {
                finish()
            }
            else -> super.onBackPressed()
        }
    }

    private fun showCancelDialog() {
        MaterialDialog(this).show {
            message(R.string.cancel_dispatch)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.login.R.string.confirm),
                getString(com.olam.warehouse.login.R.string.cancel),
                { moveToHomePage() },
                { dismiss() })
        }
    }

    private fun moveToHomePage() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun replaceFragment(fragment: String, item: VegaEcuadorBcApproveWBDetails) {
        when (fragment) {
            LIST_DETAILPAGE -> displayFragment(
                VegaEcuadorBcApproveListDetailsFragment.newInstance(item),
                true
            )
            LIST__WB_DETAILPAGE -> displayFragment(
                VegaEcuadorBcApproveWBListDetailsFragment.newInstance(item),
                true
            )
        }
    }

    override fun replaceQualityDetailsFragment(
        fragment: String,
        approveQualityList: ArrayList<VegaEcuadorApproveWbDetail>
    ) {
        when (fragment) {
            GRN_QUALITY_DETAILS -> displayFragment(
                VegaEcuadorApproveQualityFragment.newInstance(approveQualityList),
                true
            )
        }
    }

    override fun replaceFragment(weighmentType: String) {
        when (weighmentType) {
            WEIGHSCALE -> displayFragment(VegaEcuadorBcApproveListFragment.newInstance(), true)
            WEIGHBRIDGE -> displayFragment(VegaEcuadorBCApproveWBListFragment.newInstance(), true)
        }
    }

    override fun replaceQualityFragment(
        moveFrag: String,
        approveQualityList: ArrayList<VegaEcuadorApproveWbDetail>
    ) {
        when (moveFrag) {
            BC_QUALITY_DETAILS -> displayFragment(
                VegaEcuadorApproveQualityFragment.newInstance(approveQualityList),
                true
            )
        }
    }

    override fun replaceSummaryFragment(
        moveFrag: String,
        approveQualityList: ArrayList<VegaEcuadorApproveWbDetail>,
        wbDetails: VegaEcuadorBcApproveWBDetails
    ) {
        when (moveFrag) {
            BC_QUALITY_DETAILS -> displayFragment(
                VegaEcuadorApproveQualityFragment.newInstance(approveQualityList),
                true
            )
            BC_SUMMARY_DETAILS -> displayFragment(
                VegaEcuadorApproveWBSummaryFragment.newInstance(approveQualityList,wbDetails),
                true
            )
        }
    }



}
