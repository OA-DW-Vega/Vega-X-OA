package com.olam.warehouse.vegax.secretid.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.data.domain.model.VegaCameroonSecretId
import com.olam.warehouse.vegax.secretid.di.injectCameroonSecretIdFeature

class VegaCameroonSecretIdActivity : HomeBaseActivity(), VegaCameroonSecretIdListFragment.CallBack {
    private val mTAG = VegaCameroonSecretIdActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_cameroon_secretid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonSecretIdFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaCameroonSecretIdListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flSecretId, allowBackStack = flag)
    }

    override fun replaceQualityFragment(paramsFrag: String, item: VegaCameroonSecretId) {
        val bundle = Bundle().apply {
            putParcelable("LOT_LIST", item)
        }
        var fragment = VegaCameroonSecretIdSummaryFragment.newInstance()

        bundle.let { fragment.arguments = bundle }
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flSecretId,
            allowBackStack = true
        )
    }
}
