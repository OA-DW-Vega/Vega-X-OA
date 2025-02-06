package com.olam.warehouse.login.ui.quickpinaccess

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
class VegaQuickAccessPinActivity : HomeBaseActivity() {
    override val layoutResourceId = R.layout.activity_quick_access_pin
    private val mTAG = VegaQuickAccessPinActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaQuickAccessPinSelectFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainer,
            allowBackStack = flag
        )
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
        super.onBackPressed()
    }
}
