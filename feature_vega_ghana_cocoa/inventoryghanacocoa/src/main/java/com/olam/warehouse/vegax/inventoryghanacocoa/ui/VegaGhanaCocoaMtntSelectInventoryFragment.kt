package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryghanacocoa.R
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.FragmentGhanaCocoaInvSelectTypeLayoutBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.STORAGE
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.TRANSIT
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaMtntSelectInventoryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_cocoa_inv_select_type_layout
    private lateinit var binding: FragmentGhanaCocoaInvSelectTypeLayoutBinding
    private var callBack: VegaGhanaCocoaReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGhanaCocoaMtntSelectInventoryFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaCocoaInvSelectTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ui/VegaGhanaCocoaMtntSelectInventoryFragment").title("Ghana Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener {
            moveToStorage() }
        binding.llFgrn.setOnClickListener {
            moveToTransit() }
    }

    private fun moveToStorage() {
        callBack?.replaceFragment(
            STORAGE, ""
        )
    }

    private fun moveToTransit() {
        callBack?.replaceFragment(
            TRANSIT, ""
        )
    }
}

