package com.olam.warehouse.login.ui.common

import android.os.Bundle
import android.os.Parcelable
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.navigation.features.DOQualityFeatureNavigation
import com.olam.warehouse.navigation.features.VegaCommonQualityOfAnyLot
import com.olam.warehouse.navigation.features.VegaGhanaCashewQualityNavigation
import com.olam.warehouse.navigation.features.VegaNotificationConfig
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

            ( (currentKey.contains("VEGA_GH") && currentKey.contains("CASH")) ||
                    (currentKey.contains("VEGA_NG") && currentKey.contains("COCO")))  -> {
                when {
                    mNavModule?.equals("QUALITY", true) == true -> {
                        VegaGhanaCashewQualityNavigation.dynamicStart?.let {
                            it.putExtra(Constants.NAV_BUNDLE, mNavBundle)
                            it.putExtra(Constants.WEIGHBRIDGETYPE, WEIGHBRIDGETYPE)
                            it.putExtra(Constants.ISDIRECT, true)
                            startActivity(it)
                            finish()
                        }
                    }
                    mNavModule?.equals("ANY_LOT_QUALITY", true) == true -> {
                        VegaCommonQualityOfAnyLot.dynamicStart?.let {
                            startActivity(it)
                            finish()
                        }
                    }
                    mNavModule?.equals(Constants.NOTIFICATION_CONFIG, true) == true -> {
                        VegaNotificationConfig.dynamicStart?.let {
                            startActivity(it)
                            finish()
                        }

                    }

                }
            }
            (currentKey.contains("DO_GH") && currentKey.contains("CASH")) -> {
                when {
                    mNavModule?.contains("DOQUALITY", true) == true -> {
                        DOQualityFeatureNavigation.dynamicStart?.let {
                            it.putExtra(Constants.NAV_BUNDLE, mNavBundle)
                            it.putExtra(Constants.WEIGHBRIDGETYPE, WEIGHBRIDGETYPE)
                            println("==============================$WEIGHBRIDGETYPE -")
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
