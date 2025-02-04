package com.olam.warehouse.vegax.offloadingghana.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.databinding.FragmentGhanaSelectOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingghana.utils.MTNR
import com.olam.warehouse.vegax.offloadingghana.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_select_offload_type_layout
    private lateinit var binding: FragmentGhanaSelectOffloadTypeLayoutBinding
    private var callBack: VegaGhanaOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaGhanaOffloadTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaSelectOffloadTypeLayoutBinding.inflate(layoutInflater)
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
