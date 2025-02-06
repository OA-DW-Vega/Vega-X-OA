package com.olam.warehouse.login.ui.settings

import android.os.Bundle
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class SettingsActivity : HomeBaseActivity() {

    override val layoutResourceId = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        replaceFragment(SettingsFragment(), "", allowStateLoss = true, containerViewId = R.id.flSettings)
    }
}
