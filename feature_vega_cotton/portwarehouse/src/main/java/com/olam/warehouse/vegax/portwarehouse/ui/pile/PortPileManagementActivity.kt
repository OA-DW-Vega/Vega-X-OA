package com.olam.warehouse.vegax.portwarehouse.ui.pile

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.di.injectPortPileFeature
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class PortPileManagementActivity : HomeBaseActivity(), PortPileAddBaleFragment.CallBack {

    private val mTAG = PortPileManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_port_pile_mgnt_layout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectPortPileFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/pile/PortPileManagementActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initUI() {
        displayFragment(PortPileAddBaleFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPile, allowBackStack = flag)
    }

    override fun replaceConfirmFragment(storageId: PortPileStorageLocationModel) {
        displayFragment(PortPileConfirmFragment.newInstance(storageId), true)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flPile)
        when (fragment) {
            is PortPileAddBaleFragment -> {
                //fragment.applyFilterValues(mCurrentPiles.storageLocationCode)
            }
        }
        super.onBackPressed()
    }

}
