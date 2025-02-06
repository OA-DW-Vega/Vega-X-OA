package com.olam.warehouse.vegax.receivingghanacash.ui

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
import com.olam.warehouse.vegax.receivingghanacash.R
import com.olam.warehouse.vegax.receivingghanacash.di.injectVegaReceivingGhanaFeature
import com.olam.warehouse.vegax.receivingghanacash.ui.truckin.VegaReceivingGhanaTruckInListFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckin.VegaReceivingGhanaTruckInSummaryFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckin.mtnr.VegaReceivingGhanaMtnrFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckin.mtnt.VegaReceivingGhanaTruckInMtntFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckin.mtnt.VegaReceivingGhanaTruckInMtntSummaryFragments
import com.olam.warehouse.vegax.receivingghanacash.ui.truckin.supplier.VegaReceivingGhanaSupplierFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckout.VegaReceivingGhanaTruckOutAddWeightAndBagFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckout.VegaReceivingGhanaTruckOutSummaryFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckout.VegaReceivingGhanaTruckOutWBListFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckout.mtnt.VegaReceivingGhanaTruckOutMtntAddWeightFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckout.mtnt.VegaReceivingGhanaTruckOutMtntSummaryFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.truckout.mtnt.VegaReceivingGhanaTruckOutMtntWBListFragment
import com.olam.warehouse.vegax.receivingghanacash.ui.weighment.VegaReceivingGhanaWeighmentTypeFragment
import com.olam.warehouse.vegax.receivingghanacash.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/18/2019.
 */
class VegaReceivingGhanaActivity : HomeBaseActivity(), VegaReceivingGhanaWeighmentTypeFragment.CallBack,
    VegaReceivingGhanaSupplierFragment.CallBack,
    VegaReceivingGhanaMtnrFragment.CallBack,
    VegaReceivingGhanaTruckInSummaryFragment.CallBack,
    VegaReceivingGhanaTruckOutWBListFragment.CallBack,
    VegaReceivingGhanaTruckOutAddWeightAndBagFragment.CallBack,
    VegaReceivingGhanaTruckOutSummaryFragment.CallBack,
    VegaReceivingGhanaTruckInMtntFragment.CallBack,
    VegaReceivingGhanaTruckOutMtntWBListFragment.CallBack,
    VegaReceivingGhanaTruckOutMtntAddWeightFragment.CallBack,
    VegaReceivingGhanaTruckInListFragment.CallBack {

    private val mTAG = VegaReceivingGhanaActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_vega_receiving_ghana

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaReceivingGhanaFeature()
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
                            VegaReceivingGhanaTruckInSummaryFragment.newInstance(
                                receivingData
                            ), false
                        )
                        else -> {
                            if (receivingData.weighBridgeType == PROCURE)
                                displayFragment(VegaReceivingGhanaSupplierFragment.newInstance(receivingData), false)
                            else
                                displayFragment(VegaReceivingGhanaMtnrFragment.newInstance(receivingData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (receivingData.status) {
                        Status.RECEVING_COMPLETED -> displayFragment(
                            VegaReceivingGhanaTruckOutSummaryFragment.newInstance(
                                receivingData,
                                getLineItemFromReceivingLineItem(postData)
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaReceivingGhanaTruckOutAddWeightAndBagFragment.newInstance(
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
                            VegaReceivingGhanaTruckInMtntSummaryFragments.newInstance(
                                mtntData
                            ), false
                        )
                        else -> {
                            displayFragment(VegaReceivingGhanaTruckInMtntFragment.newInstance(mtntData), false)
                        }
                    }
                }
                DIRECTIONOUT -> {
                    when (mtntData.status) {
                        Status.MTNT_COMPLETED -> displayFragment(
                            VegaReceivingGhanaTruckOutMtntSummaryFragment.newInstance(
                                mtntData,
                                getMtntFromMtntLineItem(postData)
                            ), false
                        )
                        else -> {
                            displayFragment(
                                VegaReceivingGhanaTruckOutMtntAddWeightFragment.newInstance(
                                    mtntData,
                                    getMtntFromMtntLineItem(postData)
                                ), false
                            )
                        }
                    }
                }
            }
        } else
            displayFragment(VegaReceivingGhanaWeighmentTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, allowStateLoss = true, containerViewId = R.id.flReceive, allowBackStack = flag)
    }

    override fun replaceFragment(receivingType: String, direction: String, receivingData: VegaReceiving) {
        if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONIN) {
            /*if (receivingType == SUPPLIER) displayFragment(VegaSupplierFragment.newInstance(receivingData), true)
            else displayFragment(VegaMtnrFragment.newInstance(receivingData), true)*/
            displayFragment(VegaReceivingGhanaTruckInListFragment.newInstance(receivingData), true)
        } else if ((receivingType == SUPPLIER || receivingType == MTNR) && direction == DIRECTIONOUT) {
            displayFragment(VegaReceivingGhanaTruckOutWBListFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>
    ) {
        if (moveFrag == TRUCKOUT_ADD_WEIGHT_FRAG)
            displayFragment(
                VegaReceivingGhanaTruckOutAddWeightAndBagFragment.newInstance(
                    receivingData,
                    arrayListOf<VegaReceiving>()
                ), true
            )
        else if (moveFrag == TRUCKOUT_SUMMARYT_FRAG)
            displayFragment(
                VegaReceivingGhanaTruckOutSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>
                ), true
            )
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaReceiving) {
        when (moveFrag) {
            TRUCKOUT_ADD_WEIGHT_FRAG -> displayFragment(
                VegaReceivingGhanaTruckOutAddWeightAndBagFragment.newInstance(
                    receivingData,
                    arrayListOf<VegaReceiving>()
                ),
                true
            )
            TRUCKIN_SUMMARYT_FRAG -> displayFragment(VegaReceivingGhanaTruckInSummaryFragment.newInstance(receivingData), true)
            SUPPLIER_FRAG -> displayFragment(VegaReceivingGhanaSupplierFragment.newInstance(receivingData), true)
            MTNR_FRAG -> displayFragment(VegaReceivingGhanaMtnrFragment.newInstance(receivingData), true)
        }
    }

    override fun replaceMtntFragment(direction: String, mtntData: VegaMtnt) {
        if (direction == DIRECTIONIN) displayFragment(VegaReceivingGhanaTruckInMtntFragment.newInstance(mtntData), true)
        else displayFragment(VegaReceivingGhanaTruckOutMtntWBListFragment.newInstance(mtntData), true)
    }


    override fun replaceMtntFragment(flag: String, moveFrag: String, mtntData: VegaMtnt) {
        if (moveFrag == TRUCKIN_SUMMARYT_FRAG)
            displayFragment(VegaReceivingGhanaTruckInMtntSummaryFragments.newInstance(mtntData), true)
        else if (moveFrag == TRUCKOUT_MTNT_ADD_WEIGHT_FRAG)
            displayFragment(VegaReceivingGhanaTruckOutMtntAddWeightFragment.newInstance(mtntData, arrayListOf<VegaMtnt>()), true)
    }

    override fun replaceMtntFragment(moveFrag: String, mtntData: VegaMtnt, mMtntList: MutableList<VegaMtnt>) {
        displayFragment(VegaReceivingGhanaTruckOutMtntSummaryFragment.newInstance(mtntData, mMtntList as ArrayList<VegaMtnt>), true)
    }
}
