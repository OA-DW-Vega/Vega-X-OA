package com.olam.warehouse.vegax.qualitysesame.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.utils.prepareVegaQualityList
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualitysesame.R
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameProcessType
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameProcessTypeModel
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameQualityPost
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameQualityPostResponse
import com.olam.warehouse.vegax.qualitysesame.di.injectNigeriaSesameQualityFeature
import com.olam.warehouse.vegax.qualitysesame.ui.lot.VegaNigeriaSesameQualityMtnrWBLotListFragment
import com.olam.warehouse.vegax.qualitysesame.ui.offline.VegaNigeriaSesameQualityOfflineSummary
import com.olam.warehouse.vegax.qualitysesame.ui.params.VegaNigeriaSesameQualityParameterFragment
import com.olam.warehouse.vegax.qualitysesame.ui.params.VegaSesameMtnrQualityParameterFragment
import com.olam.warehouse.vegax.qualitysesame.ui.weighbridge.VegaNigeriaSesameQualityWBListFragment
import com.olam.warehouse.vegax.qualitysesame.ui.weighbridge.VegaSesameMtnrQualityWBListFragment
import com.olam.warehouse.vegax.qualitysesame.ui.weighment.VegaNigeriaSesameQualityWeighmentTypeFragment
import com.olam.warehouse.vegax.qualitysesame.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaNigeriaSesameQualityActivity : HomeBaseActivity(), VegaNigeriaSesameQualityWBListFragment.OnWeighBridgeListener,
    VegaNigeriaSesameQualityParameterFragment.OnParamsListener,VegaSesameMtnrQualityParameterFragment.OnParamsListener, VegaNigeriaSesameQualityMtnrWBLotListFragment.OnLotListener,
    VegaNigeriaSesameQualityWeighmentTypeFragment.CallBack, VegaNigeriaSesameQualityOfflineSummary.CallBack {
    private val mTAG = VegaNigeriaSesameQualityActivity::class.java.canonicalName
    private lateinit var fragment: Fragment
    private var isParamValue = false
    private var batchNo: String = ""
    private var storageLocationName: String = ""
    private var finalApprovalStatus: String = ""
    private val vm: VegaNigeriaSesameQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.activity_vega_nigeria_sesame_quality

    private var menu: Menu? = null
    private var wbDetails: VegaQualityWBDetails? = null
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var qualityParameter = ArrayList<VegaQualityParameter?>()

    private var lotDetails: VegaCoffeeLot? = null
    private var mQualityLotList: MutableList<VegaCoffeeLot> = mutableListOf()

    private var jsonData = mutableListOf<String>()
    private var processList = mutableListOf<String>()
    private var grnProcessType :String? =""
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null
    private var storageLocation: List<VegaCustomStLocation>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaSesameQualityFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        vm.quality.observe(this, Observer { updateUI(it) })
        vm.miscellaneousItems.observe(this, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())


        vm.thirdPartyMaterial.observe(this, Observer {
            thirdPartyMaterials = it
        })
        vm.getThirdPartyMaterials()

        vm.custonLocation.observe(this, Observer {
            storageLocation = it
        })
        vm.getCustomLocations()

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

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        var processTypeList = mutableListOf<VegaNigeriaSesameProcessType>()
        jsonData.forEach {
                if (it.contains(JSON_PROCESS_TYPE_LIST)) {
                      val processType = gson.fromJson(it, VegaNigeriaSesameProcessTypeModel::class.java)
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
                fragment = VegaNigeriaSesameQualityWeighmentTypeFragment.newInstance()
            }
            WEIGHBRIDGE_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaNigeriaSesameQualityWBListFragment.newInstance()
            }
            WEIGHBRIDGE_LIST_MTNR -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaSesameMtnrQualityWBListFragment.newInstance()
            }
            PARAMS_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaNigeriaSesameQualityParameterFragment.newInstance()
            }
            PARAMS_LIST_MTNR -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaSesameMtnrQualityParameterFragment.newInstance()
            }
            LOT_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaNigeriaSesameQualityMtnrWBLotListFragment.newInstance()
            }
            QUALITY_OFFLINE_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaNigeriaSesameQualityOfflineSummary.newInstance()
            }
            else -> fragment = VegaNigeriaSesameQualityWBListFragment.newInstance()
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
                    displayFragment(WEIGHBRIDGE_LIST, bundle, true)
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
        /*val bundle = Bundle().apply {
            if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                putBoolean(IS_PARAMS_VALUE, true)
            else
                putBoolean(IS_PARAMS_VALUE, false)
            putString(COPIED_WBID, copiedWbid)
            putString(COPIED_MATERIAL, copiedMaterial)
            putParcelable(WEIGHSCALE, wbDetails)
        }
        displayFragment(PARAMS_LIST, bundle, true)*/

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
        kor: String
    ) {
        this.qualityParameter = qualityParameter
        finalApprovalStatus = finalApproval
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (AppUtils.isOnline()) {
            wbDetails?.qualityDetails = prepareVegaQualityList(qtyParams, arrayListOf())
            wbDetails?.batchNumber = generateBatchNumber()
            wbDetails?.finalApproval = finalApproval
            wbDetails?.kor = kor
            storageLocation?.forEach {item ->
                if(item.procureLocationCode.contains(wbDetails?.storageLocationCode.toString(),true)){
                    storageLocationName  = item.procureLocationName.toString()
                    wbDetails?.storageLocation = storageLocationName
                }
            }
            qualityPostList.clear()
            wbDetails?.let { qualityPostList.add(it) }
            vm.postQualityParams(
                VegaNigeriaSesameQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    wbDetails= wbDetails,
                    lotDetails = qualityPostList
                )
            )
        } else {
            saveData(qualityParameter, wbId)
            moveToSuccessPage(wbId, "", "","")
        }
    }

    private fun generateBatchNumber():String {
        var productTypeCode =""

        thirdPartyMaterials?.forEach {item ->
        if(item.materialName.toString().contains(wbDetails?.materialName.toString(),true)){
            productTypeCode = item.typeCode.toString()
        }
        }
          var year = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(1)
        return wbDetails?.plant?.takeLast(2).plus(wbDetails?.storageLocationCode?.takeLast(1)).plus(year).plus(grnProcessType).plus(productTypeCode.takeLast(1))
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaNigeriaSesameQualityPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                it.data?.data?.currentWbid,
                                it.data?.data?.charg,
                                it.data?.data?.grnNumber,
                                it.data?.data?.encodedImageContent
                            )

                            vm.updateDeletedItem(it.data?.data?.currentWbid.toString())
                        }
                        else -> {
                            showErrorDialogWithFAQLink(this, "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(this, "${it.error}")

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

    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?,encodedImageContent:String?) {
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
        if(finalApprovalStatus == FNQUALITY){
            if (charg?.isNotEmpty()!! && !grnNo.isNullOrEmpty())
                intent.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(R.string.new_lot_id_created).plus("\n LOT No : ").plus(charg).plus("\n GRN No : ").plus(grnNo)
                )
            else if (charg.isNotEmpty() && grnNo.isNullOrEmpty())
                intent.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(R.string.new_lot_id_created).plus("\n LOT ID : ").plus(charg).plus("\n WB ID : ").plus(currentWbid)
                )
            else
                intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(currentWbid))
        }

        if(finalApprovalStatus == FNQUALITY) {
            val whReceipt = ArrayList<String>()
            whReceipt.add(encodedImageContent ?: "")
            val lotlist = ArrayList<VegaCoffeeSalesLots>()
            lotlist.add(
                VegaCoffeeSalesLots(
                    "",
                    charg.toString(),
                    wbDetails?.materialCode.toString(),
                    wbDetails?.materialName.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    wbDetails?.unitsOfMeasure,
                    "",
                    wbDetails?.netWeight


                )
            )
            intent.putExtra("fromsesame", true)
            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
            intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, whReceipt)
            intent.putExtra(AppUtils.PRINT_ENABLE, true)
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

}
