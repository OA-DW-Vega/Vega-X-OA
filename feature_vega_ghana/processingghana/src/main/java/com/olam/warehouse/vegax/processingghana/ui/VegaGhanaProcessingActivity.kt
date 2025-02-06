package com.olam.warehouse.vegax.processingghana.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminItems
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminLots
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.di.injectGhanaProcessingFeature
import com.olam.warehouse.vegax.processingghana.ui.fgrn.*
import com.olam.warehouse.vegax.processingghana.ui.fgrn.offline.VegaGhanaFgrnOfflineSummaryFragment
import com.olam.warehouse.vegax.processingghana.ui.fgrn.offline.VegaGhanaProcessingFgrnOfflineSummary
import com.olam.warehouse.vegax.processingghana.ui.rmin.*
import com.olam.warehouse.vegax.processingghana.ui.rmin.offline.VegaGhanaOfflineRminSummaryFragment
import com.olam.warehouse.vegax.processingghana.ui.rmin.offline.VegaGhanaProcessingRminOfflineSummary
import com.olam.warehouse.vegax.processingghana.utils.*

class VegaGhanaProcessingActivity : HomeBaseActivity(),
    VegaGhanaProcessingSelectTypeFragment.CallBack,
    VegaGhanaFgrnPendingFragment.CallBack, VegaGhanaFgrnAddWeightAndLotsFragment.CallBack,
    VegaGhanaFgrnAssignLotFragment.CallBack, VegaGhanaFgrnFilterFragment.CallBack,
    VegaGhanaFgrnGradesFragment.CallBack, VegaGhanaFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaGhanaFgrnPODetailsFragment.CallBack, VegaGhanaFgrnShiftSelectionFragment.CallBack,
    VegaGhanaFgrnSummaryFragment.CallBack, VegaGhanaRMINSelectPoFragment.CallBack,
    VegaGhanaProcessingFgrnOfflineSummary.CallBack,
    VegaGhanaRMINGradesFragment.CallBack, VegaGhanaRMINAddLotFragment.CallBack,
    VegaGhanaFgrnOfflineRminListFragment.CallBack,
    GhanaAddLotsListener, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaGhanaFgrnBagConsumptionFragment.CallBack, VegaGhanaRMINAddWeightAndLotsFragment.CallBack,
    VegaGhanaRMINShiftSelectFragment.CallBack, VegaGhanaRMINEditLotFragment.CallBack,
    VegaGhanaProcessingRminOfflineSummary.CallBack,
    VegaGhanaRminSummaryFragment.CallBack,
    VegaProcessingGhnaRminPoBomSelectFragment.CallBack {
    override val layoutResourceId: Int = R.layout.activity_vega_ghana_processing
    private val mTAG = VegaGhanaProcessingActivity::class.java.canonicalName
    private var isLastBack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent?.hasExtra(Constants.TRANSACTIONID) == true) {
            if (intent?.getStringExtra(Constants.TRANSACTIONID)?.equals("RMIN") == true) {
                displayFragment(VegaGhanaRMINSelectPoFragment.newInstance(), true)
            } else {
                displayFragment(VegaGhanaFgrnPODetailsFragment.newInstance(), true)
            }
        } else {
            displayFragment(VegaGhanaProcessingSelectTypeFragment.newInstance(), false)
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
            is VegaGhanaFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }

            is VegaGhanaRMINAddLotFragment -> {
                fragment.saveLotDetails()
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }

            is VegaGhanaRMINAddWeightAndLotsFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }

            is VegaGhanaRMINEditLotFragment -> {
                fragment.moveToShiftSelect()
            }

            else -> super.onBackPressed()
        }
    }

    override fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing) {
        when (fragment) {
            RMIN -> {
                displayFragment(VegaGhanaRMINSelectPoFragment.newInstance(), true)
            }

            FGRN -> {
                displayFragment(VegaGhanaFgrnPODetailsFragment.newInstance(), true)
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
        material: String,
        vegaStage: VegaProcessingStage
    ) {
        when (fragment) {
            FRAG_CREATE_LOT -> {
                displayFragment(
                    VegaGhanaFgrnAssignLotFragment.newInstance(model, id, isThirdPartyMaterial, material, vegaStage),
                    true
                )
            }

            FRAG_SHIFT -> {
                displayFragment(VegaGhanaFgrnShiftSelectionFragment.newInstance(model, vegaStage), true)
            }
        }
    }

    override fun replaceFgrnFragment(
        fragment: String,
        model: VegaCoffeeFgrnItems,
        id: String,
        vegaStage: VegaProcessingStage,
        bagItem: VegaCoffeeFgrnGradesMatrialWeights
    ) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            FRAG_SUMMARY -> displayFragment(
                VegaGhanaFgrnSummaryFragment.newInstance(
                    model,
                    vegaStage,
                    bagItem
                ), true
            )

        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            RMIN_OFFLINE -> displayFragment(
                VegaGhanaProcessingRminOfflineSummary.newInstance(),
                true
            )
            //  FGRN_OFFLINE -> displayFragment(VegaGhanaProcessingFgrnOfflineSummary.newInstance(), true)
        }
    }

    override fun replaceFragment(
        fragment: String,
        isPoSelect: Boolean,
        model: VegaCocoaRminProcessing
    ) {
        when (fragment) {
            POBOM ->
                displayFragment(
                    VegaProcessingGhnaRminPoBomSelectFragment.newInstance(
                        getString(R.string.po_details),
                        isPoSelect,
                        model
                    ),
                    true
                )
        }
    }

    override fun replaceFragment(fragment: String, data: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaGhanaRMINAddLotFragment -> {
//                fragment.updateAddWeight(data)
            }

            is VegaGhanaRMINEditLotFragment -> {
//                fragment.updateAddWeight(data)
            }
        }
    }

    /*override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing, id: String) {
        displayFragment(VegaCoffeeRMINShiftSelectFragment.newInstance(false, model), true)
    }*/

    override fun replaceFgrnFragment(
        fragment: String,
        model: VegaCoffeeFgrnItems,
        id: String,
        vegaStage: VegaProcessingStage
    ) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaGhanaFgrnPODetailsFragment -> displayFragment(
                        VegaGhanaFgrnPendingFragment.newInstance(model, vegaStage),
                        true
                    )
                }
            }

            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaGhanaFgrnPODetailsFragment -> displayFragment(
                        VegaGhanaFgrnGradesFragment.newInstance(
                            model,
                            id, vegaStage
                        ), true
                    )

                    is VegaGhanaFgrnPendingFragment -> displayFragment(
                        VegaGhanaFgrnGradesFragment.newInstance(
                            model,
                            id, vegaStage
                        ), true
                    )
                }
            }

            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(VegaGhanaFgrnAddWeightAndLotsFragment.newInstance(model, id, false, vegaStage), true)
            }

            FRAG_RMIN_LIST -> {
                isLastBack = false
                displayFragment(VegaGhanaFgrnOfflineRminListFragment.newInstance(model, id, false, vegaStage), true)
            }

            FRAG_BAG_CONSUMP -> {
                displayFragment(VegaGhanaFgrnBagConsumptionFragment.newInstance(model), true)
            }

            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaGhanaFgrnAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    id,
                    true, vegaStage
                ), true
            )
        }
    }

    override fun replaceFragment(moveFrag: String, vegaStage: VegaProcessingStage) {
        when (moveFrag) {
            FGRN_OFFLINE -> displayFragment(
                VegaGhanaProcessingFgrnOfflineSummary.newInstance(
                    vegaStage
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
                displayFragment(VegaGhanaRMINShiftSelectFragment.newInstance(flag, model), true)
            }

            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaGhanaRMINAddWeightAndLotsFragment.newInstance(
                        model,
                        false,
                        materialCode,
                        batchNumer
                    ), true
                )
            }
            //  SUMMARY -> displayFragment(VegaGhanaRminSummaryFragment.newInstance(model,isPoselection), true)
        }
    }

    override fun replaceSummaryFragment(
        fragment: String,
        flag: Boolean,
        model: VegaCoffeeRminProcessing, isPoselection: Boolean
    ) {
        when (fragment) {
            SUMMARY -> displayFragment(
                VegaGhanaRminSummaryFragment.newInstance(
                    model,
                    isPoselection
                ), true
            )
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
                VegaGhanaRMINLotListFragment.newInstance(
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
            FRAG_FILTER -> displayFragment(VegaGhanaFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaGhanaFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaGhanaFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaGhanaFgrnAssignLotFragment -> fragment.applyFilter(bundle)

        }
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            RMIN_GRADES -> displayFragment(
                VegaGhanaRMINGradesFragment.newInstance(model, fragment), true
            )
            /*SUMMARY -> {
                displayFragment(VegaGhanaRminSummaryFragment.newInstance(model), true)
            }*/
        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeRminLots>) {

        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (lotAddFragment) {
            is VegaGhanaRMINAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }

            is VegaGhanaRMINEditLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        model: VegaCoffeeRminProcessing,
        isPoselection: Boolean
    ) {
        when (fragment) {
            ADDLOT -> {
                isLastBack = false
                displayFragment(
                    VegaGhanaRMINAddLotFragment.newInstance(false, model, isPoselection), true
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
            is VegaGhanaFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }

            is VegaGhanaFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }

            is VegaGhanaRMINAddWeightAndLotsFragment -> {
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
            is VegaGhanaFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingFGRNMaterial(bagMaterial))
            }

            is VegaGhanaFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingFGRNMaterial(bagMaterial))
            }

            is VegaGhanaRMINAddWeightAndLotsFragment -> {
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
            EDITLOT -> displayFragment(VegaGhanaRMINEditLotFragment.newInstance(fgrnMaterialCode, model), true)
            SHIFT ->
                displayFragment(VegaGhanaRMINShiftSelectFragment.newInstance(flag, model), true)
        }
    }

    override fun replaceFgrnFragment(fragment: String, fgrnItem: VegaCoffeeFgrnItems, fgrnId: String) {
        TODO("Not yet implemented")
    }

    override fun replaceFragment(
        fragment: String,
        rminLots: List<VegaGhanaOfflineRminLots>,
        rminItems: List<VegaGhanaOfflineRminItems>,
        poNo: String, stage: String
    ) {
        when (fragment) {
            OFFLINE_RMIN_SUMMARY_FRAG -> displayFragment(
                VegaGhanaOfflineRminSummaryFragment.newInstance(
                    rminLots,
                    rminItems,
                    poNo,
                    stage
                ), true
            )
        }
    }

    override fun replaceFragment(
        fragment: String,
        rminLots: List<VegaGhanaOfflineFgrnProcessLotDetails>,
        lotData: List<VegaGhanaOfflineFgrnData>,
        data: String, model: VegaProcessingStage
    ) {
        when (fragment) {
            OFFLINE_FGRN_SUMMARY_FRAG -> displayFragment(
                VegaGhanaFgrnOfflineSummaryFragment.newInstance(
                    rminLots, lotData, model
                ), true
            )
        }
    }

}
