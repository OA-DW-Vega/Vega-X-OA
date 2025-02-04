package com.olam.warehouse.vegax.processingindiacoffee.ui

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
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn.*
import com.olam.warehouse.vegax.processingindiacoffee.ui.rmin.*
import com.olam.warehouse.vegax.processingindiacoffee.utils.*


class VegaProcessingIndiaCoffeeActivity : HomeBaseActivity(), VegaIndiaCoffeeProcessingSelectTypeFragment.CallBack,
    VegaIndiaCoffeeRminPoBomSelectFragment.CallBack,
    VegaIndiaCoffeeRminSelectProcessFragment.CallBack, AddLotsListener,
    VegaIndiaCoffeeRminAddLotFragment.CallBack, VegaIndiaCoffeeRminShiftSelectFragment.CallBack,
    VegaIndiaCoffeeFgrnPODetailsFragment.CallBack, VegaIndiaCoffeeFgrnPendingFragment.CallBack,
    VegaIndiaCoffeeFgrnGradesFragment.CallBack, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaIndiaCoffeeFgrnAddWeightAndLotsFragment.CallBack,
    VegaIndiaCoffeeRminSummaryFragment.CallBack,
    VegaIndiaCoffeeFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaIndiaCoffeeFgrnSiftSelectionFragment.CallBack,
    VegaIndiaCoffeeFgrnSummaryFragment.CallBack, VegaIndiaCoffeeFgrnAssignLotFragment.CallBack,
    VegaIndiaCoffeeFgrnFilterFragment.CallBack, VegaNicaraguaCoffeeFgrnRminLotsFragment.CallBack,
    VegaNicaraCoffeeQualityFragment.CallBack {

    override val layoutResourceId = R.layout.activity_india_coffee_processing
    private val mTAG = VegaProcessingIndiaCoffeeActivity::class.java.canonicalName
    private var isLastBack: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCocoaProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {

        if (intent?.hasExtra(UIUtils.FGRN_DATA)!!) {
            val fgrnItem = intent?.getParcelableExtra(UIUtils.FGRN_DATA) as VegaCocoaFgrnItems
            displayFragment(VegaIndiaCoffeeFgrnGradesFragment.newInstance(fgrnItem, fgrnItem.fgrnId), false)

        } else if (intent?.hasExtra(UIUtils.RMIN_DATA)!!) {
            val rminItem = intent?.getParcelableExtra(UIUtils.RMIN_DATA) as VegaCocoaRminProcessing
            displayFragment(VegaIndiaCoffeeRminAddLotFragment.newInstance(false, rminItem,false), false)
        } else {
            displayFragment(VegaIndiaCoffeeProcessingSelectTypeFragment.newInstance(), false)
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
                displayFragment(VegaIndiaCoffeeRminSelectProcessFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaIndiaCoffeeFgrnPODetailsFragment.newInstance(), true)
            }
            SUMMARY -> {
                displayFragment(VegaIndiaCoffeeRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun replaceAddLotFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing, isPoSelection: Boolean) {
        when (fragment) {
            ADDLOT -> {
                displayFragment(
                        VegaIndiaCoffeeRminAddLotFragment.newInstance(flag, model,isPoSelection), true
                )
            }
        }
    }

    override fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing) {
        when (fragment) {
           /* ADDLOT -> {
                displayFragment(
                    VegaIndiaCoffeeRminAddLotFragment.newInstance(flag, model), true
                )
            }*/
            SHIFT -> {
                displayFragment(VegaIndiaCoffeeRminShiftSelectFragment.newInstance(flag, model), true)
            }
            Lot_List -> {
                displayFragment(VegaIndiaCoffeeRminLotListFragment.newInstance(model, flag), true)
            }

            POBOM ->
                displayFragment(
                    VegaIndiaCoffeeRminPoBomSelectFragment.newInstance(
                        getString(R.string.po_details),
                        flag,
                        model
                    ),
                    true
                )
            SUMMARY -> {
                displayFragment(VegaIndiaCoffeeRminSummaryFragment.newInstance(model), true)
            }

            Lot_Lists -> {
                displayFragment(
                    VegaIndiaCoffeeRminInventoryLotListFragment.newInstance(
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
        if (lotAddFragment is VegaIndiaCoffeeRminAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }


    override fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)

        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaIndiaCoffeeFgrnPODetailsFragment -> displayFragment(
                        VegaIndiaCoffeeFgrnPendingFragment.newInstance(model),
                        true
                    )
                }
            }
            FRAG_RMIN_lOTS -> {
                when (fragment1) {
                    is VegaIndiaCoffeeFgrnPODetailsFragment -> displayFragment(
                        VegaNicaraguaCoffeeFgrnRminLotsFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaIndiaCoffeeFgrnPODetailsFragment -> displayFragment(
                        VegaIndiaCoffeeFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                    is VegaIndiaCoffeeFgrnPendingFragment -> displayFragment(
                        VegaIndiaCoffeeFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                    is VegaNicaraguaCoffeeFgrnRminLotsFragment -> displayFragment(
                        VegaIndiaCoffeeFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaIndiaCoffeeFgrnAddWeightAndLotsFragment.newInstance(
                        model,
                        id,
                        false
                    ), true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragmentEdit -> {
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
                displayFragment(VegaIndiaCoffeeFgrnAssignLotFragment.newInstance(model, id), true)
            }
            FRAG_SIFFT -> {
                displayFragment(VegaIndiaCoffeeFgrnSiftSelectionFragment.newInstance(model), true)
            }
            FRAG_QUALITY -> {
                displayFragment(VegaNicaraCoffeeQualityFragment.newInstance(model, id), true)
            }
            FRAG_SUMMARY -> displayFragment(
                VegaIndiaCoffeeFgrnSummaryFragment.newInstance(model),
                true
            )
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaIndiaCoffeeFgrnAddWeightAndLotsFragmentEdit.newInstance(
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
            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragmentEdit -> {
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
            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
                closeSocket()
            }
            is VegaIndiaCoffeeRminAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFGrnFragment(fragment: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragment) {
            FRAG_FILTER -> displayFragment(VegaIndiaCoffeeFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaIndiaCoffeeFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaIndiaCoffeeFgrnAssignLotFragment -> fragment.applyFilter(bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }
}
