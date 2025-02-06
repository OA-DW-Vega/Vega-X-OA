package com.olam.warehouse.vegax.receiving.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receiving.R
import com.olam.warehouse.vegax.receiving.di.injectVegaReceivingFeature
import com.olam.warehouse.vegax.receiving.ui.truckin.VegaTruckInListFragment
import com.olam.warehouse.vegax.receiving.ui.truckin.VegaTruckInSummaryFragment
import com.olam.warehouse.vegax.receiving.ui.truckin.mtnr.VegaMtnrFragment
import com.olam.warehouse.vegax.receiving.ui.truckin.mtnt.VegaTruckInMtntFragment
import com.olam.warehouse.vegax.receiving.ui.truckin.mtnt.VegaTruckInMtntSummaryFragments
import com.olam.warehouse.vegax.receiving.ui.truckin.supplier.VegaSupplierFragment
import com.olam.warehouse.vegax.receiving.ui.truckout.VegaTruckOutAddWeightAndBagFragment
import com.olam.warehouse.vegax.receiving.ui.truckout.VegaTruckOutSummaryFragment
import com.olam.warehouse.vegax.receiving.ui.truckout.VegaTruckOutWBListFragment
import com.olam.warehouse.vegax.receiving.ui.truckout.mtnt.VegaTruckOutMtntAddWeightFragment
import com.olam.warehouse.vegax.receiving.ui.truckout.mtnt.VegaTruckOutMtntSummaryFragment
import com.olam.warehouse.vegax.receiving.ui.truckout.mtnt.VegaTruckOutMtntWBListFragment
import com.olam.warehouse.vegax.receiving.ui.weighment.VegaWeighmentTypeFragment
import com.olam.warehouse.vegax.receiving.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/18/2019.
 */
class VegaReceivingActivity : HomeBaseActivity(), VegaWeighmentTypeFragment.CallBack,
    VegaSupplierFragment.CallBack,
    VegaMtnrFragment.CallBack,
    VegaTruckInSummaryFragment.CallBack,
    VegaTruckOutWBListFragment.CallBack,
    VegaTruckOutAddWeightAndBagFragment.CallBack,
    VegaTruckOutSummaryFragment.CallBack,
    VegaTruckInMtntFragment.CallBack,
    VegaTruckOutMtntWBListFragment.CallBack,
    VegaTruckOutMtntAddWeightFragment.CallBack,
    VegaTruckInListFragment.CallBack {

    private val mTAG = VegaReceivingActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_vega_receiving

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaReceivingFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/VegaReceivingActivity").title("Receiving").with(tracker)
    }

    private fun initUI() {
        if (intent.hasExtra(UIUtils.RECEIVING_DATA)) {
            var receivingData = VegaReceiving()
            var postData = ArrayList<VegaReceivingLineItem>()
            receivingData = intent.getParcelableExtra(UIUtils.RECEIVING_DATA)!!
            postData = intent.getParcelableArrayListExtra(UIUtils.RECEIVING_POST_DATA)!!
            when (receivingData.truckDirection) {
                DIRECTIONIN -> {
                    when (receivingData.status) {
                        Status.RECEVING_COMPLETED -> displayFragment(
                            VegaTruckInSummaryFragment.newInstance(
                                receivingData
                            ), false
                        )
                        else -> {
                            if (receivingData.weighBridgeType == PROCURE)
                                displayFragment(VegaSupplierFragment.newInstance(receivingData), false)
                            else
                                displayFragment(VegaMtnrFragment.newInstance(receivingData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (receivingData.status) {
                        Status.RECEVING_COMPLETED -> displayFragment(
                            VegaTruckOutSummaryFragment.newInstance(
                                receivingData,
                                getLineItemFromReceivingLineItem(postData)
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaTruckOutAddWeightAndBagFragment.newInstance(
                                    receivingData,
                                    getLineItemFromReceivingLineItem(postData)
                                ), false
                            )
                        }
                    }
                }
            }
        } else if (intent.hasExtra(UIUtils.MTNT_DATA)) {
            var mtntData = VegaMtnt()
            var postData = ArrayList<VegaMtntLineItem>()
            mtntData = intent.getParcelableExtra(UIUtils.MTNT_DATA)!!
            postData = intent.getParcelableArrayListExtra(UIUtils.MTNT_POST_DATA)!!
            when (mtntData.truckDirection) {
                DIRECTIONIN -> {
                    when (mtntData.status) {
                        Status.MTNT_COMPLETED -> displayFragment(
                            VegaTruckInMtntSummaryFragments.newInstance(
                                mtntData
                            ), false
                        )
                        else -> {
                            displayFragment(VegaTruckInMtntFragment.newInstance(mtntData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (mtntData.status) {
                        Status.MTNT_COMPLETED -> displayFragment(
                            VegaTruckOutMtntSummaryFragment.newInstance(
                                mtntData,
                                getMtntFromMtntLineItem(postData)
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaTruckOutMtntAddWeightFragment.newInstance(
                                    mtntData,
                                    getMtntFromMtntLineItem(postData)
                                ), false
                            )
                        }
                    }
                }
            }
        } else
            displayFragment(VegaWeighmentTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, allowStateLoss = true, containerViewId = R.id.flReceive, allowBackStack = flag)
    }

    override fun replaceFragment(receivingType: String, direction: String, receivingData: VegaReceiving) {
        if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONIN) {
            /*if (receivingType == SUPPLIER) displayFragment(VegaSupplierFragment.newInstance(receivingData), true)
            else displayFragment(VegaMtnrFragment.newInstance(receivingData), true)*/
            displayFragment(VegaTruckInListFragment.newInstance(receivingData), true)
        } else if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONOUT) {
            displayFragment(VegaTruckOutWBListFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>
    ) {
        if (moveFrag == TRUCKOUT_ADD_WEIGHT_FRAG)
            displayFragment(
                VegaTruckOutAddWeightAndBagFragment.newInstance(
                    receivingData,
                    arrayListOf<VegaReceiving>()
                ), true
            )
        else if (moveFrag == TRUCKOUT_SUMMARYT_FRAG)
            displayFragment(
                VegaTruckOutSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>
                ), true
            )
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaReceiving) {
        when (moveFrag) {
            TRUCKOUT_ADD_WEIGHT_FRAG -> displayFragment(
                VegaTruckOutAddWeightAndBagFragment.newInstance(
                    receivingData,
                    arrayListOf<VegaReceiving>()
                ),
                true
            )
            TRUCKIN_SUMMARYT_FRAG -> displayFragment(VegaTruckInSummaryFragment.newInstance(receivingData), true)
            SUPPLIER_FRAG -> displayFragment(VegaSupplierFragment.newInstance(receivingData), true)
            MTNR_FRAG -> displayFragment(VegaMtnrFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceMtntFragment(direction: String, mtntData: VegaMtnt) {
        if (direction == DIRECTIONIN) displayFragment(VegaTruckInMtntFragment.newInstance(mtntData), true)
        else displayFragment(VegaTruckOutMtntWBListFragment.newInstance(mtntData), true)
    }


    override fun replaceMtntFragment(flag: String, moveFrag: String, mtntData: VegaMtnt) {
        if (moveFrag == TRUCKIN_SUMMARYT_FRAG)
            displayFragment(VegaTruckInMtntSummaryFragments.newInstance(mtntData), true)
        else if (moveFrag == TRUCKOUT_MTNT_ADD_WEIGHT_FRAG)
            displayFragment(VegaTruckOutMtntAddWeightFragment.newInstance(mtntData, arrayListOf<VegaMtnt>()), true)
    }

    override fun replaceMtntFragment(moveFrag: String, mtntData: VegaMtnt, mMtntList: MutableList<VegaMtnt>) {
        displayFragment(VegaTruckOutMtntSummaryFragment.newInstance(mtntData, mMtntList as ArrayList<VegaMtnt>), true)
    }
}
