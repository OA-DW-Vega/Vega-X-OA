package com.olam.warehouse.vegax.offloadingsesame.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingsesame.R
import com.olam.warehouse.vegax.offloadingsesame.databinding.FragmentSesameSelectOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingsesame.utils.MTNR
import com.olam.warehouse.vegax.offloadingsesame.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaSesameOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_sesame_select_offload_type_layout
    private lateinit var binding: FragmentSesameSelectOffloadTypeLayoutBinding
    private var callBack: VegaSesameOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaSesameOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaSesameOffloadTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSesameSelectOffloadTypeLayoutBinding.inflate(layoutInflater)
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
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
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
