package com.olam.warehouse.vegax.offloadingcocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.PROCURE
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentIvcCocoSelectOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.mtnr.VegaCoCoaMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingcocoa.ui.transaction.VegaCocoaMtnrTransactionFragment
import com.olam.warehouse.vegax.offloadingcocoa.utils.MTNR
import com.olam.warehouse.vegax.offloadingcocoa.utils.STO
import com.olam.warehouse.vegax.offloadingcocoa.utils.SUPPLIER
import com.olam.warehouse.vegax.offloadingcocoa.utils.TRANSACTION
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIvcCocoOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ivc_coco_select_offload_type_layout
    private lateinit var binding: FragmentIvcCocoSelectOffloadTypeLayoutBinding
    private var callBack: VegaCoCoaOffloadReplaceFragmentCallback? = null
    private var isTransaction = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoCoaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance(isTransaction: Boolean) = VegaIvcCocoOffloadTypeSelectFragment().putArgs {
            putBoolean("TRANS", isTransaction)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIvcCocoSelectOffloadTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcoffee/ui/VegaCoffeeOffloadTypeSelectFragment").title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
       // isTransaction = arguments?.getBoolean("TRANS")?: false
        binding.llRmin.setOnClickListener { moveToSupplier() }
        binding.llFgrn.setOnClickListener { moveToMtnr() }
        /*if (isTransaction)
            callBack?.replaceFragment(TRANSACTION, VegaCoCoaReceiving())*/
        var isVirtual = false
        val roleData = Gson().fromJson<List<UserRole>>(
            PreferenceHelper.get(
                Constants.USER_ROLES,
                ""
            )
        )
            .filter { key ->
                key.roleKey.equals(
                    PreferenceHelper.get(
                        Constants.CURRENT_KEY,
                        ""
                    )
                )
            }
        roleData.forEach { rol ->
            when (UserRoles.valueOfEnum(rol.roleName.trim())) {
                UserRoles.VIRTUAL_MTNR -> isVirtual = true
                else -> {}
            }
        }
        if(isVirtual) binding.llFgrn.visible() else binding.llFgrn.gone()

    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(
            SUPPLIER, PROCURE
        )
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(
            MTNR, STO
        )
    }
}

