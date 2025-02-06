package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.LayoutGhanaCocoaModuleSelectBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.DW_MODULE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.VEGA_MODULE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGahanaCocoaSelectModuleWiseFragment :BaseFragment() {
    override val layoutResourceId = R.layout.layout_ghana_cocoa_module_select
    private lateinit var binding: LayoutGhanaCocoaModuleSelectBinding
    private var callBack: VegaGhanaCocoaOffloadReplaceFragmentCallback? = null


    companion object {
        fun newInstance() = VegaGahanaCocoaSelectModuleWiseFragment().putArgs {
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack  = context as VegaGhanaCocoaOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = LayoutGhanaCocoaModuleSelectBinding.inflate(inflater)
        intUi()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingsesame/ui/VegaGahanaCocoaSelectModuleWise").title("Grn Coffee")
            .with(tracker)
    }

    private fun intUi() {
        binding.apply {
            llRmin.setOnClickListener {
                movetoVegaRecivingPage()
            }
            llFgrn.setOnClickListener {
                movetoDWRecivingPage()
            }
        }
    }

    private fun movetoDWRecivingPage() {
        callBack?.replaceFragment(DW_MODULE)
    }

    private fun movetoVegaRecivingPage() {
        callBack?.replaceFragment(VEGA_MODULE)
    }

    override fun onDetach() {
        super.onDetach()
    }
}
