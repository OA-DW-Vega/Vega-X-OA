package com.olam.warehouse.vegax.dispatchnigeria.ui

import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.WB_ID
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.dispatchnigeria.R
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.VegaNigeriaCocoaMtntLotListModel
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.VegaNigeriaCocoaWeightedAverageDetails
import com.olam.warehouse.vegax.dispatchnigeria.di.injectNigeriaCocoaMtntDispatchFeature
import com.olam.warehouse.vegax.dispatchnigeria.ui.params.VegaNigeriaCocoaMTNTQualityParameterFragment
import com.olam.warehouse.vegax.dispatchnigeria.ui.weighbridge.VegaNigeriaCocoaMtntSummaryFragment
import com.olam.warehouse.vegax.dispatchnigeria.ui.weighbridge.VegaNigeriaCocoaMtntWeighBridgeAddLotFragment
import com.olam.warehouse.vegax.dispatchnigeria.ui.weighbridge.VegaNigeriaCocoaMtntWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale.VegaNigeriaCocoaMtntConsignmentFragment
import com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale.VegaNigeriaCocoaMtntWeighScaleAddLotFragment
import com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale.VegaNigeriaCocoaMtntWeighScalePalletFragment
import com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale.VegaNigeriaCocoaMtntWeighScaleSummaryFragment
import com.olam.warehouse.vegax.dispatchnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNigeriaCocoaDispatchMtntActivity : HomeBaseActivity(),
    VegaNigeriaCocoaReplaceFragmentCallback, VegaNigeriaCocoaAddLotListener,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaNigeriaCocoaMtntWeighScalePalletFragment.CallBack,
    VegaNigeriaCocoaMTNTQualityParameterFragment.OnParamsListener {
    private val mTAG = VegaNigeriaCocoaDispatchMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_nigeria_cocoa_mtnt_dispatch
    private var menu: Menu? = null

    private var qualityParameter = ArrayList<VegaQualityParameter?>()
    private var finalApprovalStatus: String = ""
    private var wbDetails: VegaQualityWBDetails? = null
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private val vm: VegaNigeriaCocoaMtntViewModel by viewModel()
    private var batchNo: String = ""
    private var beanWtDiscGmValue: String = ""
    private var beanCountValue: String = ""
    private var moistureValue: String = ""
    private var slatyValue: String = ""
    private var mouldValue: String = ""
    private var addMixtureValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaCocoaMtntDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        vm.qualityNigeria.observe(this, Observer { updatequalityNigeriaUI(it) })
        displayFragment(VegaNigeriaCocoaMtntConsignmentFragment.newInstance(), false)
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

    override fun replaceFragment(
        receivingType: String,
        data: VegaCocoaDispatchWB?,
        datanew: String
    ) {
        when (receivingType) {
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaNigeriaCocoaMtntWeighScaleSummaryFragment.newInstance(data),
                true
            )
        }
    }

    override fun replaceFragmentNew(receivingType: String, data: VegaCocoaDispatchWB?) {
        when (receivingType) {
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaNigeriaCocoaMtntWeighScaleSummaryFragment.newInstance(data),
                true
            )
        }
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHBRIDGE -> displayFragment(
                VegaNigeriaCocoaMtntWeighbridgeTruckListFragment.newInstance(),
                true
            )
            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaNigeriaCocoaMtntWeighBridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            LOT_LIST -> displayFragment(
                VegaNigeriaCocoaLotListFragment.newInstance(data as VegaNigeriaCocoaMtntLotListModel),
                true
            )
            WEIGHBRIDGE_SUMMARY -> displayFragment(
                VegaNigeriaCocoaMtntSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            MTNT_WEIGHSCALE -> displayFragment(
                VegaNigeriaCocoaMtntConsignmentFragment.newInstance(),
                true
            )
            WEIGHSCALE_ADD_LOT -> displayFragment(
                VegaNigeriaCocoaMtntWeighScaleAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            ADD_WEIGHT -> displayFragment(
                VegaNigeriaCocoaMtntWeighScalePalletFragment.newInstance(data as VegaCocoaDispatchLots),
                true
            )
            /*FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )*/
            /* UPDATE_WEIGHT -> {
                 val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                 when (fragment) {
                     is VegaNigeriaCocoaMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(data as String)
                 }
             }*/
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaNigeriaCocoaMtntWeighScaleSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
        }
    }

    override fun replaceFragment(
        receivingType: String,
        materialCode: String,
        mergedBatchNumber: String,
        data: Any,
        dataNew: Any
    ) {
        //  val search = menu?.findItem(R.id.search)
        // val searchView: SearchView? = search?.actionView as SearchView?
        when (receivingType) {
            PARAMS_LIST -> {
                //   search?.isVisible = false
                // searchView?.gone()
                displayFragment(
                    VegaNigeriaCocoaMTNTQualityParameterFragment.newInstance(
                        materialCode,
                        mergedBatchNumber,
                        (data as VegaCocoaDispatchWB),
                        (dataNew as ArrayList<VegaNigeriaCocoaWeightedAverageDetails>)
                    ),
                    true
                )
            }
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
            is VegaNigeriaCocoaMtntWeighBridgeAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaNigeriaCocoaMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaNigeriaCocoaMtntWeighScaleSummaryFragment -> {
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
            is VegaNigeriaCocoaMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaNigeriaCocoaMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaNigeriaCocoaMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaNigeriaCocoaMtntWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun replaceFragment(receivingType: String, grossWeight: String, data: Any) {
        when (receivingType) {
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaNigeriaCocoaMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(
                        data as String,
                        grossWeight
                    )
                }
            }
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )

        }
    }

    override fun onParamsProceedQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        storageLocationCode: String
    ) {
        this.qualityParameter = qualityParameter
        finalApprovalStatus = finalApproval
        val qtyNigeriaParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            wbDetails?.qualityDetails = qtyNigeriaParams as List<VegaQuality>
            wbDetails?.batchNumber = batchNo
            wbDetails?.finalApproval = finalApproval
            qualityPostList.clear()
            wbDetails?.let { qualityPostList.add(it) }
            vm.postQualityParamsNigeria(
                VegaQualityNigeriaPost(
                    contactNumber = "",
                    driverName = "",
                    grnApplicable = false,
                    grnFlag = false,
                    grnNumber = "",
                    image = "",
                    imageUploadMsg = "",
                    key = getCurrentKey(),
                    message = "",
                    remarks = "",
                    plant = getPlantDetails(),
                    success = false,
                    transportVendorCode = "",
                    userName = "",
                    vehicleNumber = "",
                    vehicleType = "",
                    wayBillNo = "",
                    lotDetails = qualityPostList
                )
            )
        } else {

        }
    }

    private fun updatequalityNigeriaUI(response: Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    //  when (it.data?.success) {
                    when (true) {
                        true -> {
                            var batchDetails = it.data?.data?.batchDetails
                            batchDetails?.forEach { it1 ->
                                if (it1?.atnam.equals(B_MOULD4)) {
                                    mouldValue = it1?.atwrt.toString()
                                }
                                if (it1?.atnam.equals(NG_ADMIX)) {
                                    addMixtureValue = it1?.atwrt.toString()
                                }
                                if (it1?.atnam.equals(B_DCTBW1)) {
                                    beanWtDiscGmValue = it1?.atwrt.toString()
                                }
                                if (it1?.atnam.equals(ZNGCOCOA_ACTBW)) {
                                    beanCountValue = it1?.atwrt.toString()
                                }
                                if (it1?.atnam.equals(B_SL)) {
                                    slatyValue = it1?.atwrt.toString()
                                }
                                if (it1?.atnam.equals(B_MOIST)) {
                                    moistureValue = it1?.atwrt.toString()
                                }
                            }

                            /* this.qualityParameter = qualityParameter
         finalApprovalStatus = finalApproval
         val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }*/

                            val bundle = Bundle().apply {
                                putParcelableArrayList(APPROVE_QUALITY_DATA, qualityParameter)
                                putParcelableArrayList(
                                    APPROVE_BATCH_DETAILS,
                                    batchDetails as ArrayList<out Parcelable>
                                )
                                putString("FINAL_APP", wbDetails?.finalApproval)
                                putString(WB_ID, wbDetails?.weighBridgeId)
                                putString(BATCH_NO, batchNo)
                                putString(FINAL_APPROVAL, wbDetails?.finalApproval)
                                putString(STORAGELOCATION_CODE, wbDetails?.storageLocationCode)
                                putString(FLAG, "SUPPLIER")
                                putString(MOULD_VALUE, mouldValue)
                                putString(ADD_MIXTURE, addMixtureValue)
                                putString(BEAN_WT_GRAM, beanWtDiscGmValue)
                                putString(BEAN_COUNT, beanCountValue)
                                putString(SLATY, slatyValue)
                                putString(MOISTURE, moistureValue)
                            }
                            //callback.replaceFragment(WEIGHSCALE_SUMMARY, vm.dispatchWh)
                            //displayFragment(WEIGHSCALE_SUMMARY, bundle, true)
                            // displayFragment(SUMMARY_LIST, bundle, true)
                        }
                        else -> {
                            UIUtils.showErrorDialog(this, "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(this, "${it.error}")

                    // saveWB(wbDetails?.weighBridgeId.toString(), batchNo, it.error.toString(), 3)
                    // saveData(qualityParameter, wbDetails?.weighBridgeId)
                }
            }
        }
    }
}
