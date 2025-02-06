package com.olam.warehouse.vegax.gateentrycoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrycoffee.R
import com.olam.warehouse.vegax.gateentrycoffee.databinding.FragmentVegaCoffeeGateEntryTypeBinding
import com.olam.warehouse.vegax.gateentrycoffee.utils.PROCURE
import com.olam.warehouse.vegax.gateentrycoffee.utils.STO
import com.olam.warehouse.vegax.gateentrycoffee.utils.WEIGHMENT_TYPE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCoffeeGateEntryTypeFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaCoffeeGateEntryTypeBinding
    private var callBack: VegaCoffeeReplaceCallback? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceCallback
    }

    override val layoutResourceId = R.layout.fragment_vega_coffee_gate_entry_type

    companion object {
        fun newInstance() = VegaCoffeeGateEntryTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeGateEntryTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentry/ui/VegaGateEntryTypeFragment").title("IVC/Coffee/Gate Entry/Gate Entry Type").with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
    }

    private fun moveToSupplier() {
        callBack?.replaceFragment(WEIGHMENT_TYPE, PROCURE)
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(WEIGHMENT_TYPE, STO)
    }
}
