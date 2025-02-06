package com.olam.warehouse.vegax.processing.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_POST_DATA
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.di.injectProcessingFeature
import com.olam.warehouse.vegax.processing.ui.fgrn.VegaFgrnGradeSelectionFragment
import com.olam.warehouse.vegax.processing.ui.fgrn.VegaFgrnPoDetailsFragment
import com.olam.warehouse.vegax.processing.ui.fgrn.VegaFgrnPoSelectionFragment
import com.olam.warehouse.vegax.processing.ui.fgrn.VegaFgrnSummaryFragment
import com.olam.warehouse.vegax.processing.ui.rmin.VegaRminBomListFragment
import com.olam.warehouse.vegax.processing.ui.rmin.VegaRminLotToProcessFragment
import com.olam.warehouse.vegax.processing.ui.rmin.VegaRminSelectProcessFragment
import com.olam.warehouse.vegax.processing.ui.rmin.VegaRminSummaryFragment
import com.olam.warehouse.vegax.processing.utils.*

/**
 * Created by Baskaran Kannan on 2/12/2020.
 */
class VegaProcessingActivity : HomeBaseActivity(), VegaProcessingTypeFragment.CallBack,
    VegaRminSelectProcessFragment.CallBack, VegaRminBomListFragment.CallBack, VegaRminLotToProcessFragment.CallBack,
    VegaFgrnPoDetailsFragment.CallBack, VegaFgrnGradeSelectionFragment.CallBack, VegaFgrnPoSelectionFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_processing
    private val mTAG = VegaProcessingActivity::class.java.canonicalName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(RMIN_DATA)) {
            var rminData = VegaProcessingCreatePoReq()
            var rminPostData = ArrayList<ProcessingLotDetails>()
            rminData = intent.getParcelableExtra(RMIN_DATA)!!
            rminPostData = intent.getParcelableArrayListExtra(RMIN_POST_DATA)!!
            displayFragment(
                VegaRminLotToProcessFragment.newInstance(
                    rminData.processingStage.toString(),
                    rminData.cfgNo,
                    rminData.materialName.toString(),
                    rminData.materialCode.toString(),
                    prepareRminCreatePoToBom(rminData),
                    rminPostData
                ), false
            )
        } else
            displayFragment(VegaProcessingTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flProcessing,
            allowBackStack = flag
        )
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean, poFragment: String) {
        replaceFragment(
            fragment,
            poFragment,
            allowStateLoss = true,
            containerViewId = R.id.flProcessing,
            allowBackStack = flag
        )
    }


    override fun replaceFragment(fragment: String) {
        when (fragment) {
            RMIN -> {
                displayFragment(VegaRminSelectProcessFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaFgrnPoDetailsFragment.newInstance(), true)
            }
        }

    }

    //RMIN
    override fun replaceFragment(
        stageFevor: String,
        cfgNumber: String,
        materialName: String,
        materialNo: String,
        bomList: List<VegaProcessingRminBoms>
    ) {
        val bomListData = ArrayList<VegaProcessingRminBoms>()
        bomListData.addAll(bomList)
        displayFragment(
            VegaRminBomListFragment.newInstance(stageFevor, cfgNumber, materialName, materialNo, bomListData), true
        )
    }

    override fun replaceFragment(
        stageFevor: String,
        cfgNumber: String,
        materialName: String,
        materialNo: String,
        bom: VegaProcessingRminBoms
    ) {
        displayFragment(
            VegaRminLotToProcessFragment.newInstance(
                stageFevor,
                cfgNumber,
                materialName,
                materialNo,
                bom,
                arrayListOf()
            ),
            true
        )
    }

    override fun replaceFragment(
        stageFevor: String,
        cfgNumber: String,
        materialName: String,
        materialNo: String,
        bom: VegaProcessingRminBoms,
        lotDetails: VegaDispatchLots
    ) {
        displayFragment(
            VegaRminSummaryFragment.newInstance(
                stageFevor,
                cfgNumber,
                materialName,
                materialNo,
                bom,
                lotDetails
            ), true
        )
    }

    //FGRN
    override fun replaceFragment(fragment: String, poOrder: VegaFgrnProcessingOrder, poGrade: String) {
        when (fragment) {
            GRADESELECTION -> {
                displayFragment(VegaFgrnGradeSelectionFragment.newInstance(poOrder), true)
            }
            POSELECTION -> {
                displayFragment(VegaFgrnPoSelectionFragment.newInstance(poOrder, poGrade), true, POSELECTION)
            }
            FGRNSUMMARY -> {
                displayFragment(VegaFgrnSummaryFragment.newInstance(poOrder, poGrade), true)
            }

        }

    }

    override fun onBackPressed() {
        backNaviagation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNaviagation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun backNaviagation() {
        val fragment = this.supportFragmentManager.findFragmentById(R.id.flProcessing)
        when {
            fragment?.tag.equals(POSELECTION) -> {
                (fragment as? IOBackpressed)?.onBackPressed()?.let {
                    if (it) {
                        super.onBackPressed()
                    }
                }
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}
