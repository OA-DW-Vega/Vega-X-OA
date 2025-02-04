package com.olam.warehouse.odreceiving.ui.summary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_POST_DATA
import com.olam.warehouse.odreceiving.utils.WS01
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingSummaryActivity : HomeBaseActivity() {

    private var postData: ArrayList<DOReceiving>? = null
    private var postDataOffline: ArrayList<DOReceivingLineItem>? = null
    private val mTAG = DOReceivingSummaryActivity::class.java.canonicalName
    private var receivingData = DOReceiving()
    private var doTxnDetail = DOTxnDetail()
    private var isBltEnabled: Boolean = false
    private var scanLevelId: Int = 0

    override val layoutResourceId = R.layout.activity_do_receiving_summary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/summary/DOReceivingSummaryActivity").title("OD/Receiving")
            .with(tracker)
    }

    private fun initExtras() {
        receivingData = intent.getParcelableExtra(RECEIVING_DATA)!!
        if(intent.extras?.containsKey("doTxnDetail")!!)
            doTxnDetail = intent.getParcelableExtra("doTxnDetail")!!
        Log.i("receivingIntent", receivingData.toString())
        postData = intent.getParcelableArrayListExtra<DOReceiving>(RECEIVING_POST_DATA)
        postDataOffline = intent.getParcelableArrayListExtra<DOReceivingLineItem>("postDataOffline")
        isBltEnabled = intent.getBooleanExtra("isBltEnabled", false)
        scanLevelId = intent.getIntExtra("scanLevelId", 0)
    }

    private fun initUI() {
        if (!receivingData.mtnCode.isNullOrEmpty()) {
            displayFragment(DOReceivingSummaryMtnrFragment.newInstance(receivingData, postData))
            return
        }
        when (receivingData.wsGate) {
            WS01 -> displayFragment(DOReceivingSummaryWeighScaleFragment.newInstance(receivingData, postData, isBltEnabled, scanLevelId, postDataOffline,doTxnDetail))
            else -> displayFragment(DOReceivingSummaryWeighScalePalletFragment.newInstance(receivingData, postData))
        }
    }

    private fun displayFragment(fragment: Fragment) {
        replaceFragment(fragment, mTAG, true, R.id.flSummary)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
