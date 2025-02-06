package com.olam.warehouse.vegax.ppqcameroon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.ppqcameroon.R
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLots
import com.olam.warehouse.vegax.ppqcameroon.di.injectCameroonPpqFeature
import com.olam.warehouse.vegax.ppqcameroon.utils.BATCH_NO
import com.olam.warehouse.vegax.ppqcameroon.utils.INSPECTION_LOT

class VegaCameroonPpqActivity : HomeBaseActivity(),
    VegaCameroonPpqLotListFragment.CallBack, VegaCameroonPpqQualityParamsFragment.CallBack,
    VegaCameroonPpqQualitySummaryFragment.OnSummaryParamsListener {

    private val mTAG = VegaCameroonPpqActivity::class.java.canonicalName
    override val layoutResourceId: Int = R.layout.activity_vega_cameroon_ppq

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonPpqFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaCameroonPpqLotListFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flPpq, allowBackStack = flag)
    }

    override fun replaceQualityFragment(paramsFrag: String, item: VegaCameroonPpqInspectionLots) {
        displayFragment(VegaCameroonPpqQualityParamsFragment.newInstance(item), true)
    }

    override fun replaceQualityFragment(wbid: String, batchNo: String, item: VegaCameroonPpqInspectionLotDetails) {
        val bundle = Bundle().apply {
            putParcelable("LOT_LIST", item)
            putString(INSPECTION_LOT, wbid)
            putString(BATCH_NO, batchNo)
        }
        var fragment = VegaCameroonPpqQualitySummaryFragment.newInstance()

        bundle.let { fragment.arguments = bundle }
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flPpq,
            allowBackStack = true
        )

    }

    override fun onParamsProceed(
        qualityParameter: java.util.ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        challan: String
    ) {
        TODO("Not yet implemented")
    }

    override fun onMtnrParamsProceed(
        qualityParameter: java.util.ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        lotItems: java.util.ArrayList<VegaCoffeeLot>
    ) {
        TODO("Not yet implemented")
    }


}
