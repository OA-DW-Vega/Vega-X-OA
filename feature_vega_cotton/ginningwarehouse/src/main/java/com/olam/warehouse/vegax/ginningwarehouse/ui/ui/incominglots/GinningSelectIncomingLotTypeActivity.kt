package com.olam.warehouse.ginning.ui.incominglots

import android.content.Intent
import android.os.Bundle
import com.olam.warehouse.ginning.ui.incominglots.seedcotton.GinningIncomingLotActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningIncomingLOTsFeature
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningIncomingLOTsIncomingMtnFeature
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn.GinningIncomingMtnActivity
import kotlinx.android.synthetic.main.activity_ginning_select_incoming_lot_type.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningSelectIncomingLotTypeActivity : HomeBaseActivity() {

    override val layoutResourceId = R.layout.activity_ginning_select_incoming_lot_type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGinningIncomingLOTsIncomingMtnFeature()
        injectGinningIncomingLOTsFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/incominglots/GinningSelectIncomingLotTypeActivity")
            .title("Ginningwarehouse").with(tracker)

    }

    private fun initUI() {
        cvSeedCotton.setOnClickListener {
            startActivity(Intent(this, GinningIncomingLotActivity::class.java))
        }
        cvIncoming_mtn.setOnClickListener {
            startActivity(Intent(this, GinningIncomingMtnActivity::class.java))
        }
    }
}
