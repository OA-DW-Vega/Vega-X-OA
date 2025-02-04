package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentGhanaCocoaSelectOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.MTNR
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_cocoa_select_offload_type_layout
    private lateinit var binding: FragmentGhanaCocoaSelectOffloadTypeLayoutBinding
    private var callBack: VegaGhanaCocoaOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGhanaCocoaOffloadTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaCocoaSelectOffloadTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingsesame/ui/VegaSesameOffloadTypeSelectFragment").title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
        var isMtnr = false
        val roleData =
            Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                .filter { key ->
                    key.roleKey.equals(
                        PreferenceHelper.get(
                            Constants.CURRENT_KEY,
                            ""
                        )
                    )
                }
        roleData.forEach { rol -> if (rol.roleName.equals("ROLE_PORT_LOCATION")) isMtnr = true }
        if (isMtnr) binding.llRmin.gone() else binding.llRmin.visibility
        binding.llRmin.setOnClickListener { moveToSupplier() }
        binding.llFgrn.setOnClickListener { moveToMtnr() }
    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(
            SUPPLIER, ""
        )
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(
            MTNR, ""
        )
    }
}
