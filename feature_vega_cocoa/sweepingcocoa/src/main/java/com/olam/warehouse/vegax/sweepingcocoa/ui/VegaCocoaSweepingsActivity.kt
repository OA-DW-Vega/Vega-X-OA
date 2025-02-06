package com.olam.warehouse.vegax.sweepingcocoa.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.sweepingcocoa.R
import com.olam.warehouse.vegax.sweepingcocoa.di.injectCocoaSweepingFeature
import com.olam.warehouse.vegax.sweepingcocoa.ui.summary.VegaSweepingSummaryFragment
import com.olam.warehouse.vegax.sweepingcocoa.ui.weightentry.VegaSweepingPalletFragment
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 5/27/2020.
 */
class VegaCocoaSweepingsActivity : HomeBaseActivity(), CallBack, VegaSweepingWeightEntryFragment.CallBackAddBags,
                                   VegaCocoaAddPalletFragment.CallBackPallet {
    override val layoutResourceId = R.layout.activity_vega_cocoa_sweepings
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCocoaSweepingFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("sweepingcocoa/ui/VegaCocoaSweepingsActivity")
            .title("Sweeping Cocoa")
            .with(tracker)
    }

    
    private fun initUI() {
        displayFragment(VegaSweepingPalletFragment.newInstance(), false)
        initBt(object : BtObserve {
            override fun valueObserve(btValue: String) {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaSweepingPalletFragment -> {
                        val fragment1 = VegaCocoaAddPalletFragment()
                        fragment1.updateBtWeight(btValue)
                    }
                    is VegaSweepingWeightEntryFragment -> {
                        fragment.updateBtWeight(btValue)
                    }
                }
            }

        })
    }


    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flContainer,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            "AddWeight" -> displayFragment(VegaSweepingWeightEntryFragment.newInstance(data as Bundle, true), true)
            "Summary" -> displayFragment(VegaSweepingSummaryFragment.newInstance(data as Bundle), true)
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaSweepingPalletFragment -> {
                fragment.updateBagWeight(bagMaterial)
            }
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaSweepingPalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
