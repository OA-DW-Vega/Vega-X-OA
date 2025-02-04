package com.olam.warehouse.vegax.qualityecuador.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityecuador.R

import com.olam.warehouse.vegax.qualityecuador.di.injectEcuadorQualityFeature
import com.olam.warehouse.vegax.qualityecuador.ui.offline.VegaEcuadorQualityOfflineSummary
import com.olam.warehouse.vegax.qualityecuador.ui.params.VegaEcuadorQualityParameterFragment
import com.olam.warehouse.vegax.qualityecuador.ui.weighbridge.VegaEcuadorQualityWBListFragment
import com.olam.warehouse.vegax.qualityecuador.ui.weighment.VegaEcuadorQualityWeighmentTypeFragment
import com.olam.warehouse.vegax.qualityecuador.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaEcuadorQualityActivity : HomeBaseActivity(), VegaEcuadorQualityWBListFragment.OnWeighBridgeListener,
    VegaEcuadorQualityParameterFragment.OnParamsListener,
    VegaEcuadorQualityWeighmentTypeFragment.CallBack, VegaEcuadorQualityOfflineSummary.CallBack {
    private val mTAG = VegaEcuadorQualityActivity::class.java.canonicalName
    private lateinit var fragment: Fragment
    private var isParamValue = false
    private var batchNo: String = ""
    private var finalApprovalStatus: String = ""
    private val vm: VegaEcuadorQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.activity_vega_ecuador_quality

    private var menu: Menu? = null
    private var wbDetails: VegaQualityWBDetails? = null
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var qualityParameter = ArrayList<VegaQualityParameter?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectEcuadorQualityFeature()
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
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST_TYPE, SUPPLIER)
            }
            displayFragment(WEIGHBRIDGE_LIST, bundle, false)
            //displayFragment(WEIGHMENT_TYPE, null, false)
        }
    }

    private fun displayFragment(openFragment: String, bundle: Bundle?, canReplace: Boolean) {
        val search = menu?.findItem(R.id.search)
        val searchView: SearchView? = search?.actionView as SearchView?
        when (openFragment) {
            WEIGHMENT_TYPE -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaEcuadorQualityWeighmentTypeFragment.newInstance()
            }
            WEIGHBRIDGE_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaEcuadorQualityWBListFragment.newInstance()
            }
            PARAMS_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaEcuadorQualityParameterFragment.newInstance()
            }
            QUALITY_OFFLINE_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaEcuadorQualityOfflineSummary.newInstance()
            }
            else -> fragment = VegaEcuadorQualityWBListFragment.newInstance()
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
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
            }
            displayFragment(WEIGHBRIDGE_LIST, bundle, true)
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

    override fun onParamsProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {
        this.qualityParameter = qualityParameter
        finalApprovalStatus = finalApproval
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (AppUtils.isOnline()) {
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
