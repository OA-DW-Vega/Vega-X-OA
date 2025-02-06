package com.olam.warehouse.vegax.qualitynigeria.ui.weighment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.login.ui.common.VegaCommonModuleNavigation
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitynigeria.R
import com.olam.warehouse.vegax.qualitynigeria.databinding.FragmentVegaNigeriaQualityWeighmentTypeBinding
import com.olam.warehouse.vegax.qualitynigeria.ui.VegaNigeriaQualityViewModel
import com.olam.warehouse.vegax.qualitynigeria.utils.MTNR
import com.olam.warehouse.vegax.qualitynigeria.utils.SUPPLIER
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaQualityWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaNigeriaQualityWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaNigeriaQualityWeighmentTypeBinding
    private var callBack: CallBack? = null
    private val vm: VegaNigeriaQualityViewModel by viewModel()
    private var batchNo: String = ""
    private var wbDetails: VegaQualityWBDetails? = null
    private var isParamValue = false
    private var qualityParameter = ArrayList<VegaQualityParameter?>()
    private var roleData = listOf<UserRole>()


    interface CallBack {
        fun replaceFragment(
            weightmentType: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nigeria_quality_weighment_type

    companion object {
        fun newInstance() = VegaNigeriaQualityWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaQualityWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitynigeria/ui/weighment/VegaNigeriaQualityWeighmentTypeFragment")
            .title("Ecuador Quality")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        roleData = getRoles(getCurrentKey())
        updateRoles(roleData)

        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
        binding.llAnyLot.setOnClickListener {
            val intent = Intent(requireContext(), VegaCommonModuleNavigation::class.java)
            intent.putExtra(Constants.NAV_MODULE, "ANY_LOT_QUALITY")
            startActivity(intent)

        }

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("CASH")){
            binding.llMtnr.gone()
        }
    }

    private fun updateRoles(userRoles: List<UserRole>?){
        val list = mutableListOf<String>()
        userRoles?.forEach { rol -> list.add(rol.roleName) }

        list.forEach {
            when (it) {
                UserRoles.ROLE_ANY_LOT_QUALITY.role -> {
                    binding.llAnyLot.visible()
                }
            }
        }

    }


    private fun getRoles(current_key: String): List<UserRole> {
        return Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(current_key) }
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(MTNR)
    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(SUPPLIER)

        /*vm.quality.observe(viewLifecycleOwner, Observer { updateUI(it) })
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

  /*  private fun updateUI(response: Resource<GenericReqAndResp<VegaQualityPostResponse>>) {
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
    fun saveData(qualityParameter: ArrayList<VegaQualityParameter?>, wbId: String?) {
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
            *//*putString(TRUCK_NO, wbDetails?.vehicleNumber)
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)*//*
            putBoolean(IS_PARAMS_VALUE, true)
            putParcelable(WEIGHSCALE, wbDetails)
            *//* putString(BATCH_NO, batchNo)
             putString(NET_WEIGHT, wbDetails?.netWeight)
             putString(TAR_WEIGHT, wbDetails?.bagWeight)
             putString(MATERIAL_NO, wbDetails?.materialCode)
             putString(CHALLAN, wbDetails?.challan)
             putString(ITEM, wbDetails?.item)
             putString(WEIGHBRIDGE_LIST_TYPE, wbDetails?.weighBridgeType)*//*
        }
        displayFragment(PARAMS_LIST, bundle, false)
    }*/

}
