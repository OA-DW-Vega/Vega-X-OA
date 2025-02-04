package com.olam.warehouse.portwarehouse.ui.inventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleMark
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.CropYears
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PileModel
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.di.injectPortInventoryFeaturee
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/13/2020.
 */
class InventoryActivity : HomeBaseActivity(),
    InventoryGradeListFragment.CallBack,
    InventoryPileListFragment.CallBack {

    private var mMarkList = ArrayList<BaleMark>()
    private var mYearList = ArrayList<CropYears>()
    private var mPileList = ArrayList<PileModel>()

    override val layoutResourceId = R.layout.activity_inventory
    private val mTAG = InventoryActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectPortInventoryFeaturee()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/inventory/InventoryActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initUI() {
        displayFragment(InventoryPileListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flInventory,
            allowBackStack= flag
        )
    }

    override fun replaceFragment(
        moveFrag: String,
        grades: ArrayList<String>,
        gradeList: MutableList<BaleGrade>
    ) {
        displayFragment(
            InventoryBaleListFragment.newInstance(
                grades,
                gradeList,
                mMarkList,
                mYearList,
                mPileList
            ), true
        )
    }

    override fun replaceFragment(piles: ArrayList<PileModel>,
        markList: ArrayList<BaleMark>,
        yearList: ArrayList<CropYears>
    ) {
        mMarkList = markList
        mYearList = yearList
        mPileList = piles
        displayFragment(InventoryGradeListFragment.newInstance(piles), true)
    }
}
