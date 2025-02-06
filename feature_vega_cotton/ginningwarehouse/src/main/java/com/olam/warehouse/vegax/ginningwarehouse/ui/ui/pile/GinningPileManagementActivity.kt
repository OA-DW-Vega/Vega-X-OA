package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.GinningPileStorageLocationModel
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningPileFeature

class GinningPileManagementActivity : HomeBaseActivity(),
    GinningPileAddBaleFragment.CallBack {

    private val mTAG = GinningPileManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int =
        R.layout.activity_gining_pile_mgnt_layout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       injectGinningPileFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(GinningPileAddBaleFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPile, allowBackStack = flag)
    }

    override fun replaceConfirmFragment(storageId: GinningPileStorageLocationModel) {
        displayFragment(GinningPileConfirmFragment.newInstance(storageId), true)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flPile)
        when (fragment) {
            is GinningPileAddBaleFragment -> {
                //fragment.applyFilterValues(mCurrentPiles.storageLocationCode)
            }
        }
        super.onBackPressed()
    }
}
