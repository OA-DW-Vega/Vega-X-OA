package com.olam.warehouse.vegax.weighment.ui

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
import com.olam.warehouse.vegax.weighment.R
import com.olam.warehouse.vegax.weighment.di.injectVegaReceivingFeature
import com.olam.warehouse.vegax.weighment.ui.truckin.VegaIndiaCoffeeTruckInListFragment
import com.olam.warehouse.vegax.weighment.ui.truckin.VegaIndiaCoffeeTruckInSummaryFragment
import com.olam.warehouse.vegax.weighment.ui.truckin.mtnr.VegaIndiaCoffeeMtnrFragment
import com.olam.warehouse.vegax.weighment.ui.truckin.mtnt.VegaIndiaCoffeeTruckInMtntFragment
import com.olam.warehouse.vegax.weighment.ui.truckin.mtnt.VegaIndiaCoffeeTruckInMtntSummaryFragments
import com.olam.warehouse.vegax.weighment.ui.truckin.supplier.VegaIndiaCoffeeSupplierFragment
import com.olam.warehouse.vegax.weighment.ui.truckout.VegaIndiaCoffeeTruckOutAddWeightAndBagFragment
import com.olam.warehouse.vegax.weighment.ui.truckout.VegaIndiaCoffeeTruckOutSummaryFragment
import com.olam.warehouse.vegax.weighment.ui.truckout.VegaIndiaCoffeeTruckOutWBListFragment
import com.olam.warehouse.vegax.weighment.ui.truckout.mtnt.VegaIndiaCoffeeTruckOutMtntAddWeightFragment
import com.olam.warehouse.vegax.weighment.ui.truckout.mtnt.VegaIndiaCoffeeTruckOutMtntSummaryFragment
import com.olam.warehouse.vegax.weighment.ui.truckout.mtnt.VegaIndiaCoffeeTruckOutMtntWBListFragment
import com.olam.warehouse.vegax.weighment.ui.weighment.VegaIndiaCoffeeWeighmentTypeFragment
import com.olam.warehouse.vegax.weighment.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeReceivingActivity : HomeBaseActivity(), VegaIndiaCoffeeWeighmentTypeFragment.CallBack,
    VegaIndiaCoffeeSupplierFragment.CallBack,
    VegaIndiaCoffeeMtnrFragment.CallBack,
    VegaIndiaCoffeeTruckInSummaryFragment.CallBack,
    VegaIndiaCoffeeTruckOutWBListFragment.CallBack,
    VegaIndiaCoffeeTruckOutAddWeightAndBagFragment.CallBack,
    VegaIndiaCoffeeTruckOutSummaryFragment.CallBack,
    VegaIndiaCoffeeTruckInMtntFragment.CallBack,
    VegaIndiaCoffeeTruckOutMtntWBListFragment.CallBack,
    VegaIndiaCoffeeTruckOutMtntAddWeightFragment.CallBack,
    VegaIndiaCoffeeTruckInListFragment.CallBack {

    private val mTAG = VegaIndiaCoffeeReceivingActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_vega_india_coffee_receiving

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
                            VegaIndiaCoffeeTruckInSummaryFragment.newInstance(
                                receivingData
                            ), false
                        )
                        else -> {
                            if (receivingData.weighBridgeType == PROCURE)
                                displayFragment(VegaIndiaCoffeeSupplierFragment.newInstance(receivingData), false)
                            else
                                displayFragment(VegaIndiaCoffeeMtnrFragment.newInstance(receivingData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (receivingData.status) {
                        Status.RECEVING_COMPLETED -> displayFragment(
                            VegaIndiaCoffeeTruckOutSummaryFragment.newInstance(
                                receivingData,
                                getLineItemFromReceivingLineItem(postData), false
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaIndiaCoffeeTruckOutAddWeightAndBagFragment.newInstance(
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
                            VegaIndiaCoffeeTruckInMtntSummaryFragments.newInstance(
                                mtntData
                            ), false
                        )
                        else -> {
                            displayFragment(VegaIndiaCoffeeTruckInMtntFragment.newInstance(mtntData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (mtntData.status) {
                        Status.MTNT_COMPLETED -> displayFragment(
                            VegaIndiaCoffeeTruckOutMtntSummaryFragment.newInstance(
                                mtntData,
                                getMtntFromMtntLineItem(postData), false
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaIndiaCoffeeTruckOutMtntAddWeightFragment.newInstance(
                                    mtntData,
                                    getMtntFromMtntLineItem(postData)
                                ), false
                            )
                        }
                    }
                }
            }
        } else
            displayFragment(VegaIndiaCoffeeWeighmentTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, allowStateLoss = true, containerViewId = R.id.flReceive, allowBackStack = flag)
    }

    override fun replaceFragment(receivingType: String, direction: String, receivingData: VegaReceiving) {
        if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONIN) {
            /*if (receivingType == SUPPLIER) displayFragment(VegaSupplierFragment.newInstance(receivingData), true)
            else displayFragment(VegaMtnrFragment.newInstance(receivingData), true)*/
            displayFragment(VegaIndiaCoffeeTruckInListFragment.newInstance(receivingData), true)
        } else if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONOUT) {
            displayFragment(VegaIndiaCoffeeTruckOutWBListFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>, isRoundoff: Boolean
    ) {
        if (moveFrag == TRUCKOUT_ADD_WEIGHT_FRAG)
            displayFragment(
                VegaIndiaCoffeeTruckOutAddWeightAndBagFragment.newInstance(
                    receivingData,
                    arrayListOf<VegaReceiving>()
                ), true
            )
        else if (moveFrag == TRUCKOUT_SUMMARYT_FRAG)
            displayFragment(
                VegaIndiaCoffeeTruckOutSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>, isRoundoff
                ), true
            )
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaReceiving) {
        when (moveFrag) {
            TRUCKOUT_ADD_WEIGHT_FRAG -> displayFragment(
                VegaIndiaCoffeeTruckOutAddWeightAndBagFragment.newInstance(
                    receivingData,
                    arrayListOf<VegaReceiving>()
                ),
                true
            )
            TRUCKIN_SUMMARYT_FRAG -> displayFragment(
                VegaIndiaCoffeeTruckInSummaryFragment.newInstance(receivingData),
                true
            )
            SUPPLIER_FRAG -> displayFragment(VegaIndiaCoffeeSupplierFragment.newInstance(receivingData), true)
            MTNR_FRAG -> displayFragment(VegaIndiaCoffeeMtnrFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceMtntFragment(direction: String, mtntData: VegaMtnt) {
        if (direction == DIRECTIONIN) displayFragment(VegaIndiaCoffeeTruckInMtntFragment.newInstance(mtntData), true)
        else displayFragment(VegaIndiaCoffeeTruckOutMtntWBListFragment.newInstance(mtntData), true)
    }


    override fun replaceMtntFragment(flag: String, moveFrag: String, mtntData: VegaMtnt) {
        if (moveFrag == TRUCKIN_SUMMARYT_FRAG)
            displayFragment(VegaIndiaCoffeeTruckInMtntSummaryFragments.newInstance(mtntData), true)
        else if (moveFrag == TRUCKOUT_MTNT_ADD_WEIGHT_FRAG)
            displayFragment(
                VegaIndiaCoffeeTruckOutMtntAddWeightFragment.newInstance(mtntData, arrayListOf<VegaMtnt>()),
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
            VegaIndiaCoffeeTruckOutMtntSummaryFragment.newInstance(
                mtntData,
                mMtntList as ArrayList<VegaMtnt>, isRoundoff
            ), true
        )
    }


}
