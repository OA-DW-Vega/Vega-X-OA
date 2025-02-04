package com.olam.warehouse.ginning.ui.inventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.ginning.data.model.BaleGrade
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningInventoryFeature
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/13/2020.
 */
class GinningInventoryActivity : HomeBaseActivity(),
    GinningInventoryGradeListFragment.CallBack {

    override val layoutResourceId = R.layout.activity_inventory_ginning
    private val mTAG = GinningInventoryActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGinningInventoryFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/inventory/GinningInventoryActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initUI() {
        displayFragment(GinningInventoryGradeListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flInventory,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(
        moveFrag: String,
        grades: ArrayList<String>,
        gradeList: ArrayList<BaleGrade>
    ) {
        displayFragment(GinningInventoryBaleListFragment.newInstance(grades, gradeList), true)
    }
}
