package com.olam.warehouse.vegax.dummyquality.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.dummyquality.R
import com.olam.warehouse.vegax.dummyquality.di.injectDummyQualityFeature
import com.olam.warehouse.vegax.dummyquality.utils.DUMMY_QUALITY_CAPTURE
import com.olam.warehouse.vegax.dummyquality.utils.DUMMY_QUALITY_CAPTURE_ACCEPT
import com.olam.warehouse.vegax.dummyquality.utils.DUMMY_QUALITY_CAPTURE_FINISH
import com.olam.warehouse.vegax.dummyquality.utils.DUMMY_QUALITY_LIST

/**
 * Created by Ramesh Rm on 9/21/2022.
 */
class VegaCommonDummyQualityActivity : HomeBaseActivity(),
    VegaCommonDummyQualityOptionFragment.CallBack,
    VegaCommonDummyQualityCaptureFragment.CallBack ,
    VegaCommonDummyQualityAcceptFragment.CallBack,
    VegaCommonDummySampleListFragment.CallBack{

    private val mTAG = VegaCommonDummyQualityActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_dummy_quality


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDummyQualityFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCommonDummyQualityOptionFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.frameLayoutDummyQuality,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(fragmentName: String) {
        when (fragmentName) {
            DUMMY_QUALITY_CAPTURE -> displayFragment(
                VegaCommonDummyQualityCaptureFragment.newInstance(
                ), true
            )
            DUMMY_QUALITY_CAPTURE_FINISH ->{
                finish()
            }
        }
    }

    override fun replaceFragment(moveFrag: String, supplierCode: String, supplierName: String, materialName: String,materialCode: String,id:String) {
        when (moveFrag) {
            DUMMY_QUALITY_CAPTURE_ACCEPT -> displayFragment(
                VegaCommonDummyQualityAcceptFragment.newInstance(
                    supplierCode,
                    supplierName,
                    materialName,
                    materialCode,
                    id
                ), true
            )
            DUMMY_QUALITY_LIST ->displayFragment(
                VegaCommonDummySampleListFragment.newInstance(
                    supplierCode,
                    supplierName,
                    materialName,
                    materialCode,
                    id
                ), true
            )
        }
    }
}
