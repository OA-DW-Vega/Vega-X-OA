package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots

import android.content.Intent
import android.os.Bundle
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningSelectIncomingLotTypeBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningIncomingLOTsFeature
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningIncomingLOTsIncomingMtnFeature
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn.GinningIncomingMtnActivity
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.seedcotton.GinningIncomingLotActivity
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningSelectIncomingLotTypeActivity : HomeBaseActivity() {

    override val layoutResourceId = R.layout.activity_ginning_select_incoming_lot_type
    private lateinit var binding: ActivityGinningSelectIncomingLotTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinningSelectIncomingLotTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.cvSeedCotton.setOnClickListener {
            startActivity(Intent(this, GinningIncomingLotActivity::class.java))
        }
        binding.cvIncomingMtn.setOnClickListener {
            startActivity(Intent(this, GinningIncomingMtnActivity::class.java))
        }
    }
}
