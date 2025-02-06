package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.common.VegaTrackTraceListener
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWLotManualModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.di.injectGhanaOffloadingFeature
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.VegaGhanaCocoaMtnrTypeSelectFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighbridge.VegaGhanaCocoaMtnrWBListFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighbridge.VegaGhanaCocoaMtnrWbConsignmentFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighbridge.VegaGhanaCocoaMtnrWbWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighscale.VegaGhanaCocoaMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighscale.VegaGhanaCocoaMtnrWSListFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighscale.VegaGhanaCocoaMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.offline.VegaGhanaCocoaMTNROffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.offline.VegaGhanaCocoaOfflineMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.offline.VegaGhanaCocoaOffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.supplier.VegaGhanaCocoaOffloadingSupplierFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.supplier.VegaGhanaCocoaOffloadingSupplierSummaryFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.supplier.VegaGhanaCocoaOffloadingWeightEntryFragment
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaGhanaCocoaCocoaOffloadingActivity : HomeBaseActivity(), VegaGhanaCocoaOffloadingSupplierFragment.CallBack,
    VegaGhanaCocoaMtnrWBListFragment.OnWeighBridgeListener,
    VegaGhanaCocoaMtnrWSListFragment.OnWeighScaleListener,
    VegaGhanaCocoaOffloadingWeightEntryFragment.CallBackAddBags, VegaGhanaCocoaOffloadingOfflineSummary.CallBack,
    VegaGhanaCocoaOffloadReplaceFragmentCallback, VegaGhanaCocoaMtnrWbConsignmentFragment.CallBack,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaGhanaCocoaMTNROffloadingOfflineSummary.CallBack,
    VegaCocoaAddPalletFragment.CallBackPallet, VegaGhanaCocoaAddLotListener, VegaTrackTraceListener {
    private val mTAG = VegaGhanaCocoaCocoaOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_ghana_cocoa_offloading
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    var procurementType = ""
    var complaintType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        procurementType = intent.getStringExtra(Constants.PROCUREMENT_TYPE)?:""
        complaintType = intent.getStringExtra(Constants.COMPLAINT_TYPE)?:""
//        displayFragment(VegaGahanaCocoaSelectModuleWiseFragment.newInstance(), false)
        displayFragment(VegaGhanaCocoaOffloadTypeSelectFragment.newInstance(), false)
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

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            MTNR -> displayFragment(VegaGhanaCocoaMtnrTypeSelectFragment.newInstance(), true)
            SUPPLIER -> displayFragment(
                VegaGhanaCocoaOffloadingSupplierFragment.newInstance(
                    VegaReceiving(),
                    ArrayList<VegaEcuadorOffloadingBagMaterial>(), procurementType, complaintType,
                ), true
            )
            FRAG_ADD_BAG_WEIGHT_SUPPLIER -> displayFragment(VegaGhanaCocoaOffloadingWeightEntryFragment.newInstance(data as Bundle), true)

            ADD_WEIGHT -> displayFragment(
                VegaGhanaCocoaMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaGhanaCocoaMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
//            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
//                VegaGhanaCocoaMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
//                true
//            )
            WEIGHSCALE -> displayFragment(VegaGhanaCocoaMtnrWSListFragment.newInstance(), true)
//            MTNT_WEIGHSCALE -> displayFragment(
//                VegaGhanaCocoaMtnrConsignmentFragment.newInstance(),
//                true
//            )
            WEIGHBRIDGE_WEIHSCALE -> displayFragment(
                VegaGhanaCocoaMtnrWBListFragment.newInstance(),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaGhanaCocoaMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }

            }
        }
    }

    override fun replaceFragment(receivingType: String, data: Any, datas: Any) {
       when(receivingType)
       {
           SUPPLIER -> displayFragment(
               VegaGhanaCocoaOffloadingSupplierFragment.newInstance(
                   VegaReceiving(),
                   ArrayList<VegaEcuadorOffloadingBagMaterial>(),
                   procurementType, complaintType,
                   data as VegaGRNDWLotManualModel
               ), true
           )

           com.olam.warehouse.vegax.mtntghanacocoa.utils.LOT_LIST -> displayFragment(
               VegaGhanaCocoaLotListDWGrn.newInstance(
                   data as VegaGRNDWLotManualModel,
                   datas as String
               ), true
           )
       }
    }

    override fun replaceFragment(
        receivingType: String,
        data: Any,
        rejectList: HashMap<String, String>,
        acceptedBagCount: String
    ) {
        TODO("Not yet implemented")
    }

    override fun replaceFragment(
        receivingType: String,
        data: Any,
        wbDetails: Any,
        rejectList: HashMap<String, String>,
        acceptedBagCount: String
    ) {
        when (receivingType) {
            MTNR_WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaGhanaCocoaMtnrWbWeighScaleSummaryFragment.newInstance(
                    data as VegaCoffeeReceiving,
                    wbDetails as VegaQualityWBDetails,
                    rejectList,
                    acceptedBagCount,
                    emptyList<VegaCoffeeReceiveLots>() as ArrayList<VegaCoffeeReceiveLots>
                ),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaGhanaCocoaMtnrWeighScaleSummaryFragment.newInstance(
                    data as VegaCoffeeReceiving,
                    wbDetails as VegaQualityWBDetails,
                    rejectList,
                    acceptedBagCount
                ),
                true
            )
        }

    }

    override fun replaceFragment(
        receivingType: String,
        data: Any,
        wbDetails: Any,
        rejectList: HashMap<String, String>,
        acceptedBagCount: String,
        batchList: Any
    ) {
        when (receivingType) {
            MTNR_WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaGhanaCocoaMtnrWbWeighScaleSummaryFragment.newInstance(
                    data as VegaCoffeeReceiving,
                    wbDetails as VegaQualityWBDetails,
                    rejectList,
                    acceptedBagCount,
                    batchList as ArrayList<VegaCoffeeReceiveLots>
                ),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaGhanaCocoaMtnrWeighScaleSummaryFragment.newInstance(
                    data as VegaCoffeeReceiving,
                    wbDetails as VegaQualityWBDetails,
                    rejectList,
                    acceptedBagCount
                ),
                true
            )
        }
    }

    override fun replaceFragment(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails) {
        when (fragment) {
//            MTNR_WEIGHSCALE_OFFLINE_SUMMARY ->
//                displayFragment(VegaGhanaCocoaMtnrWbWeighScaleSummaryFragment.newInstance(flag,wbDetails), true)
        }
    }

    override fun navigateToSummary(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails) {
        when (fragment) {
//            MTNR_WEIGHSCALE_OFFLINE_SUMMARY -> displayFragment(
//                VegaGhanaCocoaMtnrWbWeighScaleSummaryFragment.newInstance(flag,wbDetails), true)
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            OFFLOADING_OFFLINE -> displayFragment(VegaGhanaCocoaOffloadingOfflineSummary.newInstance(), true)
            MTNR_OFFLOADING_OFFLINE -> displayFragment(VegaGhanaCocoaMTNROffloadingOfflineSummary.newInstance(), true)
            VEGA_MODULE->
                displayFragment(VegaGhanaCocoaOffloadTypeSelectFragment.newInstance(),
                    true)
            DW_MODULE->
                displayFragment(VegaGahnaCocoaCocoaGrnDwWeighBridgeAddLotFragment.newInstance(),
                    true)
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>,
        bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
        grnPrice:String,
        isOffline:Boolean
    ) {
        when (moveFrag) {
            OFFLOADING_SUMMARY_FRAG -> displayFragment(
                VegaGhanaCocoaOffloadingSupplierSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>,
                    bagList,
                    grnPrice,isOffline
                ), true
            )
        }
    }

    override fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaOffloadingSupplierFragment -> {
//                fragment.updateBagWeight(bagMaterial)
            }
        }
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
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
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainer)
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaGhanaCocoaOffloadingWeightEntryFragment -> {2
                /*No Implementation*/
            }
            is VegaGahnaCocoaCocoaGrnDwWeighBridgeAddLotFragment->
            {
                /*No Implementation*/
            }
            is VegaGahanaCocoaSelectModuleWiseFragment->
            {
                if (fragment1 is VegaGhanaCocoaOffloadTypeSelectFragment ||
                    fragment1 is VegaGahnaCocoaCocoaGrnDwWeighBridgeAddLotFragment)
                {

                }
                else {
                    super.onBackPressed()
                }

            }
            is VegaGhanaCocoaOffloadingSupplierFragment -> {
                /*No Implementation*/
            }
            is VegaGhanaCocoaMtnrWeighScaleSummaryFragment -> {
                /*No Implementation*/
            }
            is VegaGhanaCocoaMtnrTypeSelectFragment -> {
                when(fragment1){
                    is VegaGhanaCocoaMtnrConsignmentFragment -> {
                        /*No Implementation*/
                    }
                }
            }
            is VegaGhanaCocoaMtnrWBListFragment -> {
                when(fragment1){
                    is VegaGhanaCocoaMtnrWbConsignmentFragment -> {
                        /*No Implementation*/
                    }
                }
            }
            is VegaGhanaCocoaMtnrWSListFragment -> {
                when(fragment1){
                    is VegaGhanaCocoaMtnrConsignmentFragment -> {
                        /*No Implementation*/
                    }
                }
            }
            is VegaGhanaCocoaOffloadTypeSelectFragment -> {
                when (fragment1) {
                    is VegaGhanaCocoaMtnrTypeSelectFragment -> {
                        /*No Implementation*/
                    }
                    is VegaGhanaCocoaOffloadingSupplierFragment -> {
                        /*No Implementation*/
                    }
                    else -> finish()
                }
            }
            is VegaGhanaCocoaMtnrWeighScalePalletFragment -> {
                /*No Implementation*/
            }
            is VegaGhanaCocoaMtnrConsignmentFragment -> {
                when (fragment1) {
                    is VegaGhanaCocoaMtnrWeighScaleSummaryFragment -> fragment.getBack()
                }
            }
            is VegaGhanaCocoaMtnrWbConsignmentFragment -> {
                when (fragment1) {
                    is VegaGhanaCocoaMtnrWbWeighScaleSummaryFragment -> fragment.getBack()
                }
            }
            else -> super.onBackPressed()
        }
    }

    /*override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {
            is VegaCoffeeMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaCoffeeMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoffeeMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }*/

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun onWeighBridgeClick(wbDetails: VegaQualityWBDetails?) {
        displayFragment(
            VegaGhanaCocoaMtnrWbConsignmentFragment.newInstance(
                wbDetails
            ), true
        )
    }

    override fun onWeighScaleClick(wbDetails: VegaQualityWBDetails?) {
        displayFragment(
            VegaGhanaCocoaMtnrConsignmentFragment.newInstance(
                wbDetails
            ), true
        )
    }

    override fun setQualityWBList(it: List<VegaQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaCoffeeReceivingMtnrWithLots) {
        when (moveFrag) {
            OFFLOADING_OFFLINE_SUMMARY_FRAG -> displayFragment(
                VegaGhanaCocoaOfflineMtnrWeighScaleSummaryFragment.newInstance(
                    receivingData
                ), true
            )
        }

    }

    override fun addedLots(lots: ArrayList<VegaGRNDWLotManualModel>) {
        supportFragmentManager.popBackStackImmediate()
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when(fragment)
        {
            is VegaGahnaCocoaCocoaGrnDwWeighBridgeAddLotFragment->
            {
                fragment.updateLotList(lots)
            }
        }
    }

    override fun isVendor(flag: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaOffloadingSupplierFragment -> {
//                fragment.isVendor(flag)
            }
        }
    }

    override fun isComplaint(status: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaOffloadingSupplierFragment -> {
                fragment.isComplaint(status)
            }
        }
    }

    override fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaOffloadingSupplierFragment -> {
                fragment.updateSourceLotDetails(sourceLotDetails)
            }
        }
    }

    override fun updateTransactionIdDetails(transactionIdDetails: TrackTraceTransactionIdDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaOffloadingSupplierFragment -> {
                fragment.updateFarmerlessTransactionDetails(transactionIdDetails)
            }
        }
    }

    override fun updateFarmerDetails(farmerListDetails: ArrayList<TrackTraceFarmerModel>) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaGhanaCocoaOffloadingSupplierFragment -> {
                fragment.updateFarmerListDetails(farmerListDetails)
            }
        }
    }

}
