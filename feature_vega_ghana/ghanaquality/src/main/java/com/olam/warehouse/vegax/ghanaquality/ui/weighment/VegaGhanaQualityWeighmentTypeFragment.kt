package com.olam.warehouse.vegax.ghanaquality.ui.weighment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.ui.common.VegaCommonModuleNavigation
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.enums.UserRoles.Companion.valueOfEnum
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ghanaquality.R
import com.olam.warehouse.vegax.ghanaquality.databinding.FragmentVegaGhanaQualityWeighmentTypeBinding
import com.olam.warehouse.vegax.ghanaquality.ui.VegaGhanaQualityViewModel
import com.olam.warehouse.vegax.ghanaquality.utils.MTNR
import com.olam.warehouse.vegax.ghanaquality.utils.PROCURE
import com.olam.warehouse.vegax.ghanaquality.utils.STO
import com.olam.warehouse.vegax.ghanaquality.utils.SUPPLIER
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaGhanaQualityWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaGhanaQualityWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaGhanaQualityWeighmentTypeBinding
    private var callBack: CallBack? = null
    private val vm: VegaGhanaQualityViewModel by viewModel()
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

    override val layoutResourceId = R.layout.fragment_vega_ghana_quality_weighment_type

    companion object {
        fun newInstance() = VegaGhanaQualityWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaQualityWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitysesame/ui/weighment/VegaNigeriaSesameQualityWeighmentTypeFragment")
            .title("Ecuador Quality")
            .with(tracker)
        initUI()
    }

    private fun getRoles(current_key: String): List<UserRole> {
        return Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(current_key) }
    }

    private fun updateRoles(userRoles: List<UserRole>?){
        val list = mutableListOf<String>()
        userRoles?.forEach { rol -> list.add(rol.roleName) }

        list.forEach {
            when (valueOfEnum(it.replace(" ", "_"))) {
                UserRoles.ROLE_ANY_LOT_QUALITY -> {
                 binding.llAnyLot.visible()
                }
                else -> {
                  binding.llAnyLot.gone()
                }
            }
            }

    }


    private fun initUI() {
        roleData = getRoles(getCurrentKey())

        if (AppUtils.isOnline()) {
            vm.weighBridgeOnline.observe(viewLifecycleOwner, Observer {
                updateUIWithOnlineData(it)
            })
            vm.getWeighBridgeDataOnline()
        } else {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { item ->
                updateUI(item)
            })
            vm.getWeighBridgeDetail()
        }
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }

        binding.llAnyLot.setOnClickListener {
            val intent = Intent(requireContext(), VegaCommonModuleNavigation::class.java)
            intent.putExtra(Constants.NAV_MODULE, "ANY_LOT_QUALITY")
            startActivity(intent)

        }


        updateRoles(roleData)
    }


        private fun moveToMtnr() {
        callBack?.replaceFragment(MTNR)
    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(SUPPLIER)
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()

                var weighTypedata: List<VegaQualityWBDetails>? = null
                var weighTypedata1: List<VegaQualityWBDetails>? = null

                val data = response.data?.data
                    ?.filter { value -> !value.qcStatus!!.contains("X") }
//                            ?.filter { it.grnNumber.isNotEmpty() }
                weighTypedata = data?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                    ?.filter { value -> value.weighBridgeType == PROCURE }
                    ?.filter { value -> !value.netWeight.equals("0.0") }
                    ?.filter { value -> value.challan.isNullOrEmpty() }

                binding.suppliercount.text = weighTypedata!!.size.toString()
                val data1 =
                    response.data?.data/*?.filter { value -> value.qcStatus.toString() == "R" }*/
                weighTypedata1 = data1?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                    ?.filter { value -> value.weighBridgeType == STO }
                    ?.filter { value -> !value.netWeight.equals("0.000") }

                binding.mtnrcount.text = weighTypedata1!!.size.toString()
                /* MTNR -> {
                     val data = response.data?.data*//*?.filter { value -> value.qcStatus.toString() == "R" }*//*
                        weighTypedata = data?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                            ?.filter { value -> value.weighBridgeType == STO }
                            ?.filter { value -> !value.netWeight.equals("0.000") }
                    }*/

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {

            }
        }
    }

    private fun updateUI(response: List<VegaQualityWBDetails>?) {
        response?.let { qualityDetail ->
            val offlineData = qualityDetail.filter { value -> value.isNotWBID }
                .filter { it.wbTempId.contains("TMP") }
            val mtnrOfflineData = qualityDetail.filter { it.wbTempId.contains("TMP") }
            val data =
                qualityDetail.filter { value -> !value.qcStatus!!.contains("X") }
//                    .filter { it.grnNumber.isNotEmpty() }
            var weighTypedata: List<VegaQualityWBDetails>? = null
            var weighTypedata1: List<VegaQualityWBDetails>? = null

            weighTypedata = data.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                .filter { value -> value.weighBridgeType == PROCURE }
            binding.suppliercount.text = weighTypedata.size.toString()
            val data2 = response
            weighTypedata1 = data2.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                .filter { value -> value.weighBridgeType == STO }
                .filter { value -> !value.netWeight.equals("0.000") }
            binding.mtnrcount.text = weighTypedata1.size.toString()


        }
    }
}
