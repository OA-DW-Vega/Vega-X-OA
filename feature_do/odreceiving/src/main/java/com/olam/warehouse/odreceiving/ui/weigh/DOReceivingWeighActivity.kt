package com.olam.warehouse.odreceiving.ui.weigh

//import org.matomo.sdk.Tracker
//import org.matomo.sdk.extra.TrackHelper
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.ui.blt.AttachNewQrFragment
import com.olam.warehouse.odreceiving.ui.blt.BagDetailsBltFragment
import com.olam.warehouse.odreceiving.ui.blt.MissingBagsFragment
import com.olam.warehouse.odreceiving.utils.*
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.replaceOtherFragment
import com.olam.warehouse.vegax.App
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingWeighActivity : HomeBaseActivity(), DOReceivingWeighScalePalletAddFragment.CallBack,
    DOReceivingWeighScaleFragment.CallBack, BagDetailsBltFragment.CallBack {

    private var totalScannedWeight: String? = "0.0"
    private var totalScannedBags: Int = 0

    private val mTAG = DOReceivingWeighActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_do_receiving_weigh
    private var receivingData = DOReceiving()
    private var doTxnDetail = DOTxnDetail()
    private var postData: ArrayList<DOReceivingLineItem>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/weigh/DOReceivingWeighActivity")
            .title("OD/Receiving")
            .with(tracker)
    }

    override fun replaceFragment(receivingData: DOReceiving) {
        displayFragment(DOReceivingWeighScalePalletFragment.newInstance(receivingData), true)
    }

    private fun initExtras() {
        receivingData = intent.getParcelableExtra(RECEIVING_DATA)!!
        if(intent.extras?.containsKey("doTxnDetail")!!)
            doTxnDetail = intent.getParcelableExtra("doTxnDetail")!!

        totalScannedWeight = intent.getStringExtra("totalScannedWeight")
        totalScannedBags = intent.getIntExtra("totalScannedBags", 0)

        if (intent.hasExtra(RECEIVING_POST_DATA)) postData = intent.getParcelableArrayListExtra(RECEIVING_POST_DATA)
    }

    private fun initUI() {
        when (receivingData.wsGate) {
            WS01 -> {
                replaceFragment(DOReceivingWeighScaleFragment.newInstance(receivingData, postData, doTxnDetail),
                    "DOReceivingWeighScaleFragment::class.simpleName", allowStateLoss = true, containerViewId = R.id.flWeigh, allowBackStack = false)
//                displayFragment(DOReceivingWeighScaleFragment.newInstance(receivingData, postData, doTxnDetail), false)
            }
            else -> displayFragment(DOReceivingWeighScalePalletAddFragment.newInstance(receivingData, postData), false)
        }
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, allowStateLoss = true, containerViewId = R.id.flWeigh, allowBackStack = flag)
    }

    override fun replaceFragment(
        moveFrag: String,
        totalScannedBags: Int,
        totalScannedWeight: String,
        tagName: String,
        receivingData: DOReceiving) {
        if(moveFrag === BAG_DETAILS) {
            replaceOtherFragment(BagDetailsBltFragment.newInstance(doTxnDetail,
                totalScannedBags, totalScannedWeight, receivingData), tagName, allowStateLoss = true, containerViewId = R.id.flWeigh)
//            displayFragment(BagDetailsBltFragment.newInstance(doTxnDetail), false)
        }
    }

    override fun replaceFragment(
        moveFrag: String, id: String
    ) {
        if (moveFrag === MISSING_BAG) {
            replaceOtherFragment(
                MissingBagsFragment.newInstance(doTxnDetail, id),
                mTAG,
                allowStateLoss = true,
                containerViewId = R.id.flWeigh
            )
//            displayFragment(MissingBagsFragment.newInstance(doTxnDetail), false)
        } else if (moveFrag === ATTACH_NEW_QR) {
            replaceOtherFragment(
                AttachNewQrFragment.newInstance(doTxnDetail, id),
                mTAG,
                allowStateLoss = true,
                containerViewId = R.id.flWeigh
            )
//            displayFragment(AttachNewQrFragment.newInstance(doTxnDetail), false)
        }
    }
}
