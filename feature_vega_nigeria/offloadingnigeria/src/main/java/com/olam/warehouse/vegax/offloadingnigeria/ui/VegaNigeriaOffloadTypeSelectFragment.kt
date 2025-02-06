package com.olam.warehouse.vegax.offloadingnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.databinding.FragmentNigeriaSelectOffloadTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingnigeria.utils.MTNR
import com.olam.warehouse.vegax.offloadingnigeria.utils.SUPPLIER
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaOffloadTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_nigeria_select_offload_type_layout
    private lateinit var binding: FragmentNigeriaSelectOffloadTypeLayoutBinding
    private var callBack: VegaNigeriaOffloadReplaceFragmentCallback? = null
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaNigeriaOffloadTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNigeriaSelectOffloadTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingNigeria/ui/VegaSesameOffloadTypeSelectFragment")
            .title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener { moveToSupplier() }
        binding.llFgrn.setOnClickListener { moveToMtnr() }

        vm.featureMaster.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateFeatureUI(it)
        })
        vm.getFeatureMaster("Offloading")
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

    private fun updateFeatureUI(it: List<VegaFeatureMaster>?) {
        it?.let {
            it.forEach {
                when (it.featureName) {
                    "Supplier" -> if (it.mandatory == true) binding.llRmin.visible() else binding.llRmin.gone()
                    "MTNR" -> if (it.mandatory == true) binding.llFgrn.visible() else binding.llFgrn.gone()
                }
            }
        }

    }
}
