package com.olam.warehouse.vegax.qualitynigeria.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualitynigeria.R
import com.olam.warehouse.vegax.qualitynigeria.di.injectNigeriaQualityFeature
import com.olam.warehouse.vegax.qualitynigeria.ui.offline.VegaNigeriaQualityOfflineSummary
import com.olam.warehouse.vegax.qualitynigeria.ui.params.VegaNigeriaQualityParameterFragment
import com.olam.warehouse.vegax.qualitynigeria.ui.weighbridge.VegaNigeriaQualityWBListFragment
import com.olam.warehouse.vegax.qualitynigeria.ui.weighment.VegaNigeriaQualityWeighmentTypeFragment
import com.olam.warehouse.vegax.qualitynigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Riya Jose on 6/21/2020.
 */
class VegaNigeriaQualityActivity : HomeBaseActivity(),
    VegaNigeriaQualityWBListFragment.OnWeighBridgeListener,
    VegaNigeriaQualityParameterFragment.OnParamsListener,
    VegaNigeriaQualityWeighmentTypeFragment.CallBack, VegaNigeriaQualityOfflineSummary.CallBack,
    VegaNigeriaQualitySummaryFragment.OnSummaryParamsListener {
    private val mTAG = VegaNigeriaQualityActivity::class.java.canonicalName
    private lateinit var fragment: Fragment
    private var isParamValue = false
    private var mSelectedPlantId = ""
    private var batchNo: String = ""
    private var finalApprovalStatus: String = ""
    private var beanWtDiscGmValue: String = ""
    private var beanCountValue: String = ""
    private var moistureValue: String = ""
    private var slatyValue: String = ""
    private var mouldValue: String = ""
    private var addMixtureValue: String = ""
    private val vm: VegaNigeriaQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.activity_vega_nigeria_quality

    private var menu: Menu? = null
    private var wbDetails: VegaQualityWBDetails? = null
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()

    //  private var qualityNigeriaPostList = arrayListOf<VegaQualityParameter>()
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var qualityParameter = ArrayList<VegaQualityParameter?>()
    private var plantList = mutableListOf<Plant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaQualityFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaQualityWeighmentTypeFragment.newInstance(), false)
        vm.quality.observe(this, Observer { updateUI(it) })

        vm.qualityNigeria.observe(this, Observer { updatequalityNigeriaUI(it) })
        /*vm.quality.observe(this, Observer { updateUI(it) })
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
            displayFragment(WEIGHBRIDGE_LIST, bundle, false)
            //displayFragment(WEIGHMENT_TYPE, null, false)
        }*/
    }
    private fun displayFragment(fragment: Fragment, flag: Boolean){
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flEcuadorQuality,
            allowBackStack = flag
        )
    }

    private fun displayFragment(openFragment: String, bundle: Bundle?, canReplace: Boolean) {
        val search = menu?.findItem(R.id.search)
        val searchView: SearchView? = search?.actionView as SearchView?
        when (openFragment) {
            WEIGHMENT_TYPE -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaNigeriaQualityWeighmentTypeFragment.newInstance()
            }
            WEIGHBRIDGE_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaNigeriaQualityWBListFragment.newInstance()
            }
            PARAMS_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaNigeriaQualityParameterFragment.newInstance()
            }
            SUMMARY_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaNigeriaQualitySummaryFragment.newInstance()
            }
            QUALITY_OFFLINE_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaNigeriaQualityOfflineSummary.newInstance()
            }
            else -> fragment = VegaNigeriaQualityWBListFragment.newInstance()
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
        } else {

            when (weightmentType) {
                SUPPLIER -> {
                    val bundle = Bundle().apply {
                        putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
                    }
                    displayFragment(WEIGHBRIDGE_LIST, bundle, true)
                }
                MTNR -> {
                    val bundle = Bundle().apply {
                        putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
                    }
                    displayFragment(WEIGHBRIDGE_LIST, bundle, true)
                }
            }

            /* val bundle = Bundle().apply {
                 putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
             }
             displayFragment(WEIGHBRIDGE_LIST, bundle, true)*/
        }
    }

    override fun setQualityWBList(it: List<VegaQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }

    override fun onWeighBridgeClick(
        wbDetails: VegaQualityWBDetails?,
        copiedWbid: String,
        copiedMaterial: String, selectedPlantId: String
    ) {
        this.wbDetails = wbDetails
        isParamValue = false
        mSelectedPlantId = selectedPlantId
        batchNo =
            if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
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

    override fun onParamsProceedQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String, storageLocationCode: String
    ) {
        /*this.qualityParameter = qualityParameter
        finalApprovalStatus = finalApproval
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }

        val bundle = Bundle().apply {
            putParcelableArrayList(APPROVE_QUALITY_DATA, qualityParameter )
            putString("FINAL_APP", finalApproval)
            putString(WB_ID, wbId)
            putString(BATCH_NO, batchNo)
            putString(FINAL_APPROVAL, finalApproval)
            putString(STORAGELOCATION_CODE, storageLocationCode)
            putString(FLAG, "SUPPLIER")
        }

        displayFragment(SUMMARY_LIST, bundle, true)
        *//*if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            wbDetails?.qualityDetails = qtyParams as List<VegaQuality>
            wbDetails?.batchNumber = batchNo
            wbDetails?.finalApproval = finalApproval
            qualityPostList.clear()
            wbDetails?.let { qualityPostList.add(it) }
            vm.postQualityParams(
                VegaQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            saveData(qualityParameter, wbId)
            moveToSuccessPage(wbId, "", "")
        }*/



        this.qualityParameter = qualityParameter
        finalApprovalStatus = finalApproval
        val qtyNigeriaParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            wbDetails?.qualityDetails = qtyNigeriaParams as List<VegaQuality>
            wbDetails?.batchNumber = batchNo
            wbDetails?.finalApproval = finalApproval
            wbDetails?.plant = mSelectedPlantId
            qualityPostList.clear()
            wbDetails?.let { qualityPostList.add(it) }
            plantList = getMultiPlantList() as MutableList<Plant>
            var plantDetails = getPlantDetails(mSelectedPlantId).single()
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
                    plant = plantDetails,
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

    private fun updateUI(response: Resource<GenericReqAndResp<VegaQualityPostResponse>>) {
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
                                putString(FLAG, wbDetails?.weighBridgeType)
                                putString(MOULD_VALUE, mouldValue)
                                putString(ADD_MIXTURE, addMixtureValue)
                                putString(BEAN_WT_GRAM, beanWtDiscGmValue)
                                putString(BEAN_COUNT, beanCountValue)
                                putString(SLATY, slatyValue)
                                putString(PLANTID, mSelectedPlantId)
                                putString(CHALLAN, wbDetails?.challan)
                                putString(MOISTURE, moistureValue)
                            }

                            displayFragment(SUMMARY_LIST, bundle, true)
                            /*if (AppUtils.isOnline()) {
                                @Suppress("UNCHECKED_CAST")
                                wbDetails?.qualityDetails = qtyParams as List<VegaQuality>
                                wbDetails?.batchNumber = batchNo
                                wbDetails?.finalApproval = finalApproval
                                qualityPostList.clear()
                                wbDetails?.let { qualityPostList.add(it) }
                                vm.postQualityParams(
                                    VegaQualityPost(
                                        key = getCurrentKey(),
                                        plant = getPlantDetails(),
                                        lotDetails = qualityPostList
                                    )
                                )
                            } else {
                                saveData(qualityParameter, wbId)
                                moveToSuccessPage(wbId, "", "")
                            }*/
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

    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?) {
        val intent = Intent(this, SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            if (finalApprovalStatus == FNQUALITY || finalApprovalStatus == FNQUALITY_THRESHOLD || finalApprovalStatus == "D")
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject))
        } else {
            if (finalApprovalStatus == FNQUALITY || finalApprovalStatus == FNQUALITY_THRESHOLD)
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success_offline))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject_offline))
        }
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.weigh_bridge_id_text).plus(currentWbid)
        )
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

    private fun getPlantDetails(plantId: String?): List<Plant> {
        return plantList.filter { it.plantId == plantId }
    }

    fun transactionWBDetails(wbDetails: VegaQualityWBDetails?) {
        isParamValue = true
        batchNo =
            if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        val bundle = Bundle().apply {
            /*putString(TRUCK_NO, wbDetails?.vehicleNumber)
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)*/
            putBoolean(IS_PARAMS_VALUE, true)
            putParcelable(WEIGHSCALE, wbDetails)
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

    override fun replaceFragment(moveFrag: String, bundle: Bundle) {
        bundle.let { this.wbDetails = it.getParcelable(WEIGHSCALE)!! }
        when (moveFrag) {
            PARAMS_LIST -> displayFragment(PARAMS_LIST, bundle, true)
        }
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
            wbDetails?.finalApproval = finalApproval
            wbDetails?.appName = "QC"
            wbDetails?.plant = mSelectedPlantId
            qualityPostList.clear()
            wbDetails?.let { qualityPostList.add(it) }
            plantList = getMultiPlantList() as MutableList<Plant>
            var plantDetails = getPlantDetails(mSelectedPlantId).single()
            vm.postQualityParams(
                VegaQualityPost(
                    key = getCurrentKey(),
                    plant = plantDetails,
                    lotDetails = qualityPostList
                )
            )
        } else {
            saveData(qualityParameter, wbId)
            moveToSuccessPage(wbId, "", "")
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
        TODO("Not yet implemented")
    }

}
