package com.olam.warehouse.vegax.processingecuador.ui

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
import com.olam.warehouse.vegax.processingecuador.R
import com.olam.warehouse.vegax.processingecuador.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingecuador.ui.fgrn.*
import com.olam.warehouse.vegax.processingecuador.ui.rmin.*
import com.olam.warehouse.vegax.processingecuador.utils.*


class VegaEcuadorProcessingActivity : HomeBaseActivity(), VegaEcuadorProcessingSelectTypeFragment.CallBack,
    VegaEcuadorCocoaRminPoBomSelectFragment.CallBack,
    VegaEcuadorCocoaRminSelectProcessFragment.CallBack, AddLotsListener,
    VegaEcuadorCocoaRminAddLotFragment.CallBack, VegaEcuadorCocoaRminShiftSelectFragment.CallBack,
    VegaEcuadorProcessingFgrnPODetailsFragment.CallBack, VegaEcuadorProcessingFgrnPendingFragment.CallBack,
    VegaEcuadorProcessingFgrnGradesFragment.CallBack, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaEcuadorProcessingFgrnAddWeightAndLotsFragment.CallBack,
    VegaEcuadorCocoaRminSummaryFragment.CallBack,
    VegaEcuadorProcessingFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaEcuadorProcessingFgrnSiftSelectionFragment.CallBack,
    VegaEcuadorProcessingFgrnSummaryFragment.CallBack, VegaEcuadorProcessingFgrnAssignLotFragment.CallBack,
    VegaEcuadorProcessingFgrnFilterFragment.CallBack, VegaEcuadorProcessingFgrnRminLotsFragment.CallBack,
    VegaEcuadorProcessingQualityFragment.CallBack {

    override val layoutResourceId = R.layout.activity_ecuador_processing
    private val mTAG = VegaEcuadorProcessingActivity::class.java.canonicalName
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
            displayFragment(VegaEcuadorProcessingFgrnGradesFragment.newInstance(fgrnItem, fgrnItem.fgrnId), false)

        } else if (intent?.hasExtra(UIUtils.RMIN_DATA)!!) {
            val rminItem =
                intent?.getParcelableExtra(UIUtils.RMIN_DATA) ?: VegaCocoaRminProcessing()
            displayFragment(VegaEcuadorCocoaRminAddLotFragment.newInstance(false, rminItem), false)
        } else {
            displayFragment(VegaEcuadorProcessingSelectTypeFragment.newInstance(), false)
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
                displayFragment(VegaEcuadorCocoaRminSelectProcessFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaEcuadorProcessingFgrnPODetailsFragment.newInstance(), true)
            }
            SUMMARY -> {
                displayFragment(VegaEcuadorCocoaRminSummaryFragment.newInstance(model), true)
            }
        }
    }

//    override fun replaceAddLotFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing) {
//        when (fragment) {
//            ADDLOT -> {
//                displayFragment(
//                        VegaEcuadorCocoaRminAddLotFragment.newInstance(flag, model), true
//                )
//            }
//        }
//    }

    override fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing) {
        when (fragment) {
            ADDLOT -> {
                displayFragment(
                    VegaEcuadorCocoaRminAddLotFragment.newInstance(flag, model), true
                )
            }
            SHIFT -> {
                displayFragment(VegaEcuadorCocoaRminShiftSelectFragment.newInstance(flag, model), true)
            }
            Lot_List -> {
                displayFragment(VegaEcuadorCocoaRminLotListFragment.newInstance(model, flag), true)
            }

            POBOM ->
                displayFragment(
                    VegaEcuadorCocoaRminPoBomSelectFragment.newInstance(
                        getString(R.string.po_details),
                        flag,
                        model
                    ),
                    true
                )
            SUMMARY -> {
                displayFragment(VegaEcuadorCocoaRminSummaryFragment.newInstance(model), true)
            }

            Lot_Lists -> {
                displayFragment(
                    VegaEcuadorCocoaRminLotListFragment.newInstance(
                        model,
                        flag
                    ), true
                )
            }
        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaRminLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        if (lotAddFragment is VegaEcuadorCocoaRminAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }


    
    override fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)

        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaEcuadorProcessingFgrnPODetailsFragment -> displayFragment(
                        VegaEcuadorProcessingFgrnPendingFragment.newInstance(model),
                        true
                    )
                }
            }
            FRAG_RMIN_lOTS -> {
                when (fragment1) {
                    is VegaEcuadorProcessingFgrnPODetailsFragment -> displayFragment(
                        VegaEcuadorProcessingFgrnRminLotsFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaEcuadorProcessingFgrnPODetailsFragment -> displayFragment(
                        VegaEcuadorProcessingFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                    is VegaEcuadorProcessingFgrnPendingFragment -> displayFragment(
                        VegaEcuadorProcessingFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                    is VegaEcuadorProcessingFgrnRminLotsFragment -> displayFragment(
                        VegaEcuadorProcessingFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaEcuadorProcessingFgrnAddWeightAndLotsFragment.newInstance(
                        model,
                        id,
                        false
                    ), true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragmentEdit -> {
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
                displayFragment(VegaEcuadorProcessingFgrnAssignLotFragment.newInstance(model, id), true)
            }
            FRAG_SIFFT -> {
                displayFragment(VegaEcuadorProcessingFgrnSiftSelectionFragment.newInstance(model), true)
            }
            FRAG_QUALITY -> {
                displayFragment(VegaEcuadorProcessingQualityFragment.newInstance(model, id), true)
            }
            FRAG_SUMMARY -> displayFragment(
                VegaEcuadorProcessingFgrnSummaryFragment.newInstance(model),
                true
            )
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaEcuadorProcessingFgrnAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    id,
                    true
                ), true
            )
            /*FRAG_SUMMARY_BACK -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                when (fragment) {
                    is VegaIndiaCoffeeFgrnSummaryFragment -> {
                        fragment.updateSummary()
                    }
                }
            }*/
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
        }
    }

    override fun replaceFgrnFragment(fragment: String, bundle: Bundle) {
        when (fragment) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(VegaSweepingWeightEntryFragment.newInstance(bundle, true), true)
        }
    }


    /* override fun replaceFgrnFragment(fragment: String, model: VegaIndiaCoffeeFgrnItems, id: String) {
         when (fragment) {
             FRAG_SIFFT -> {
                 displayFragment(VegaIndiaCoffeeFgrnSiftSelectionFragment.newInstance(model), true)
             }
             FRAG_SUMMARY -> displayFragment(VegaIndiaCoffeeFgrnSummaryFragment.newInstance(model), true)
             FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                 VegaIndiaCoffeeFgrnAddWeightAndLotsFragmentEdit.newInstance(
                     model,
                     id,
                     true
                 ), true
             )
             FRAG_ADD_WEIGHT -> {
                 isLastBack = false
                 displayFragment(VegaIndiaCoffeeFgrnAddWeightAndLotsFragment.newInstance(model, id, false), true)
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
            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
                closeSocket()
            }
            is VegaEcuadorCocoaRminAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFGrnFragment(fragment: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragment) {
            FRAG_FILTER -> displayFragment(VegaEcuadorProcessingFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaEcuadorProcessingFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaEcuadorProcessingFgrnAssignLotFragment -> fragment.applyFilter(bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
