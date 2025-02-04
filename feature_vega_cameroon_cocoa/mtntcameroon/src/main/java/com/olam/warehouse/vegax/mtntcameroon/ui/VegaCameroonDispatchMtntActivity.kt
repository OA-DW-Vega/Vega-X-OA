package com.olam.warehouse.vegax.mtntcameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcameroon.R
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonLotListModel
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonWSBagModel
import com.olam.warehouse.vegax.mtntcameroon.di.injectCameroonDispatchFeature
import com.olam.warehouse.vegax.mtntcameroon.ui.weighbridge.VegaCameroonMtntSummaryFragment
import com.olam.warehouse.vegax.mtntcameroon.ui.weighbridge.VegaCameroonMtntWeighBridgeAddLotFragment
import com.olam.warehouse.vegax.mtntcameroon.ui.weighbridge.VegaCameroonMtntWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.mtntcameroon.ui.weighscale.VegaCameroonMtntConsignmentFragment
import com.olam.warehouse.vegax.mtntcameroon.ui.weighscale.VegaCameroonMtntWeighScaleAddLotFragment
import com.olam.warehouse.vegax.mtntcameroon.ui.weighscale.VegaCameroonMtntWeighScalePalletFragment
import com.olam.warehouse.vegax.mtntcameroon.ui.weighscale.VegaCameroonMtntWeighScaleSummaryFragment
import com.olam.warehouse.vegax.mtntcameroon.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCameroonDispatchMtntActivity : HomeBaseActivity(), VegaCameroonReplaceFragmentCallback, VegaCameroonAddLotListener,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCocoaAddPalletFragment.CallBackPallet {
    private val mTAG = VegaCameroonDispatchMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_cameroon_mtnt_dispatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonDispatchFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("mtntcameroon/ui/VegaCameroonDispatchMtntActivity")
            .title("Vega_Cameroon/Mtnt").with(tracker)
    }
    private fun initUI() {
        displayFragment(VegaCameroonMtntConsignmentFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainer,
            allowBackStack = flag
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val view: View? = this.currentFocus
        val ret = super.dispatchTouchEvent(event)
        if (view is EditText) {
            try {
                val w: View? = this.currentFocus
                val scrcords = IntArray(2)
                w?.getLocationOnScreen(scrcords)
                val x: Float = event.rawX + w?.left?.toFloat()!! - scrcords[0]
                val y: Float = event.rawY + w.top - scrcords[1]
                if (event.action == MotionEvent.ACTION_UP && (x < w.left || x >= w.right || y < w.top || y > w.bottom)) {
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (window != null && window.currentFocus != null) {
                        imm.hideSoftInputFromWindow(window.currentFocus!!.windowToken, 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return ret
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHBRIDGE -> displayFragment(VegaCameroonMtntWeighbridgeTruckListFragment.newInstance(), true)
            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaCameroonMtntWeighBridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaCameroonMtntSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            MTNT_WEIGHSCALE -> displayFragment(
                VegaCameroonMtntConsignmentFragment.newInstance(),
                true
            )
            WEIGHSCALE_ADD_LOT -> displayFragment(
                VegaCameroonMtntWeighScaleAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            ADD_WEIGHT -> {
                displayFragment(
                    VegaCameroonMtntWeighScalePalletFragment.newInstance(data as VegaCocoaDispatchLots),
                    true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                        when (fragment) {
                            is VegaCameroonMtntWeighScalePalletFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaSweepingWeightEntryFragment -> {
                                fragment.updateBtWeight(btValue)
                            }
                        }
                    }

                })
            }

            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCameroonMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(data as VegaCameroonWSBagModel)
                }
            }
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaCameroonMtntWeighScaleSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
        }
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNavigation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backNavigation() {
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaCameroonMtntWeighBridgeAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaCameroonMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaCameroonMtntWeighScaleSummaryFragment -> {
                if (fragment.isPostCreated)
                    fragment.backNav()
                else super.onBackPressed()
            }

            else -> super.onBackPressed()
        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {
            is VegaCameroonMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaCameroonMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonMtntWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun replaceFragment(receivingType: String, data: Any, list: VegaCocoaDispatchWB) {
        when (receivingType) {
            LOT_LIST -> displayFragment(
                VegaCameroonLotListFragment.newInstance(
                    data as VegaCameroonLotListModel,
                    list
                ), true
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
