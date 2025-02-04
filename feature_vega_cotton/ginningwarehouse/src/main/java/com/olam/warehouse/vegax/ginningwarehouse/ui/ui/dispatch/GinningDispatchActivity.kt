package com.olam.warehouse.ginning.ui.dispatch

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.ginning.di.injectGinningDispatchFeature
import com.olam.warehouse.ginning.utils.FRGA_DISPATCH_ADD_BALE
import com.olam.warehouse.ginning.utils.FRGA_DISPATCH_CONFIRM
import com.olam.warehouse.ginning.utils.FRGA_DISPATCH_OFFLINE
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.ginningwarehouse.R


/**
 * Created by Baskaran Kannan on 20-03-2020.
 */

class GinningDispatchActivity : HomeBaseActivity(),
    GinningDispatchAddBaleFragment.CallBack,
    GinningOfflineDispatchFragment.CallBack {

    private val mTAG = GinningDispatchActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_ginning_dispatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGinningDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(GinningDispatchAddBaleFragment.newInstance(""), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flDispatch, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String, deliveryNo: String) {
        when (moveFrag) {
            FRGA_DISPATCH_CONFIRM -> displayFragment(
                GinningDispatchConfirmFragment.newInstance(
                    deliveryNo
                ), true
            )
            FRGA_DISPATCH_ADD_BALE -> displayFragment(
                GinningDispatchAddBaleFragment.newInstance(
                    deliveryNo
                ), false
            )
            FRGA_DISPATCH_OFFLINE -> displayFragment(
                GinningOfflineDispatchFragment.newInstance(
                    deliveryNo
                ), true
            )
        }
    }

}
