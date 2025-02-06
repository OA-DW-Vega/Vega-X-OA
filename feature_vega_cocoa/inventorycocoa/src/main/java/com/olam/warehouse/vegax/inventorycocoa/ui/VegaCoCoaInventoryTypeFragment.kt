package com.olam.warehouse.vegax.inventorycocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorycocoa.R
import com.olam.warehouse.vegax.inventorycocoa.databinding.FragmentVegaCocoaInventoryTypeBinding
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoCoaInventoryTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cocoa_inventory_type
    private lateinit var binding: FragmentVegaCocoaInventoryTypeBinding
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceInventoryFragment(isThirdParty: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaCoCoaInventoryTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCocoaInventoryTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycocoa/ui/VegaCoCoaInventoryTypeFragment").title("Inventory Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        binding.llOlam.setOnClickListener { moveToOLam() }
        binding.llThirdParty.setOnClickListener { moveToThirdParty() }
    }

    private fun moveToOLam() {
        callBack?.replaceInventoryFragment(
            false
        )
    }

    private fun moveToThirdParty() {
        callBack?.replaceInventoryFragment(
            true
        )
    }
}
