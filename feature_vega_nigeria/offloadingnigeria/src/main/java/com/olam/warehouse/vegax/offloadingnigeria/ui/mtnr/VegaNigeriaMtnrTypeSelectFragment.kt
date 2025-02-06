package com.olam.warehouse.vegax.offloadingnigeria.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.databinding.FragmentNigeriaSelectMtnrTypeLayoutBinding
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingnigeria.utils.WEIGHBRIDGE_WEIHSCALE
import com.olam.warehouse.vegax.offloadingnigeria.utils.WEIGHSCALE
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaMtnrTypeSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_nigeria_select_mtnr_type_layout
    private lateinit var binding: FragmentNigeriaSelectMtnrTypeLayoutBinding
    private var callBack: VegaNigeriaOffloadReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaNigeriaMtnrTypeSelectFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNigeriaSelectMtnrTypeLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingNigeria/ui/mtnr/VegaSesameMtnrTypeSelectFragment")
            .title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener { moveToWS() }
        binding.llFgrn.setOnClickListener { moveToWBWS() }
    }

    private fun moveToWBWS() {
        callBack?.replaceFragment(
            WEIGHBRIDGE_WEIHSCALE, ""
        )
    }

    private fun moveToWS() {
        callBack?.replaceFragment(
            WEIGHSCALE, ""
        )
    }
}
