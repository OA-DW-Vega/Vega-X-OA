package com.olam.warehouse.vegax.nigeriaweighment.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.nigeriaweighment.R
import com.olam.warehouse.vegax.nigeriaweighment.di.injectVegaReceivingFeature
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckin.VegaNigeriaTruckInListFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckin.VegaNigeriaTruckInSummaryFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckin.mtnr.VegaNigeriaMtnrFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckin.mtnt.VegaNigeriaTruckInMtntFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckin.mtnt.VegaNigeriaTruckInMtntSummaryFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckin.supplier.VegaNigeriaSupplierFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckout.VegaNigeriaTruckOutAddWeightAndBagFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckout.VegaNigeriaTruckOutSummaryFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckout.VegaNigeriaTruckOutWBListFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckout.mtnt.VegaNigeriaTruckOutMtntAddWeightFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckout.mtnt.VegaNigeriaTruckOutMtntSummaryFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.truckout.mtnt.VegaNigeriaTruckOutMtntWBListFragment
import com.olam.warehouse.vegax.nigeriaweighment.ui.weighment.VegaNigeriaWeighmentTypeFragment
import com.olam.warehouse.vegax.nigeriaweighment.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaReceivingActivity: HomeBaseActivity(), VegaNigeriaWeighmentTypeFragment.CallBack,
    VegaNigeriaSupplierFragment.CallBack,
    VegaNigeriaMtnrFragment.CallBack,
    VegaNigeriaTruckInSummaryFragment.CallBack,
    VegaNigeriaTruckOutWBListFragment.CallBack,
    VegaNigeriaTruckOutAddWeightAndBagFragment.CallBack,
    VegaNigeriaTruckOutSummaryFragment.CallBack,
    VegaNigeriaTruckInMtntFragment.CallBack,
    VegaNigeriaTruckOutMtntWBListFragment.CallBack,
    VegaNigeriaTruckOutMtntAddWeightFragment.CallBack,
    VegaNigeriaTruckInListFragment.CallBack {

    private val mTAG = VegaNigeriaReceivingActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_vega_nigeria_receiving

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
            var receiving = VegaQualityWBDetails()
            var postData = ArrayList<VegaReceivingLineItem>()
            receivingData = intent.getParcelableExtra(UIUtils.RECEIVING_DATA)!!
            postData = intent.getParcelableArrayListExtra(UIUtils.RECEIVING_POST_DATA)
                ?: ArrayList<VegaReceivingLineItem>()
            when (receivingData.truckDirection) {
                DIRECTIONIN -> {
                    when (receivingData.status) {
                        Status.RECEVING_COMPLETED -> displayFragment(
                            VegaNigeriaTruckInSummaryFragment.newInstance(
                                receivingData
                            ), false
                        )
                        else -> {
                            if (receivingData.weighBridgeType == PROCURE)
                                displayFragment(VegaNigeriaSupplierFragment.newInstance(receiving), false)
                            else
                                displayFragment(VegaNigeriaMtnrFragment.newInstance(receivingData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (receivingData.status) {
                        Status.RECEVING_COMPLETED -> displayFragment(
                            VegaNigeriaTruckOutSummaryFragment.newInstance(
                                receivingData,
                                getLineItemFromReceivingLineItem(postData), false
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaNigeriaTruckOutAddWeightAndBagFragment.newInstance(
                                    receiving,
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
            postData = intent.getParcelableArrayListExtra(UIUtils.MTNT_POST_DATA)
                ?: ArrayList<VegaMtntLineItem>()
            when (mtntData.truckDirection) {
                DIRECTIONIN -> {
                    when (mtntData.status) {
                        Status.MTNT_COMPLETED -> displayFragment(
                            VegaNigeriaTruckInMtntSummaryFragment.newInstance(
                                mtntData
                            ), false
                        )
                        else -> {
                            displayFragment(VegaNigeriaTruckInMtntFragment.newInstance(mtntData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (mtntData.status) {
                        Status.MTNT_COMPLETED -> displayFragment(
                            VegaNigeriaTruckOutMtntSummaryFragment.newInstance(
                                mtntData,
                                getMtntFromMtntLineItem(postData), false
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaNigeriaTruckOutMtntAddWeightFragment.newInstance(
                                    mtntData,
                                    getMtntFromMtntLineItem(postData)
                                ), false
                            )
                        }
                    }
                }
            }
        } else
            displayFragment(VegaNigeriaWeighmentTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, allowStateLoss = true, containerViewId = R.id.flReceive, allowBackStack = flag)
    }

    override fun replaceFragment(receivingType: String, direction: String, receivingData: VegaReceiving) {

    }

    /*override fun replaceFragment(receivingType: String, direction: String, receivingData: VegaReceiving) {
        *//*if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONIN) {
            *//**//*if (receivingType == SUPPLIER) displayFragment(VegaSupplierFragment.newInstance(receivingData), true)
            else displayFragment(VegaMtnrFragment.newInstance(receivingData), true)*//**//*
            displayFragment(VegaNigeriaTruckInListFragment.newInstance(receivingData), true)
        } else*//* if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONOUT) {
            displayFragment(VegaNigeriaTruckOutWBListFragment.newInstance(receivingData), true)
        }
    }*/

    override fun replaceFragment(receivingType: String, direction: String, receivingData: VegaQualityWBDetails) {
        if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONIN) {
            displayFragment(VegaNigeriaTruckInListFragment.newInstance(receivingData), true)
        } else if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONOUT) {
            displayFragment(VegaNigeriaTruckOutWBListFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>, isRoundoff: Boolean
    ) {

        if (moveFrag == TRUCKOUT_SUMMARYT_FRAG)
            displayFragment(
                VegaNigeriaTruckOutSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>, isRoundoff
                ), true
            )
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaReceiving) {
        when (moveFrag) {
            TRUCKIN_SUMMARYT_FRAG -> displayFragment(
                VegaNigeriaTruckInSummaryFragment.newInstance(receivingData),
                true
            )

            MTNR_FRAG -> displayFragment(VegaNigeriaMtnrFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceMtntFragment(direction: String, mtntData: VegaMtnt) {
        if (direction == DIRECTIONIN) displayFragment(VegaNigeriaTruckInMtntFragment.newInstance(mtntData), true)
        else displayFragment(VegaNigeriaTruckOutMtntWBListFragment.newInstance(mtntData), true)
    }


    override fun replaceMtntFragment(flag: String, moveFrag: String, mtntData: VegaMtnt) {
        if (moveFrag == TRUCKIN_SUMMARYT_FRAG)
            displayFragment(VegaNigeriaTruckInMtntSummaryFragment.newInstance(mtntData), true)
        else if (moveFrag == TRUCKOUT_MTNT_ADD_WEIGHT_FRAG)
            displayFragment(
                VegaNigeriaTruckOutMtntAddWeightFragment.newInstance(mtntData, arrayListOf<VegaMtnt>()),
                true
            )
    }

    override fun replaceMtntFragment(
        moveFrag: String,
        mtntData: VegaMtnt,
        mMtntList: MutableList<VegaMtnt>,
        isRoundoff: Boolean
    ) {
        displayFragment(
            VegaNigeriaTruckOutMtntSummaryFragment.newInstance(
                mtntData,
                mMtntList as ArrayList<VegaMtnt>, isRoundoff
            ), true
        )
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaQualityWBDetails) {
        when (moveFrag) {
            SUPPLIER_FRAG -> displayFragment(VegaNigeriaSupplierFragment.newInstance(receivingData), true)

            TRUCKOUT_ADD_WEIGHT_FRAG -> displayFragment(VegaNigeriaTruckOutAddWeightAndBagFragment.newInstance(receivingData),true)
        }
    }

}
