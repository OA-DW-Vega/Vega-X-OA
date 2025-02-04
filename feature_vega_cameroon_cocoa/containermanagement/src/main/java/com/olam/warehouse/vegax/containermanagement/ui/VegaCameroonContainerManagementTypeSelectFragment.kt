package com.olam.warehouse.vegax.containermanagement.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.containermanagement.R
import com.olam.warehouse.vegax.containermanagement.databinding.FragmentVegaCameroonContainerManagementTypeSelectBinding
import com.olam.warehouse.vegax.containermanagement.utils.ADD_CONTAINER
import com.olam.warehouse.vegax.containermanagement.utils.CONTAINER_INVENTORY
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonContainerManagementTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_container_management_type_select
    private lateinit var binding: FragmentVegaCameroonContainerManagementTypeSelectBinding
    private var callBack: iCallBack? = null

    interface iCallBack {
        fun replaceFragment(containerType: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? iCallBack
    }

    companion object {
        fun newInstance() = VegaCameroonContainerManagementTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonContainerManagementTypeSelectBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("containermanagement/ui/VegaCameroonContainerManagementTypeSelectFragment")
            .title("Vega_Cameroon/Containermanagement").with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        binding.llAddContainer.setOnClickListener { moveToAddContainer() }
        binding.llContainerInventory.setOnClickListener { moveToContainerInventory() }
    }

    private fun moveToAddContainer() {
        callBack?.replaceFragment(
            ADD_CONTAINER, ""
        )
    }

    private fun moveToContainerInventory() {
        callBack?.replaceFragment(
            CONTAINER_INVENTORY, ""
        )
    }

}

