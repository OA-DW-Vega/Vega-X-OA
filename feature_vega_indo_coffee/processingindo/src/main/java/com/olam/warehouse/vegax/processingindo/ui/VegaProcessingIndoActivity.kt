package com.olam.warehouse.vegax.processingindo.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.processingindo.R
import com.olam.warehouse.vegax.processingindo.di.injectProcessingIndoFeature
import com.olam.warehouse.vegax.processingindo.ui.fgrn.*
import com.olam.warehouse.vegax.processingindo.ui.rmin.*
import com.olam.warehouse.vegax.processingindo.utils.*

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */

class VegaProcessingIndoActivity : HomeBaseActivity(), VegaProcessingIndoSelectTypeFragment.CallBack,
    VegaProcessingIndoRminPoBomSelectFragment.CallBack,
    VegaProcessingIndoRminSelectProcessFragment.CallBack, ProcessingIndoAddLotsListener,
    VegaProcessingIndoRminAddLotFragment.CallBack, VegaProcessingIndoRminShiftSelectFragment.CallBack,
    VegaProcessingIndoFgrnPODetailsFragment.CallBack, VegaProcessingIndoFgrnPendingFragment.CallBack,
    VegaProcessingIndoFgrnGradesFragment.CallBack, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaProcessingIndoFgrnAddWeightAndLotsFragment.CallBack,
    VegaProcessingIndoRminSummaryFragment.CallBack,
    VegaProcessingIndoFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaProcessingIndoFgrnSiftSelectionFragment.CallBack,
    VegaProcessingIndoFgrnSummaryFragment.CallBack, VegaProcessingIndoFgrnAssignLotFragment.CallBack,
    VegaProcessingIndoFgrnFilterFragment.CallBack {

    override val layoutResourceId = R.layout.activity_vega_indo_coffee_processing
    private val mTAG = VegaProcessingIndoActivity::class.java.canonicalName
    private var isLastBack: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectProcessingIndoFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {

        if (intent.hasExtra(UIUtils.FGRN_DATA)) {
            val fgrnItem = intent.getParcelableExtra(UIUtils.FGRN_DATA) as VegaCocoaFgrnItems
            displayFragment(VegaProcessingIndoFgrnGradesFragment.newInstance(fgrnItem, fgrnItem.fgrnId), false)

        } else if (intent.hasExtra(UIUtils.RMIN_DATA)) {
            val rminItem = intent.getParcelableExtra(UIUtils.RMIN_DATA) as VegaCocoaRminProcessing
            displayFragment(VegaProcessingIndoRminAddLotFragment.newInstance(true, rminItem), false)
        } else {
            displayFragment(VegaProcessingIndoSelectTypeFragment.newInstance(), false)
        }

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

    override fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing) {
        when (fragment) {
            RMIN -> {
                displayFragment(VegaProcessingIndoRminSelectProcessFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaProcessingIndoFgrnPODetailsFragment.newInstance(), true)
            }
            SUMMARY -> {
                displayFragment(VegaProcessingIndoRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing) {
        when (fragment) {
            ADDLOT -> {
                displayFragment(
                    VegaProcessingIndoRminAddLotFragment.newInstance(true, model), true
                )
            }
            SHIFT -> {
                displayFragment(VegaProcessingIndoRminShiftSelectFragment.newInstance(flag, model), true)
            }
            Lot_List -> {
                displayFragment(VegaProcessingIndoRminLotListFragment.newInstance(model, flag), true)
            }
            POBOM ->
                displayFragment(
                    VegaProcessingIndoRminPoBomSelectFragment.newInstance(getString(R.string.po_details), flag, model), true
                )
            SUMMARY -> {
                displayFragment(VegaProcessingIndoRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaRminLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        if (lotAddFragment is VegaProcessingIndoRminAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }


    override fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)

        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaProcessingIndoFgrnPODetailsFragment -> displayFragment(
                        VegaProcessingIndoFgrnPendingFragment.newInstance(model),
                        true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaProcessingIndoFgrnPODetailsFragment -> displayFragment( VegaProcessingIndoFgrnGradesFragment.newInstance( model, id ), true  )
                    is VegaProcessingIndoFgrnPendingFragment -> displayFragment( VegaProcessingIndoFgrnGradesFragment.newInstance( model, id ), true)
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(VegaProcessingIndoFgrnAddWeightAndLotsFragment.newInstance(model, id, false), true)
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaProcessingIndoFgrnAddWeightAndLotsFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaProcessingIndoFgrnAddWeightAndLotsFragmentEdit -> {
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
            FRAG_CREATE_LOT -> {
                displayFragment(VegaProcessingIndoFgrnAssignLotFragment.newInstance(model, id), true)
            }
            FRAG_SIFFT -> {
                displayFragment(VegaProcessingIndoFgrnSiftSelectionFragment.newInstance(model), true)
            }
            FRAG_SUMMARY -> displayFragment(VegaProcessingIndoFgrnSummaryFragment.newInstance(model), true)
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaProcessingIndoFgrnAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    id,
                    true
                ), true
            )
            /*FRAG_SUMMARY_BACK -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                when (fragment) {
                    is VegaCocoaFgrnSummaryFragment -> {
                        fragment.updateSummary()
                    }
                }
            }*/
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaProcessingIndoFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaProcessingIndoFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaProcessingIndoFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaProcessingIndoFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
        }
    }

    override fun replaceFgrnFragment(fragment: String, bundle: Bundle) {
        when (fragment) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(VegaSweepingWeightEntryFragment.newInstance(bundle, false), true)
        }
    }



   /* override fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String) {
        when (fragment) {
            FRAG_SIFFT -> {
                displayFragment(VegaCocoaFgrnSiftSelectionFragment.newInstance(model), true)
            }
            FRAG_SUMMARY -> displayFragment(VegaCocoaFgrnSummaryFragment.newInstance(model), true)
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaCocoaFgrnAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    id,
                    true
                ), true
            )
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(VegaCocoaFgrnAddWeightAndLotsFragment.newInstance(model, id, false), true)
            }

        }
    }*/

    override fun isLastBack() {
        isLastBack = true
    }

    override fun onBackPressed() {
        backNaviagation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
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
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaProcessingIndoFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
                closeSocket()
            }
            is VegaProcessingIndoRminAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFGrnFragment(fragment: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragment) {
            FRAG_FILTER -> displayFragment(VegaProcessingIndoFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaProcessingIndoFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaProcessingIndoFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaProcessingIndoFgrnAssignLotFragment -> fragment.applyFilter(bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
