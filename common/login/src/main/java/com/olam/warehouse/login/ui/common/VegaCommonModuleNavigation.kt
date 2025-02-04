package com.olam.warehouse.login.ui.common

import android.os.Bundle
import android.os.Parcelable
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.navigation.features.VegaGhanaCashewQualityNavigation
import com.olam.warehouse.presentation.utils.Constants

/**
 * Created by Baskaran Kannan on 8/13/2021.
 */
class VegaCommonModuleNavigation : HomeBaseActivity() {

    private var mNavModule: String? = ""
    private var WEIGHBRIDGETYPE: String? = ""
    private var mNavBundle: Parcelable? = null
    override val layoutResourceId = R.layout.activity_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initExtra()
        initUI()

    }

    private fun initExtra() {
        mNavModule = intent.getStringExtra(Constants.NAV_MODULE) ?: ""
        WEIGHBRIDGETYPE = intent.getStringExtra(Constants.WEIGHBRIDGETYPE) ?: ""
        mNavBundle = intent.getBundleExtra(Constants.NAV_BUNDLE) ?: Bundle()

    }

    private fun initUI() {
        val currentKey = getCurrentKey()
        when {
            currentKey.contains("VEGA_GH_CASH") -> {
                when {
                    mNavModule?.contains("QUALITY", true) == true -> {
                        VegaGhanaCashewQualityNavigation.dynamicStart?.let {
                            it.putExtra(Constants.NAV_BUNDLE, mNavBundle)
                            it.putExtra(Constants.WEIGHBRIDGETYPE, WEIGHBRIDGETYPE)
                            it.putExtra(Constants.ISDIRECT, true)
                            startActivity(it)
                            finish()
                        }
                    }
                }
            }
        }
    }


}
