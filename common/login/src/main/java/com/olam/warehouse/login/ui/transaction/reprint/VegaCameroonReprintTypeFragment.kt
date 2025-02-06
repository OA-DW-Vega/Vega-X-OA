package com.olam.warehouse.login.ui.transaction.reprint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentVegaReprintTypeBinding
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.navigation.features.VegaCameroonCocoaGrnNavigation
import com.olam.warehouse.navigation.features.VegaCameroonCocoaOffloadingNavigation
import com.olam.warehouse.navigation.features.VegaQualityApproveCameroonNavigation
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_OFFLOADING_RECEIPT
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_OFFLOADING_TICKET
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_QA_RECEIPT
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_QA_TICKET
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson

class VegaCameroonReprintTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_reprint_type

    private lateinit var binding:FragmentVegaReprintTypeBinding

    companion object {
        fun newInstance() = VegaCameroonReprintTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaReprintTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUi()
        clickEvent()
    }

    private fun clickEvent() {
        binding.clOffloadingReceipt.setOnClickListener {
            VegaCameroonCocoaOffloadingNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_OFFLOADING_RECEIPT, true)
                startActivity(intent)
            }
        }
        binding.clOffloadingTicket.setOnClickListener {
            VegaCameroonCocoaOffloadingNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_OFFLOADING_TICKET, true)
                startActivity(intent)
            }

        }
        binding.clQualityApprovalRecpt.setOnClickListener {
            VegaQualityApproveCameroonNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_QA_RECEIPT, true)
                startActivity(intent)
            }
        }
        binding.clQualityApprovalTicket.setOnClickListener {
            VegaQualityApproveCameroonNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_QA_TICKET, true)
                startActivity(intent)
            }
        }
    }

    private fun initUi() {

        val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(PreferenceHelper.get(Constants.CURRENT_KEY, "")) }
        roleData.forEach { rol ->
            when (UserRoles.valueOfEnum(rol.roleName.trim())) {
                UserRoles.OFFLOADING -> {
                    binding.clOffloadingReceipt.visible()
                    binding.clOffloadingTicket.visible()
                }
                UserRoles.APPROVE ->{
                    binding.clQualityApprovalRecpt.visible()
                    binding.clQualityApprovalTicket.visible()
                }
                else -> {}
            }
        }


        binding.tvOffloadingReceipt.text = getString(R.string.offloading_receipt)
        binding.tvOffloadingTicket.text = getString(R.string.offloading_ticket)
        binding.tvQualityApprovalRecpt.text= getString(R.string.quality_approval_receipt)
        binding.tvQualityApprovalTicket.text= getString(R.string.quality_approval_ticket)
    }
}
