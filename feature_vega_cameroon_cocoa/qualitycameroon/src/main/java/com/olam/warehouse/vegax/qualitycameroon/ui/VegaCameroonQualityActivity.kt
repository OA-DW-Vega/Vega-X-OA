package com.olam.warehouse.vegax.qualitycameroon.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualitycameroon.R
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.*
import com.olam.warehouse.vegax.qualitycameroon.di.injectCameroonQualityFeature
import com.olam.warehouse.vegax.qualitycameroon.ui.lot.VegaCameroonQualityMtnrWBLotListFragment
import com.olam.warehouse.vegax.qualitycameroon.ui.offline.VegaCameroonQualityOfflineSummary
import com.olam.warehouse.vegax.qualitycameroon.ui.params.VegaCameroonMtnrQualityParameterFragment
import com.olam.warehouse.vegax.qualitycameroon.ui.params.VegaCameroonQualityParameterFragment
import com.olam.warehouse.vegax.qualitycameroon.ui.weighbridge.VegaCameroonMtnrQualityWBListFragment
import com.olam.warehouse.vegax.qualitycameroon.ui.weighbridge.VegaCameroonQualityWBListFragment
import com.olam.warehouse.vegax.qualitycameroon.ui.weighment.VegaCameroonQualityWeighmentTypeFragment
import com.olam.warehouse.vegax.qualitycameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaCameroonQualityActivity : HomeBaseActivity(), VegaCameroonQualityWBListFragment.OnWeighBridgeListener,
    VegaCameroonQualityParameterFragment.OnParamsListener, VegaCameroonMtnrQualityParameterFragment.OnParamsListener,
    VegaCameroonQualityMtnrWBLotListFragment.OnLotListener, VegaCameroonMtnrQualityWBListFragment.OnWeighBridgeListener,
    VegaCameroonQualityWeighmentTypeFragment.CallBack, VegaCameroonQualityOfflineSummary.CallBack,
    VegaCameroonQualitySummaryFragment.OnSummaryParamsListener {
    private val mTAG = VegaCameroonQualityActivity::class.java.canonicalName
    private lateinit var fragment: Fragment
    private var isParamValue = false
    private var batchNo: String = ""
    private var finalApprovalStatus: String = ""
    private val vm: VegaCameroonQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.activity_vega_cameroon_quality

    private var menu: Menu? = null
    private var wbDetails: VegaQualityWBDetails? = null
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var qualityParameter = ArrayList<VegaQualityParameter?>()

    private var lotDetails: VegaCoffeeLot? = null
    private var mQualityLotList: MutableList<VegaCoffeeLot> = mutableListOf()
    private var mtnrlotItems = arrayListOf<VegaCoffeeLot>()

    private var jsonData = mutableListOf<String>()
    private var processList = mutableListOf<String>()
    private var grnProcessType :String? =""
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonQualityFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        vm.quality.observe(this, Observer { updateUI(it) })
        vm.miscellaneousItems.observe(this, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        vm.qualityMtnr.observe(this, Observer { updateMtnrUI(it) })

        vm.thirdPartyMaterial.observe(this, Observer {
            thirdPartyMaterials = it
        })
        vm.getThirdPartyMaterials()

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
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST_TYPE, SUPPLIER)
            }
            displayFragment(WEIGHMENT_TYPE, null, false)
        }
    }

    private fun updateMtnrUI(response: Resource<GenericReqAndResp<VegaCameroonQualityParamPost>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToMtnrSuccessPage(
                                it.data?.data?.grnNumber
                            )

                        }
                        else -> {
                            UIUtils.showErrorDialog(this, "${it.data?.message}")
                            it.data?.data?.let {
                                it.lotDetails?.let {
                                    it.forEach {
                                        it.let { it1 ->
                                            mtnrlotItems.forEach { it2 ->
                                                if (it.batchNumber.equals(it1.batchNumber)) it2.qualityFlag =
                                                    it1.qualityFlag
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(this, "${it.error}")


                }
            }
        }
    }


    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        jsonData.forEach {
            if (it.contains(JSON_PROCESS_TYPE_LIST)) {
                val processType = gson.fromJson(it, VegaCameroonProcessTypeModel::class.java)
                grnProcessType = processType.PROCESS_TYPE_LIST[0].GRN
            }
        }

    }

    private fun displayFragment(openFragment: String, bundle: Bundle?, canReplace: Boolean) {
        val search = menu?.findItem(R.id.search)
        val searchView: SearchView? = search?.actionView as SearchView?
        when (openFragment) {
            WEIGHMENT_TYPE -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaCameroonQualityWeighmentTypeFragment.newInstance()
            }
            WEIGHBRIDGE_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaCameroonQualityWBListFragment.newInstance()
            }
            WEIGHBRIDGE_LIST_MTNR -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaCameroonMtnrQualityWBListFragment.newInstance()
            }
            PARAMS_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaCameroonQualityParameterFragment.newInstance()
            }
            PARAMS_LIST_MTNR -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaCameroonMtnrQualityParameterFragment.newInstance()
            }
            LOT_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaCameroonQualityMtnrWBLotListFragment.newInstance()
            }
            QUALITY_OFFLINE_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaCameroonQualityOfflineSummary.newInstance()
            }
            QUALITY_SUMMARY -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaCameroonQualitySummaryFragment.newInstance()
            }
            else -> fragment = VegaCameroonQualityWBListFragment.newInstance()
        }
        bundle?.let { fragment.arguments = bundle }
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flEcuadorQuality,
            allowBackStack = canReplace
        )
    }

    override fun replaceFragment(weightmentType: String) {
        if (intent?.hasExtra(QUALITY_OFFLINE)!!) {
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST, "")
                putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
            }
            displayFragment(QUALITY_OFFLINE_LIST, bundle, true)
        }

        else {
            when(weightmentType){
                SUPPLIER -> {
                    val bundle = Bundle().apply {
                        putString(WEIGHBRIDGE_LIST_TYPE, weightmentType) }
                    displayFragment(WEIGHBRIDGE_LIST, bundle, true) }
                MTNR -> {
                    val bundle = Bundle().apply {
                        putString(WEIGHBRIDGE_LIST_TYPE, weightmentType) }
                    displayFragment(WEIGHBRIDGE_LIST_MTNR, bundle, true)
                }
            }
        }
    }

    override fun setQualityWBList(it: List<VegaQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }

    override fun onWeighBridgeClick(
        wbDetails: VegaQualityWBDetails?,
        copiedWbid: String,
        copiedMaterial: String
    ) {
        this.wbDetails = wbDetails
        isParamValue = false
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()


        if(wbDetails?.weighBridgeType == PROCURE)
        {
            val bundle = Bundle().apply {
                if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                    putBoolean(IS_PARAMS_VALUE, true)
                else
                    putBoolean(IS_PARAMS_VALUE, false)
                putString(COPIED_WBID, copiedWbid)
                putString(COPIED_MATERIAL, copiedMaterial)
                putParcelable(WEIGHSCALE, wbDetails)
            }
            displayFragment(PARAMS_LIST, bundle, true)
        }
        else if (wbDetails?.weighBridgeType == STO )
        {
            val bundle = Bundle().apply {
                if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                    putBoolean(IS_PARAMS_VALUE, true)
                else
                    putBoolean(IS_PARAMS_VALUE, false)
                putString(COPIED_WBID, copiedWbid)
                putString(COPIED_MATERIAL, copiedMaterial)
                putParcelable(WEIGHSCALE, wbDetails)
            }
            displayFragment(LOT_LIST, bundle, true)

        }
    }

    override fun onLotClick(lotDetails: VegaCoffeeLot?, lotList: MutableList<VegaCoffeeLot>) {
        this.lotDetails = lotDetails
        isParamValue = false
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        val lotItems = arrayListOf<VegaCoffeeLot>()
        lotItems.addAll(lotList)
        val bundle = Bundle().apply {
            putBoolean(IS_PARAMS_VALUE, false)
            putParcelable(LOT, lotDetails)
            putParcelableArrayList(LOT_LIST, lotItems)
            putParcelable(WEIGHSCALE, wbDetails)
        }
        displayFragment(PARAMS_LIST_MTNR, bundle, true)
    }

    override fun setQualityLotList(it: List<VegaCoffeeLot>?) {
        this.mQualityLotList.clear()
        this.mQualityLotList = it?.toMutableList() ?: mutableListOf()
    }

    override fun onParamsProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        challan: String
    ) {
        this.qualityParameter = qualityParameter
        finalApprovalStatus = finalApproval
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            wbDetails?.qualityDetails = qtyParams as List<VegaQuality>

            wbDetails?.batchNumber = batchNo
            wbDetails?.finalApproval = ""
            qualityPostList.clear()
            wbDetails?.let { qualityPostList.add(it) }
            vm.postQualityParams(
                VegaCameroonQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            saveData(qualityParameter, wbId)
            moveToSuccessPage( challan)
        }
    }

    override fun onMtnrParamsProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        lotItems: ArrayList<VegaCoffeeLot>,
        lotDetails: VegaCoffeeLot
    ) {

        mtnrlotItems = lotItems
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        val lotCoutPost = lotItems.filter { it.batchNumber.equals(lotDetails.batchNumber) }
        val qcDoneLot = lotItems.filter { it.qcStatus.equals("X") }
        val isApplicableGrn = lotItems.size - 1 == qcDoneLot.size
        var qualityPostList = arrayListOf<VegaCoffeeLot>()

        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            lotDetails.qualityDetails = qtyParams as List<VegaQuality>
            lotDetails.batchNumber = batchNo
            lotDetails.finalApproval = finalApproval
            qualityPostList.clear()
            if (isApplicableGrn) {
                lotItems.forEach {
                    if (it.batchNumber.equals(lotDetails.batchNumber)) it.qualityFlag = false
                }
                lotDetails.let { qualityPostList.addAll(lotItems) }
            } else {
                lotCoutPost.forEach {
                    it.qualityFlag = false
                }
                lotDetails.let { qualityPostList.addAll(lotCoutPost) }
            }

            vm.postQualityParamsMtnr(
                VegaCameroonQualityParamPost(
                    grnApplicable = isApplicableGrn,
                    grnFlag = false,
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList,
                    bcMessage = "",
                    charg = "",
                    currentWbid = "",
                    errorMessage = "",
                    grnNumber = ""
                )
            )
        } else {
            moveToMtnrSuccessPage("")
        }
    }

    private fun moveToMtnrSuccessPage( grnNo: String?) {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
        if (!grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created_mtnr).plus(lotDetails?.batchNumber).plus("\n GRN No : ")
                    .plus(grnNo)
            )
        else if (grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created_mtnr).plus(lotDetails?.batchNumber)
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(lotDetails?.weighBridgeId))

        val lotlist = ArrayList<VegaCoffeeSalesLots>()

        intent.putExtra("fromcoffee", true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        startActivity(intent)
        finish()
    }

    override fun onProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        challan: String
    ) {
        val bundle = Bundle().apply {
            putParcelableArrayList(QUALITY_LIST, qualityParameter)
            putString(WB_ID, wbId)
            putString(BATCH_NO, batchNo)
            putString(FINAL_APPROVAL, finalApproval)
            putString(CHALLAN, challan)
            putString(FLAG, "SUPPLIER")
        }
        displayFragment(QUALITY_SUMMARY, bundle, true)
    }


    private fun updateUI(response: Resource<GenericReqAndResp<VegaCameroonQualityPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                wbDetails?.challan
                            )

                            vm.updateDeletedItem(it.data?.data?.currentWbid.toString())
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

                    saveWB(wbDetails?.weighBridgeId.toString(), batchNo, it.error.toString(), 3)
                    saveData(qualityParameter, wbDetails?.weighBridgeId)
                }
            }
        }
    }

    fun saveWB(weighBrideId: String, batchNo: String, message: String, status: Int) {
        wbDetails?.batchNumber = batchNo
        wbDetails?.wbTempId = weighBrideId
        wbDetails?.status = status
        wbDetails?.finalApproval = finalApprovalStatus
        wbDetails?.message = message
        wbDetails?.let { vm.saveWBDB(it) }
    }

    private fun moveToSuccessPage(
        challan: String?
    ) {
        val intent = Intent(this, SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            if (finalApprovalStatus == FNQUALITY)
                intent.putExtra(AppUtils.TITLE, getString(R.string.supplier_quality_success))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject))
        } else {
            if (finalApprovalStatus == FNQUALITY)
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success_offline))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject_offline))
        }
        if (finalApprovalStatus == FNQUALITY) {
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.sample_id).plus(challan)
            )
        }

        startActivity(intent)
        finish()
    }

    override fun onQualityOfflineClick() {
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
        }
        displayFragment(QUALITY_OFFLINE_LIST, bundle, true)
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

    fun transactionWBDetails(wbDetails: VegaQualityWBDetails?) {
        isParamValue = true
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        val bundle = Bundle().apply {

            putBoolean(IS_PARAMS_VALUE, true)
            putParcelable(WEIGHSCALE, wbDetails)

        }
        displayFragment(PARAMS_LIST, bundle, false)
    }

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        bundle.let { this.wbDetails = it.getParcelable(WEIGHSCALE)!! }
        when (moveFrag) {
            PARAMS_LIST -> displayFragment(PARAMS_LIST, bundle, true)
        }
    }

    override fun onMtnrProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        lotItems: ArrayList<VegaCoffeeLot>,
        lotDetails: VegaCoffeeLot,
        flag: String
    ) {
        println("Roshna => MTNR in actiity class")
        val bundle = Bundle().apply {
            putParcelableArrayList(QUALITY_LIST, qualityParameter)
            putParcelableArrayList("LOT_LIST", lotItems)
            putParcelable("LOT_DETAILS", lotDetails)
            putString(WB_ID, wbId)
            putString(BATCH_NO, batchNo)
            putString(FINAL_APPROVAL, finalApproval)
            putString(CHALLAN, "")
            putString(FLAG, flag)
        }
        displayFragment(QUALITY_SUMMARY, bundle, true)
    }


}
