package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.di.injectPortDispatchFeaturee
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch.MtnDispatchAddBaleFragment
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch.MtnDispatchConfirmFragment
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */

class DispatchSelectionActivity : HomeBaseActivity(), DispatchSelectionFragment.CallBack,
    MtnDispatchAddBaleFragment.CallBack {

    private val mTAG = DispatchSelectionActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_port_dispatch_selection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectPortDispatchFeaturee()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/dispatch/DispatchSelectionActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initUI() {
        displayFragment(DispatchSelectionFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flDispatch,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            PortWHUtil.FRAG_OT_DISPATCH -> startActivity(Intent(this, PortDispatchActivity::class.java))
            PortWHUtil.FRAG_MTN_DISPATCH -> displayFragment(MtnDispatchAddBaleFragment.newInstance(""), true)
        }
    }

    override fun replaceFragment(moveFrag: String, deliveryNo: String) {
        when (moveFrag) {
            PortWHUtil.FRGA_DISPATCH_CONFIRM -> displayFragment(
                MtnDispatchConfirmFragment.newInstance(
                    deliveryNo
                ), true
            )
            PortWHUtil.FRGA_DISPATCH_ADD_BALE -> displayFragment(
                MtnDispatchAddBaleFragment.newInstance(
                    deliveryNo
                ), true
            )
        }
    }

}
