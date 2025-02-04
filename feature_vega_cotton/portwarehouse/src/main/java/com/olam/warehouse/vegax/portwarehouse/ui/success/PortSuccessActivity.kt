package com.olam.warehouse.vegax.portwarehouse.ui.success

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SUB_TITLE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.TITLE
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.activity_port_success.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by SangiliPandian C on 09-03-2020.
 */

class PortSuccessActivity : HomeBaseActivity() {

    override val layoutResourceId = R.layout.activity_port_success

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/success/PortSuccessActivity")
            .title("Portwarehouse").with(tracker)
    }

    override fun onBackPressed() {
        moveToHomePage()
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    moveToHomePage()
                }
            }
        }
        return false
    }

    private fun initUI() {
        val title = intent?.getStringExtra(TITLE)
        val subTitle = intent?.getStringExtra(SUB_TITLE)
        tvTitle.text = title
        tvSubTitle.text = subTitle
        btnOk.setOnClickListener { moveToHomePage() }
    }

    private fun moveToHomePage() {
        val intent = Intent(this, com.olam.warehouse.login.ui.home.HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
