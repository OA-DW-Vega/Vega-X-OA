package com.olam.warehouse.vegax.processingcocoa.ui

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
import com.olam.warehouse.vegax.processingcocoa.R
import com.olam.warehouse.vegax.processingcocoa.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingcocoa.ui.fgrn.*
import com.olam.warehouse.vegax.processingcocoa.ui.rmin.*
import com.olam.warehouse.vegax.processingcocoa.utils.*

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */

class VegaProcessingCocoaActivity : HomeBaseActivity(), VegaCocoaProcessingSelectTypeFragment.CallBack,
    VegaCocoaRminPoBomSelectFragment.CallBack,
    VegaCocoaRminSelectProcessFragment.CallBack, AddLotsListener,
    VegaCocoaRminAddLotFragment.CallBack, VegaCocoaRminShiftSelectFragment.CallBack,
    VegaCocoaFgrnPODetailsFragment.CallBack, VegaCocoaFgrnPendingFragment.CallBack,
    VegaCocoaFgrnGradesFragment.CallBack, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaFgrnAddWeightAndLotsFragment.CallBack,
    VegaCocoaRminSummaryFragment.CallBack,
    VegaCocoaFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaCocoaFgrnSiftSelectionFragment.CallBack,
    VegaCocoaFgrnSummaryFragment.CallBack, VegaCocoaFgrnAssignLotFragment.CallBack,
    VegaCocoaFgrnFilterFragment.CallBack {

    override val layoutResourceId = R.layout.activity_vega_cocoa_processing
    private val mTAG = VegaProcessingCocoaActivity::class.java.canonicalName
    private var isLastBack: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCocoaProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {

        if (intent?.hasExtra(UIUtils.FGRN_DATA)!!) {
            val fgrnItem = intent?.getParcelableExtra(UIUtils.FGRN_DATA) ?: VegaCocoaFgrnItems()
            displayFragment(VegaCocoaFgrnGradesFragment.newInstance(fgrnItem, fgrnItem.fgrnId), false)

        } else if (intent?.hasExtra(UIUtils.RMIN_DATA)!!) {
            val rminItem =
                intent?.getParcelableExtra(UIUtils.RMIN_DATA) ?: VegaCocoaRminProcessing()
            displayFragment(VegaCocoaRminAddLotFragment.newInstance(false, rminItem), false)
        } else
        {
            displayFragment(VegaCocoaProcessingSelectTypeFragment.newInstance(), false)
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
                displayFragment(VegaCocoaRminSelectProcessFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaCocoaFgrnPODetailsFragment.newInstance(), true)
            }
            SUMMARY -> {
                displayFragment(VegaCocoaRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing) {
        when (fragment) {
            ADDLOT -> {
                displayFragment(
                    VegaCocoaRminAddLotFragment.newInstance(flag, model), true
                )
            }
            SHIFT -> {
                displayFragment(VegaCocoaRminShiftSelectFragment.newInstance(flag, model), true)
            }
            Lot_List -> {
                displayFragment(VegaCocoaRminLotListFragment.newInstance(model, flag), true)
            }
            POBOM ->
                displayFragment(
                    VegaCocoaRminPoBomSelectFragment.newInstance(getString(R.string.po_details), flag, model), true
                )
            SUMMARY -> {
                displayFragment(VegaCocoaRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaRminLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        if (lotAddFragment is VegaCocoaRminAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }


    
    override fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)

        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaCocoaFgrnPODetailsFragment -> displayFragment(
                        VegaCocoaFgrnPendingFragment.newInstance(model),
                        true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaCocoaFgrnPODetailsFragment -> displayFragment( VegaCocoaFgrnGradesFragment.newInstance( model, id ), true  )
                    is VegaCocoaFgrnPendingFragment -> displayFragment( VegaCocoaFgrnGradesFragment.newInstance( model, id ), true)
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(VegaCocoaFgrnAddWeightAndLotsFragment.newInstance(model, id, false), true)
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaCocoaFgrnAddWeightAndLotsFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaCocoaFgrnAddWeightAndLotsFragmentEdit -> {
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
                displayFragment(VegaCocoaFgrnAssignLotFragment.newInstance(model, id), true)
            }
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
            is VegaCocoaFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaCocoaFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCocoaFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaCocoaFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
        }
    }

    override fun replaceFgrnFragment(fragment: String, bundle: Bundle) {
        when (fragment) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(VegaSweepingWeightEntryFragment.newInstance(bundle, true), true)
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
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCocoaFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
                closeSocket()
            }
            is VegaCocoaRminAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFGrnFragment(fragment: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragment) {
            FRAG_FILTER -> displayFragment(VegaCocoaFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCocoaFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaCocoaFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCocoaFgrnAssignLotFragment -> fragment.applyFilter(bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
