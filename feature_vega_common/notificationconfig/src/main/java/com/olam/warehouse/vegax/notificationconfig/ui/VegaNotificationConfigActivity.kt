package com.olam.warehouse.vegax.notificationconfig.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.notificationconfig.R
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserDetails
import com.olam.warehouse.vegax.notificationconfig.di.injectNotificationConfigFeature
import com.olam.warehouse.vegax.notificationconfig.ui.callback.VegaNotificationConfigCallbackListener
import com.olam.warehouse.vegax.notificationconfig.utils.MODULE_LIST_FRAGMENT

class VegaNotificationConfigActivity : HomeBaseActivity(), VegaNotificationConfigCallbackListener {

    private val mTAG = VegaNotificationConfigActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_notification_config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vega_notification_config)
        injectNotificationConfigFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        /*By default, loding the selection fragment*/
        displayFragment(VegaNotificationConfigUserListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flNotifyConfig,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            MODULE_LIST_FRAGMENT -> displayFragment(VegaNotificationConfigModuleListFragment.newInstance(data as VegaNotifyConfigUserDetails), true)
        }
    }
}
