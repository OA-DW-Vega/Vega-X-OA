package com.olam.warehouse.vegax.grnecuador.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.R
import com.olam.warehouse.vegax.grnecuador.databinding.FragmentVegaEcuadorWeighmentTypeBinding
import com.olam.warehouse.vegax.grnecuador.utils.WEIGHBRIDGE
import com.olam.warehouse.vegax.grnecuador.utils.WEIGHSCALE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import androidx.lifecycle.Observer
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible

class VegaEcuadorWeighmentTypeFragment :BaseFragment()  {

    private val mTAG = VegaEcuadorWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaEcuadorWeighmentTypeBinding
    private var callBack: CallBack? = null
    private var weighmentType =""
    private val vm: VegaEcuadorGrnViewModel by viewModel()
    interface CallBack {
        fun replaceFragment(weighmentType: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_ecuador_weighment_type

//    companion object {
//        fun newInstance() = FragmentVegaEcuadorWeighmentTypeBinding().putArgs {
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorWeighmentTypeBinding.inflate(layoutInflater)
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
        vm.getFeatureMaster("GRN")
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

    private fun moveToWeighbridge() {
        weighmentType = WEIGHBRIDGE
        callBack?.replaceFragment( weighmentType)
    }

    private fun moveToWeighscale() {
        weighmentType = WEIGHSCALE
        callBack?.replaceFragment( weighmentType)
    }


}
