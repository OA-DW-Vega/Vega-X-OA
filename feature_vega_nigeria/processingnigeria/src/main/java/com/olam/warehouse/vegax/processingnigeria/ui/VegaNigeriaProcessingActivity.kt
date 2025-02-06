package com.olam.warehouse.vegax.processingnigeria.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.di.injectNigeriaProcessingFeature
import com.olam.warehouse.vegax.processingnigeria.ui.fgrn.*
import com.olam.warehouse.vegax.processingnigeria.ui.rmin.*
import com.olam.warehouse.vegax.processingnigeria.utils.*

class VegaNigeriaProcessingActivity : HomeBaseActivity(),
    VegaNigeriaProcessingSelectTypeFragment.CallBack,
    VegaNigeriaFgrnPendingFragment.CallBack, VegaNigeriaFgrnAddWeightAndLotsFragment.CallBack,
    VegaNigeriaFgrnAssignLotFragment.CallBack, VegaNigeriaFgrnFilterFragment.CallBack,
    VegaNigeriaFgrnGradesFragment.CallBack, VegaNigeriaFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaNigeriaFgrnPODetailsFragment.CallBack, VegaNigeriaFgrnShiftSelectionFragment.CallBack,
    VegaNigeriaFgrnSummaryFragment.CallBack, VegaNigeriaRMINSelectPoFragment.CallBack,
    VegaNigeriaRMINGradesFragment.CallBack, VegaNigeriaRMINAddLotFragment.CallBack,
    NigeriaAddLotsListener, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaNigeriaFgrnBagConsumptionFragment.CallBack,
    VegaNigeriaRMINAddWeightAndLotsFragment.CallBack,
    VegaNigeriaRMINShiftSelectFragment.CallBack, VegaNigeriaRMINEditLotFragment.CallBack,
    VegaNigeriaRminSummaryFragment.CallBack, VegaNigeriaCocoaRminPoBomSelectFragment.CallBack,
    VegaNigeriaCocoaRminPoBomSubStagesSelectFragment.CallBack {
    override val layoutResourceId: Int =
        R.layout.activity_vega_nigeria_processing
    private val mTAG = VegaNigeriaProcessingActivity::class.java.canonicalName
    private var isLastBack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaProcessingSelectTypeFragment.newInstance(), false)
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
            is VegaNigeriaFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }
            is VegaNigeriaRMINAddLotFragment -> {
                fragment.saveLotDetails()
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }
            is VegaNigeriaRMINAddWeightAndLotsFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaNigeriaRMINEditLotFragment -> {
                fragment.moveToShiftSelect()
                // super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing) {
        when (fragment) {
            RMIN -> {
                displayFragment(VegaNigeriaRMINSelectPoFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaNigeriaFgrnPODetailsFragment.newInstance(), true)
            }
        }
    }

    override fun replaceFgrnFragment(fragment: String, bundle: Bundle) {
        when (fragment) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(VegaSweepingWeightEntryFragment.newInstance(bundle, false), true)
        }
    }

    override fun replaceFgrnFragment(
        fragment: String,
        model: VegaCoffeeFgrnItems,
        id: String,
        isThirdPartyMaterial: Boolean,
        material: String
    ) {
        when (fragment) {
            FRAG_CREATE_LOT -> {
                displayFragment(
                    VegaNigeriaFgrnAssignLotFragment.newInstance(model, id, isThirdPartyMaterial, material),
                    true
                )
            }
            FRAG_SHIFT -> {
                displayFragment(VegaNigeriaFgrnShiftSelectionFragment.newInstance(model), true)
            }
        }
    }

    override fun replaceFragment(fragment: String, data: String, batchNo: String?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaNigeriaRMINAddLotFragment -> {
                fragment.updateAddWeight(data, batchNo)
            }
            is VegaNigeriaRMINEditLotFragment -> {
                fragment.updateAddWeight(data, batchNo)
            }
        }
    }

    /*override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing, id: String) {
        displayFragment(VegaCoffeeRMINShiftSelectFragment.newInstance(false, model), true)
    }*/

    
    override fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaNigeriaFgrnPODetailsFragment -> displayFragment(
                        VegaNigeriaFgrnPendingFragment.newInstance(model),
                        true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaNigeriaFgrnPODetailsFragment -> displayFragment(
                        VegaNigeriaFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                    is VegaNigeriaFgrnPendingFragment -> displayFragment(
                        VegaNigeriaFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(VegaNigeriaFgrnAddWeightAndLotsFragment.newInstance(model, id, false), true)
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaNigeriaFgrnAddWeightAndLotsFragment -> {
                                val fragment1 = supportFragmentManager.findFragmentById(R.id.flPallet)
//                                val fragment1 = VegaCocoaAddPalletFragment()
                                when(fragment1){
                                   // is VegaCocoaAddPalletFragment ->  fragment1.updateBtWeight(btValue)
                                }

                            }
                            is VegaSweepingWeightEntryFragment -> {
                                fragment.updateBtWeight(btValue)
                            }
                        }
                    }

                })
            }
            FRAG_BAG_CONSUMP -> {
                displayFragment(VegaNigeriaFgrnBagConsumptionFragment.newInstance(model), true)
            }
            FRAG_SUMMARY -> displayFragment(VegaNigeriaFgrnSummaryFragment.newInstance(model), true)
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaNigeriaFgrnAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    id,
                    true
                ), true
            )
        }
    }

    
    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        model: VegaCoffeeRminProcessing,
        materialCode: String, batchNumer: String
    ) {
        when (fragment) {
            SHIFT -> {
                displayFragment(VegaNigeriaRMINShiftSelectFragment.newInstance(flag, model), true)
            }

            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaNigeriaRMINAddWeightAndLotsFragment.newInstance(
                        model,
                        false,
                        materialCode,
                        batchNumer
                    ), true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaNigeriaRMINAddWeightAndLotsFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                               // fragment1.updateBtWeight(btValue)
                            }
                            is VegaSweepingWeightEntryFragment -> {
                                fragment.updateBtWeight(btValue)
                            }
                        }
                    }

                })
            }
            SUMMARY -> displayFragment(VegaNigeriaRminSummaryFragment.newInstance(model), true)
        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        model: VegaCoffeeRminProcessing,
        materialCode: String,
        batchNo: String,
        isThirdParty: Boolean
    ) {
        when (fragment) {
            Lot_List -> displayFragment(
                VegaNigeriaRMINLotListFragment.newInstance(
                    model,
                    flag,
                    materialCode,
                    isThirdParty
                ), true
            )
        }
    }

    override fun isLastBack() {
        isLastBack = true
    }

    override fun replaceFGrnFragment(fragFilter: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragFilter) {
            FRAG_FILTER -> displayFragment(VegaNigeriaFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaNigeriaFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaNigeriaFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaNigeriaFgrnAssignLotFragment -> fragment.applyFilter(bundle)

        }
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            /* RMIN_GRADES -> displayFragment(
                 VegaNigeriaRMINGradesFragment.newInstance(model, fragment), true
             )*/
            SUMMARY -> {
                displayFragment(VegaNigeriaRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeRminLots>) {

        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (lotAddFragment) {
            is VegaNigeriaRMINAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaNigeriaRMINEditLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        model: VegaCoffeeRminProcessing,
        isPoSelection: Boolean
    ) {
        when (fragment) {

            SUBSTAGES -> {
                isLastBack = false
                displayFragment(
                    VegaNigeriaCocoaRminPoBomSubStagesSelectFragment.newInstance(
                        false,
                        model,
                        isPoSelection
                    ), true
                )
            }
            ADDLOT -> {
                isLastBack = false
                displayFragment(
                    VegaNigeriaRMINAddLotFragment.newInstance(false, model, isPoSelection), true
                )
            }
            /*FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaCoffeeRMINAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    true
                ), true
            )*/

        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaNigeriaFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaNigeriaFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaNigeriaRMINAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            /*is VegaCoffeeRMINAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }*/
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaNigeriaFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaNigeriaFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaNigeriaRMINAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            /*is VegaCoffeeRMINAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }*/
        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        fgrnMaterialCode: String,
        model: VegaCoffeeRminProcessing
    ) {
        when (fragment) {
            EDITLOT -> displayFragment(
                VegaNigeriaRMINEditLotFragment.newInstance(
                    fgrnMaterialCode,
                    model
                ), true
            )
            SHIFT ->
                displayFragment(VegaNigeriaRMINShiftSelectFragment.newInstance(flag, model), true)
        }
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing, flag: Boolean) {
        when (fragment) {
            /* ADDLOT -> {
                 displayFragment(
                     VegaIndiaCoffeeRminAddLotFragment.newInstance(flag, model), true
                 )
             }*/
            /*  SHIFT -> {
                  displayFragment(VegaIndiaCoffeeRminShiftSelectFragment.newInstance(flag, model), true)
              }
              Lot_List -> {
                  displayFragment(VegaIndiaCoffeeRminLotListFragment.newInstance(model, flag), true)
              }*/
            POBOM ->
                displayFragment(
                    VegaNigeriaCocoaRminPoBomSelectFragment.newInstance(
                        getString(R.string.po_details),
                        flag,
                        model
                    ),
                    true
                )
            RMIN_GRADES -> displayFragment(
                VegaNigeriaRMINGradesFragment.newInstance(model, fragment, flag), true
            )
            /*SUMMARY -> {
                displayFragment(VegaIndiaCoffeeRminSummaryFragment.newInstance(model), true)
            }*/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }

}
