package com.olam.warehouse.vegax.secretidnigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.secretidnigeria.R
import com.olam.warehouse.vegax.secretidnigeria.data.domain.model.VegaNigeriaSecretId
import com.olam.warehouse.vegax.secretidnigeria.di.injectNigeriaSecretIdFeature
import com.olam.warehouse.vegax.secretidnigeria.utils.*

class VegaNigeriaSecretIdActivity : HomeBaseActivity(), VegaNigeriaSecretIdListFragment.CallBack, VegaNigeriaReplaceFragmentCallback {
    private val mTAG = VegaNigeriaSecretIdActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_nigeria_secretid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaSecretIdFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        //displayFragment(VegaNigeriaSecretIdListFragment.newInstance(), false)
        displayFragment(VegaNigeriaSecretIdSelectDispatchTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flSecretId, allowBackStack = flag)
    }

    override fun replaceQualityFragment(paramsFrag: String, item: VegaNigeriaSecretId,wbType: String, text: String) {
        val bundle = Bundle().apply {
            putParcelable("LOT_LIST", item)
            putString("TEXT", text)
            putString(WB_TYPE, wbType)
        }
        var fragment = VegaNigeriaSecretIdSummaryFragment.newInstance()

        bundle.let { fragment.arguments = bundle }
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flSecretId,
            allowBackStack = true
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHBRIDGE -> {
                displayFragment(VegaNigeriaSecretIdListFragment.newInstance(WB), false)
            }
            MTNT_WEIGHSCALE -> {
                displayFragment(VegaNigeriaSecretIdListFragment.newInstance(WS), false)
            }
        }
    }

    override fun replaceFragment(receivingType: String, data: Any, list: VegaCocoaDispatchWB) {
        TODO("Not yet implemented")
    }

    override fun replaceFragment(receivingType: String) {
        TODO("Not yet implemented")
    }
}
