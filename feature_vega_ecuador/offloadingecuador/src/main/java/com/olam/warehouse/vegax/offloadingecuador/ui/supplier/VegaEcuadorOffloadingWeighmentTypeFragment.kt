package com.olam.warehouse.vegax.offloadingecuador.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingecuador.R
import com.olam.warehouse.vegax.offloadingecuador.databinding.FragmentVegaEcuadorOffloadingWeighmentTypeBinding
import com.olam.warehouse.vegax.offloadingecuador.ui.VegaEcuadorOffloadingViewModel
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHBRIDGE
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHSCALE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible

class VegaEcuadorOffloadingWeighmentTypeFragment :BaseFragment()  {

    private val mTAG = VegaEcuadorOffloadingWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaEcuadorOffloadingWeighmentTypeBinding
    private var callBack: VegaEccuadorOffloadingListener? = null
    private var weighmentType =""
    private val vm: VegaEcuadorOffloadingViewModel by viewModel()

    /*interface CallBack {
        fun replaceFragment(weighmentType: String)
    }*/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaEccuadorOffloadingListener
    }

    override val layoutResourceId = R.layout.fragment_vega_ecuador_offloading_weighment_type

    companion object {
        fun newInstance() = VegaEcuadorOffloadingWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorOffloadingWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grn/ui/receivingtype/VegaReceivingTypeFragments").title("Grn")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.clWeightScale.setOnClickListener { moveToWeighscale() }
        binding.clWeighBridge.setOnClickListener { moveToWeighbridge() }
        vm.featureMaster.observe(viewLifecycleOwner, Observer { updateFeatureUI(it) })
        vm.getFeatureMaster("Offloading")
    }

    private fun moveToWeighbridge() {
        weighmentType = WEIGHBRIDGE
        callBack?.replaceFragment( weighmentType)
    }

    private fun moveToWeighscale() {
        weighmentType = WEIGHSCALE
        callBack?.replaceFragment( weighmentType)
    }
    private fun updateFeatureUI(it: List<VegaFeatureMaster>?) {
        it?.let {
            it.forEach {
                when (it.featureName) {
                    "WeighScale" -> if (it.mandatory == true) binding.clWeightScale.visible() else binding.clWeightScale.gone()
                    "WeighBridge" -> if (it.mandatory == true) binding.clWeighBridge.visible() else binding.clWeighBridge.gone()
                }
            }
        }

    }

}
