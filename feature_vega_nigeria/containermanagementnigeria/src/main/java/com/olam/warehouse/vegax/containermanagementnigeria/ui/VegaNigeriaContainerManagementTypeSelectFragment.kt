package com.olam.warehouse.vegax.containermanagementnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.containermanagementnigeria.R
import com.olam.warehouse.vegax.containermanagementnigeria.databinding.FragmentVegaNigeriaContainerManagementTypeSelectBinding
import com.olam.warehouse.vegax.containermanagementnigeria.utils.ADD_CONTAINER
import com.olam.warehouse.vegax.containermanagementnigeria.utils.CONTAINER_INVENTORY
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaContainerManagementTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_container_management_type_select
    private lateinit var binding: FragmentVegaNigeriaContainerManagementTypeSelectBinding
    private var callBack: iCallBack? = null

    interface iCallBack {
        fun replaceFragment(containerType: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? iCallBack
    }

    companion object {
        fun newInstance() = VegaNigeriaContainerManagementTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaContainerManagementTypeSelectBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/VegaNigeriaContainerManagementTypeSelectFragment")
            .title("Vega_Nigeria/Containermanagement").with(tracker)
    }

    private fun initUI() {
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

