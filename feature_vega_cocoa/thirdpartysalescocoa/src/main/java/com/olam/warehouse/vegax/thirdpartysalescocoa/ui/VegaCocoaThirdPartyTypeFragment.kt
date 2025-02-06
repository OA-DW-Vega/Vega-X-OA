package com.olam.warehouse.vegax.thirdpartysalescocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartycocoa.R
import com.olam.warehouse.vegax.thirdpartycocoa.databinding.FragmentCocoaThirdPartySalesSelectionBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyReplaceFragmentCallback
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaThirdPartyTypeFragment : BaseFragment() {

    private lateinit var binding: FragmentCocoaThirdPartySalesSelectionBinding
    private var callBack: VegaCocoaThirdPartyReplaceFragmentCallback? = null

    private var isThirdPartyOwnership: Boolean = false
    private var isThirdPartyTP: Boolean = false
    private var isThirdPartyOlam: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCocoaThirdPartyReplaceFragmentCallback
    }

    override val layoutResourceId = R.layout.fragment_cocoa_third_party_sales_selection

    companion object {
        fun newInstance() = VegaCocoaThirdPartyTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCocoaThirdPartySalesSelectionBinding.inflate(layoutInflater)
        val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(PreferenceHelper.get(Constants.CURRENT_KEY, "")) }
        roleData.forEach { rol ->
            when (UserRoles.valueOfEnum(rol.roleName.trim())) {
                UserRoles.THIRDPARTY_OWNERSHIP -> isThirdPartyOwnership = true
                UserRoles.THIRDPARTY_TP_TO_TP -> isThirdPartyTP = true
                UserRoles.THIRDPARTY_TP_OLAM -> isThirdPartyOlam = true
                else -> {}
            }
        }
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("thirdpartysalescoffee/ui/VegaCocoaThirdPartyTypeFragment")
            .title("Third party sales Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.llOwnershipTransfer.setOnClickListener { moveToOwnershipTransfer() }
        binding.llThirdPartyOlam.setOnClickListener { moveToThirdPartyOlam() }
        binding.llThirdPartySales.setOnClickListener { movetoSales() }
        binding.llOwnershipTransfer.visibility = if (isThirdPartyOwnership) View.VISIBLE else View.GONE
        binding.llThirdPartyOlam.visibility = if (isThirdPartyOlam) View.VISIBLE else View.GONE
        binding.llThirdPartySales.visibility = if (isThirdPartyTP) View.VISIBLE else View.GONE
    }

    private fun moveToOwnershipTransfer() {
        callBack?.replaceFragment(Ownership_Transfer, TP_TO_TP)
    }

    private fun moveToThirdPartyOlam() {
        callBack?.replaceFragment(Third_Party_Olam, TP_TO_OLAM)
    }

    private fun movetoSales() {
        callBack?.replaceFragment(Third_Party_Sales, SAME_TP)
    }

}
