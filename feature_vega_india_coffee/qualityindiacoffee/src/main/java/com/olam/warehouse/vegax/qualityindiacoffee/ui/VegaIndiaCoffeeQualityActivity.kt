package com.olam.warehouse.vegax.qualityindiacoffee.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPostResponse
import com.olam.warehouse.vegax.qualityindiacoffee.di.injectQualityFeature
import com.olam.warehouse.vegax.qualityindiacoffee.ui.offline.VegaIndiaCoffeeQualityOfflineFragment
import com.olam.warehouse.vegax.qualityindiacoffee.ui.params.VegaIndiaCoffeeQualityParameterFragment
import com.olam.warehouse.vegax.qualityindiacoffee.ui.weighbridge.VegaIndiaCoffeeQualityWBListFragment
import com.olam.warehouse.vegax.qualityindiacoffee.ui.weighment.VegaIndiaCoffeeQualityWeighmentTypeFragment
import com.olam.warehouse.vegax.qualityindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaIndiaCoffeeQualityActivity : HomeBaseActivity(), VegaIndiaCoffeeQualityWBListFragment.OnWeighBridgeListener,
    VegaIndiaCoffeeQualityParameterFragment.OnParamsListener, VegaIndiaCoffeeQualityOfflineFragment.OnOfflineListener,
    VegaIndiaCoffeeQualityWeighmentTypeFragment.CallBack, VegaIndiaCoffeeMtnrQualityAssignLotFragment.CallBack {
    private var wbDetails: VegaQualityWBDetails? = null
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private val mTAG = VegaIndiaCoffeeQualityActivity::class.java.canonicalName
    private lateinit var fragment: Fragment
    private var isParamValue = false
    private var batchNo: String = ""
    private var finalApprovalStatus: String = ""

    private var menu: Menu? = null
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var qualityParameter = ArrayList<VegaQualityParameter?>()
    private var offloadingData = VegaQualityWBDetails()

    private val vm: VegaIndiaCoffeeQualityViewModel by viewModel()

    override val layoutResourceId = R.layout.activity_vega_india_coffee_quality

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectQualityFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        vm.quality.observe(this, Observer { updateUI(it) })
        if (intent.hasExtra(UIUtils.QUALITY_DATA)) {
            wbDetails = intent.getParcelableExtra(UIUtils.QUALITY_DATA)
            if (wbDetails?.isCopy!!) {
                val type = if (wbDetails?.weighBridgeType.equals(PROCURE)) SUPPLIER else MTNR
                val bundle = Bundle().apply {
                    putString(WEIGHBRIDGE_LIST_TYPE, type)
                    putString(COPIED_WBID, wbDetails?.weighBridgeId)
                    putString(COPIED_MATERIAL, wbDetails?.materialCode)
                }
                displayFragment(WEIGHBRIDGE_LIST, bundle, false)
            } else
                transactionWBDetails(wbDetails)
        } else {
            displayFragment(
                WEIGHMENT_TYPE,
                Bundle().apply { putBoolean(UIUtils.MTNR, intent.getBooleanExtra(UIUtils.MTNR, false)) },
                false
            )
        }

    }

    override fun setQualityWBList(it: List<VegaQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }

    private fun displayFragment(openFragment: String, bundle: Bundle?, canReplace: Boolean) {
        val search = menu?.findItem(R.id.search)
        val searchView: SearchView? = search?.actionView as SearchView?
        when (openFragment) {
            WEIGHMENT_TYPE -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaIndiaCoffeeQualityWeighmentTypeFragment.newInstance()
            }
            WEIGHBRIDGE_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaIndiaCoffeeQualityWBListFragment.newInstance()
            }
            PARAMS_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaIndiaCoffeeQualityParameterFragment.newInstance()
            }
            QUALITY_OFFLINE_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaIndiaCoffeeQualityOfflineFragment.newInstance()
            }
            else -> fragment = VegaIndiaCoffeeQualityWBListFragment.newInstance()
        }
        bundle?.let { fragment.arguments = bundle }
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flQuality,
            allowBackStack = canReplace
        )
    }

    override fun onWeighBridgeClick(
        wbDetails: VegaQualityWBDetails?,
        copiedWbid: String,
        copiedMaterial: String
    ) {
        this.wbDetails = wbDetails
        isParamValue = false
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        val bundle = Bundle().apply {
            /* putString(TRUCK_NO, wbDetails?.vehicleNumber)
             putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
             putString(BATCH_NO, batchNo)
            putString(NET_WEIGHT, wbDetails?.netWeight)
            putString(TAR_WEIGHT, wbDetails?.bagWeight)
            putString(MATERIAL_NO, wbDetails?.materialCode)
            putString(CHALLAN, wbDetails?.challan)
            putString(ITEM, wbDetails?.item)
            putString(WEIGHBRIDGE_LIST_TYPE, wbDetails?.weighBridgeType)*/
            if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                putBoolean(IS_PARAMS_VALUE, true)
            else
                putBoolean(IS_PARAMS_VALUE, false)
            putString(COPIED_WBID, copiedWbid)
            putString(COPIED_MATERIAL, copiedMaterial)
            putParcelable(WEIGHBRIDGE, wbDetails)
        }
        displayFragment(PARAMS_LIST, bundle, true)
    }

    override fun onParamsProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        mergedBatchNumber: String,
        receivedWeight: String,
        finalApproval: String,
        materialCode: String,
        supplierCode: String,
        wbType: String?

    ) {
        if (wbId != null) {
            offloadingData.weighBridgeId = wbId
        }
        offloadingData.batchNumber = batchNo
        offloadingData.mergedBatchNumber = mergedBatchNumber
        offloadingData.finalApproval = finalApproval
        //        offloadingData.weighBridgeType == PROCURE
        offloadingData.weighBridgeType = wbType.toString()
        offloadingData.qualityFlag=true
        offloadingData.materialCode=materialCode
        offloadingData.receivedWeight = receivedWeight
        offloadingData.supplierCode=supplierCode
        offloadingData.item="0001"
        if (wbType.equals("STO")) {
            wbDetails?.finalApproval = finalApproval
            wbDetails?.batchNumber = batchNo
            wbDetails?.mergedBatchNumber = mergedBatchNumber
            wbDetails?.receivedWeight = receivedWeight
            wbDetails?.materialCode = materialCode
            wbDetails?.supplierCode = supplierCode
            wbDetails?.weighBridgeId = wbId.toString()
            wbDetails?.weighBridgeType = wbType.toString()
            wbDetails?.let { VegaIndiaCoffeeQualitySummaryFragment.newInstance(it, qualityParameter) }
                    ?.let { displayFragmentIndiaCoffee(it, true) }
        }else
        displayFragmentIndiaCoffee(VegaIndiaCoffeeQualitySummaryFragment.newInstance(offloadingData, qualityParameter), true)


        /* this.qualityParameter = qualityParameter
         finalApprovalStatus = finalApproval
         val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }

         if (AppUtils.isOnline()) {
             @Suppress("UNCHECKED_CAST")
             wbDetails?.qualityDetails = qtyParams as List<VegaQuality>
             wbDetails?.batchNumber = batchNo
             wbDetails?.finalApproval = finalApproval
             qualityPostList.clear()
             wbDetails?.let {
                 it.finalApproval = "R"
                 qualityPostList.add(it)
             }
             vm.postQualityParams(
                 VegaIndiaCoffeeQualityPost(
                     key = getCurrentKey(),
                     plant = getPlantDetails(),
                     lotDetails = qualityPostList
                 )
             )
         } else {
             //saveData(qualityParameter, wbId)
             moveToSuccessPage(wbId, "", "")
         }*/
    }

    fun saveData(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?
    ) {
        qualityParameter.forEachIndexed { index, it ->
            it?.wbid = wbId.toString()
            it?.position = index
            vm.saveQualityData(prepareVegaQualityData(it!!), batchNo)
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                it.data?.data?.currentWbid,
                                it.data?.data?.charg,
                                it.data?.data?.grnNumber
                            )
                            val batch = it.data?.data?.charg
                            val msg = it.data?.message
                            saveWB(it.data?.data?.currentWbid.toString(), batch.toString(), msg.toString(), 4)
                            saveData(qualityParameter, it.data?.data?.currentWbid)
                        }
                        else -> {
                            showErrorDialogWithFAQLink(this, "${it.data?.message}")
                            saveWB(wbDetails?.weighBridgeId.toString(), batchNo, it.data?.message.toString(), 3)
                            saveData(qualityParameter, wbDetails?.weighBridgeId)
                        }
                        //toast("${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(this, "${it.error}")

                    saveWB(wbDetails?.weighBridgeId.toString(), batchNo, it.error.toString(), 3)
                    saveData(qualityParameter, wbDetails?.weighBridgeId)
                    //toast("${it.error}")
                }
            }
        }
    }

    fun saveWB(weighBrideId: String, batchNo: String, message: String, status: Int) {
        wbDetails?.batchNumber = batchNo
        wbDetails?.wbTempId = weighBrideId
        wbDetails?.status = status
        wbDetails?.finalApproval = finalApprovalStatus
        wbDetails?.qualityFlag=true
        wbDetails?.message = message
        wbDetails?.let { vm.saveWBDB(it) }
    }

    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?) {
        val intent = Intent(this, SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            if (finalApprovalStatus == FNQUALITY)
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject))
        } else {
            if (finalApprovalStatus == FNQUALITY)
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success_offline))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject_offline))
        }
        if (charg?.isNotEmpty()!! && !grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created).plus(charg).plus("\n GRN No : ").plus(grnNo)
            )
        else if (charg.isNotEmpty() && grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created).plus(charg)
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(currentWbid))
        startActivity(intent)
        finish()
    }

    override fun onQualityOfflineClick() {
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
        }
        displayFragment(QUALITY_OFFLINE_LIST, bundle, true)
    }

    override fun onUpdateDeletedItem(wbid: String?) {
        vm.updateDeletedItem(wbid)
    }

    override fun onViewDetails(weighBridge: VegaQualityWBDetails?) {
        this.wbDetails = weighBridge
        isParamValue = true
        batchNo = if (weighBridge?.batchNumber.isNullOrEmpty()) "" else weighBridge?.batchNumber.toString()
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, weighBridge?.weighBridgeId)
            putBoolean(IS_PARAMS_VALUE, true)
            putString(BATCH_NO, batchNo)
            putString(NET_WEIGHT, weighBridge?.netWeight)
            putString(TAR_WEIGHT, weighBridge?.bagWeight)
            putString(MATERIAL_NO, weighBridge?.materialCode)
            putString(CHALLAN, weighBridge?.challan)
        }
        displayFragment(PARAMS_LIST, bundle, true)
    }

    fun transactionWBDetails(wbDetails: VegaQualityWBDetails?) {
        isParamValue = true
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        val bundle = Bundle().apply {
            /*putString(TRUCK_NO, wbDetails?.vehicleNumber)
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)*/
            putBoolean(IS_PARAMS_VALUE, true)
            putParcelable(WEIGHBRIDGE, wbDetails)
            /* putString(BATCH_NO, batchNo)
             putString(NET_WEIGHT, wbDetails?.netWeight)
             putString(TAR_WEIGHT, wbDetails?.bagWeight)
             putString(MATERIAL_NO, wbDetails?.materialCode)
             putString(CHALLAN, wbDetails?.challan)
             putString(ITEM, wbDetails?.item)
             putString(WEIGHBRIDGE_LIST_TYPE, wbDetails?.weighBridgeType)*/
        }
        displayFragment(PARAMS_LIST, bundle, false)
    }

    override fun replaceFragment(weightmentType: String) {
        if (intent?.hasExtra(QUALITY_OFFLINE)!!) {
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST, "")
                putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
            }
            displayFragment(QUALITY_OFFLINE_LIST, bundle, true)
        } else {
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
            }
            displayFragment(WEIGHBRIDGE_LIST, bundle, true)
        }


    }

    private fun displayFragmentIndiaCoffee(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flQuality,
            allowBackStack = flag
        )
    }

    override fun replaceFgrnFragment(
        frag: String,
        model: VegaQualityWBDetails,
        id: String,
        isThirdPartyMaterial: Boolean,
        currentMaterial: String
    ) {
        when (frag) {
            FRAG_CREATE_LOT -> {
//                val bundle = Bundle().apply {
//                }
                fragment = VegaIndiaCoffeeMtnrQualityAssignLotFragment.newInstance(model, id, isThirdPartyMaterial, currentMaterial)
//                bundle?.let { fragment.arguments = bundle }
                replaceFragment(
                    fragment,
                    mTAG,
                    allowStateLoss = true,
                    containerViewId = R.id.flQuality,
                    allowBackStack = true
                )

            }
        }
    }

    override fun replaceFGrnFragment(fragFilter: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragFilter) {
            FRAG_FILTER -> {
                fragment = VegaIndiaCoffeeMtnrFilterFragment.newInstance(bundle, fullFilter)
                replaceFragment(
                    fragment,
                    mTAG,
                    allowStateLoss = true,
                    containerViewId = R.id.flQuality,
                    allowBackStack = true
                )
            }
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flQuality)
        when (fragment) {
            is VegaIndiaCoffeeQualityParameterFragment -> fragment.updateLotDetails(bundle)
        }
    }
}
