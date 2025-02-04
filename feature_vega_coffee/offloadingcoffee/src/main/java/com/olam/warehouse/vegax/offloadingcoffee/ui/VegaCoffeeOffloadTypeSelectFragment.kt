package com.olam.warehouse.vegax.offloadingcoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcoffee.R
import com.olam.warehouse.vegax.offloadingcoffee.databinding.FragmentSelectOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingcoffee.utils.MTNR
import com.olam.warehouse.vegax.offloadingcoffee.utils.PROCURE
import com.olam.warehouse.vegax.offloadingcoffee.utils.STO
import com.olam.warehouse.vegax.offloadingcoffee.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_select_offload_type_layout
    private lateinit var binding: FragmentSelectOffloadTypeLayoutBinding
    private var callBack: VegaCoffeeOffloadReplaceFragmentCallback? = null
    private var currentKey = getCurrentKey()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaCoffeeOffloadTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectOffloadTypeLayoutBinding.inflate(layoutInflater)
        if (currentKey.split("_")[1].contains("NI")) {
            binding.llRmin.visibility = View.GONE
        }
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
        binding.llRmin.setOnClickListener { moveToSupplier() }
        binding.llFgrn.setOnClickListener { moveToMtnr() }
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
