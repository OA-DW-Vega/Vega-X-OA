package com.olam.warehouse.vegax.offloadingcameroon.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.common.VegaTrackTraceListener
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.di.injectCameroonOffloadingFeature
import com.olam.warehouse.vegax.offloadingcameroon.ui.mtnr.VegaCameroonMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingcameroon.ui.mtnr.VegaCameroonMtnrTypeSelectFragment
import com.olam.warehouse.vegax.offloadingcameroon.ui.mtnr.VegaCameroonMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingcameroon.ui.offline.VegaCameroonOffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadingcameroon.ui.reprint.VegaCameroonOffloadingReceiptReprintFragment
import com.olam.warehouse.vegax.offloadingcameroon.ui.supplier.VegaCameroonOffloadingSupplierFragment
import com.olam.warehouse.vegax.offloadingcameroon.ui.supplier.VegaCameroonOffloadingSupplierSummaryFragment
import com.olam.warehouse.vegax.offloadingcameroon.ui.supplier.VegaCameroonOffloadingWeightEntryFragment
import com.olam.warehouse.vegax.offloadingcameroon.utils.*

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaCameroonOffloadingActivity : HomeBaseActivity(), VegaCameroonOffloadingSupplierFragment.CallBack,
    VegaCameroonOffloadingWeightEntryFragment.CallBackAddBags, VegaCameroonOffloadingOfflineSummary.CallBack,
    VegaCameroonOffloadReplaceFragmentCallback,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCameroonOffloadingTruckListFragment.CallBack,
    VegaCocoaAddPalletFragment.CallBackPallet, VegaTrackTraceListener {
    private val mTAG = VegaCameroonOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_cameroon_offloading
    var procurementType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        procurementType = intent.getStringExtra(Constants.PROCUREMENT_TYPE)?:""
        if(intent.hasExtra(UIUtils.REPRINT_OFFLOADING_RECEIPT)){
            displayFragment(VegaCameroonOffloadingReceiptReprintFragment.newInstance("Receipt"), false)
        } else if(intent.hasExtra(UIUtils.REPRINT_OFFLOADING_TICKET)){
            displayFragment(VegaCameroonOffloadingReceiptReprintFragment.newInstance("Ticket"), false)
        }else {displayFragment(VegaCameroonOffloadTypeSelectFragment.newInstance(), false)}
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
            MTNR -> displayFragment(VegaCameroonMtnrConsignmentFragment.newInstance(), true)
            SUPPLIER ->
                displayFragment(
                    VegaCameroonOffloadingTruckListFragment.newInstance(
                        data as VegaOffloadingTrucks
                    ), false
                )

            FRAG_ADD_BAG_WEIGHT_SUPPLIER -> displayFragment(
                VegaCameroonOffloadingWeightEntryFragment.newInstance(data as Bundle),
                true
            )

            ADD_WEIGHT -> {
                displayFragment(
                    VegaCameroonMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                    true
                )
                /* initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                        when (fragment) {
                            is VegaCameroonMtnrWeighScalePalletFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaSweepingWeightEntryFragment -> {
                                fragment.updateBtWeight(btValue)
                            }
                        }
                    }

                })*/
            }
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCameroonMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaCameroonMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCameroonMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }

            }
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            OFFLOADING_OFFLINE -> displayFragment(VegaCameroonOffloadingOfflineSummary.newInstance(), true)
        }
    }

    
    override fun replaceFragment(
        paramsListFrag: String,
        data: VegaOffloadingTrucks,
        plantId: String,
        plantDetails: Plant
    ) {
        when (paramsListFrag) {
            TRUCK_LIST_FRAG -> {
                displayFragment(
                    VegaCameroonOffloadingSupplierFragment.newInstance(
                        VegaReceiving(),
                        ArrayList<VegaEcuadorOffloadingBagMaterial>(),
                        data, plantId, plantDetails, procurementType
                    ), true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                        when (fragment) {
                            is VegaCameroonOffloadingSupplierFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaCameroonOffloadingWeightEntryFragment -> {
                                fragment.updateBtWeight(btValue)
                            }
                        }
                    }

                })
            }
        }

    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>,
        bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
        receivingPlant: String?,
        otLotNumber: String?,
        plantDetails: Plant
    ) {
        when (moveFrag) {
            OFFLOADING_SUMMARY_FRAG -> displayFragment(
                VegaCameroonOffloadingSupplierSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>,
                    bagList,
                    receivingPlant,
                    otLotNumber,
                    plantDetails
                ), true
            )
        }
    }

    override fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonOffloadingSupplierFragment -> {
                fragment.updateBagWeight(bagMaterial)
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
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        if (fragment != null) {
            when (fragment) {
                is VegaCameroonOffloadingWeightEntryFragment -> {
                }
                is VegaCameroonOffloadingSupplierFragment -> {
                }
                is VegaCameroonMtnrWeighScaleSummaryFragment -> {
                }
                is VegaCameroonOffloadTypeSelectFragment -> {
                    when (fragment1) {
                        is VegaCameroonMtnrTypeSelectFragment -> {
                        }
                        is VegaCameroonOffloadingSupplierFragment -> {
                        }
                        is VegaCameroonOffloadingTruckListFragment -> {
                        }
                        else -> finish()
                    }
                }
                is VegaCameroonMtnrWeighScalePalletFragment -> {
                    closeSocket()
                }
                is VegaCameroonMtnrConsignmentFragment -> {
                    when (fragment1) {
                        is VegaCameroonMtnrWeighScaleSummaryFragment -> fragment.getBack()
                    }
                }
                is VegaCameroonOffloadingTruckListFragment -> {
                    when (fragment1) {
                        is VegaCameroonOffloadingSupplierFragment -> {
                        }
                        else -> super.onBackPressed()
                    }
                    closeSocket()
                }
                else -> super.onBackPressed()
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaCameroonOffloadingSupplierFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>,
        bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
    ) {

    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }

   override fun isVendor(flag: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonOffloadingSupplierFragment -> {
//                fragment.isVendor(flag)
            }
        }
    }

    override fun isComplaint(status: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonOffloadingSupplierFragment -> {
                fragment.isComplaint(status)
            }
        }
    }

    override fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonOffloadingSupplierFragment -> {
                fragment.updateSourceLotDetails(sourceLotDetails)
            }
        }
    }

    override fun updateTransactionIdDetails(transactionIdDetails: TrackTraceTransactionIdDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonOffloadingSupplierFragment -> {
                fragment.updateFarmerlessTransactionDetails(transactionIdDetails)
            }
        }

    }

    override fun updateFarmerDetails(farmerListDetails: ArrayList<TrackTraceFarmerModel>) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCameroonOffloadingSupplierFragment -> {
                fragment.updateFarmerListDetails(farmerListDetails)
            }
        }
    }
}
