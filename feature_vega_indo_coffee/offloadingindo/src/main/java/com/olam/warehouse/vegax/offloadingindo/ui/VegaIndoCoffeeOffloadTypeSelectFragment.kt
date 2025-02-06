package com.olam.warehouse.vegax.offloadingindo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingindo.R
import com.olam.warehouse.vegax.offloadingindo.databinding.FragmentIndoCoffeeSelectOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingindo.ui.callback.VegaIndoCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingindo.utils.MTNR
import com.olam.warehouse.vegax.offloadingindo.utils.PROCURE
import com.olam.warehouse.vegax.offloadingindo.utils.STO
import com.olam.warehouse.vegax.offloadingindo.utils.SUPPLIER
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffee_select_offload_type_layout
    private lateinit var binding: FragmentIndoCoffeeSelectOffloadTypeLayoutBinding
    private var callBack: VegaIndoCoffeeOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaIndoCoffeeOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaIndoCoffeeOffloadTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIndoCoffeeSelectOffloadTypeLayoutBinding.inflate(layoutInflater)
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

