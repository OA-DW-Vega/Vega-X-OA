package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorBcApproveWeighmentTypeBinding
import com.olam.warehouse.vegax.bcapproveecuador.utils.WEIGHBRIDGE
import com.olam.warehouse.vegax.bcapproveecuador.utils.WEIGHSCALE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import androidx.lifecycle.Observer
class VegaEcuadorBCApproveWeighmentTypeFragment :BaseFragment()  {

    private val mTAG = VegaEcuadorBCApproveWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaEcuadorBcApproveWeighmentTypeBinding
    private var callBack: CallBack? = null
    private var weighmentType =""
    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    interface CallBack {
        fun replaceFragment(weighmentType: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_ecuador_bc_approve_weighment_type

//    companion object {
//        fun newInstance() = FragmentVegaEcuadorBcApproveWeighmentTypeBinding().putArgs {
//        }
//    }
companion object {
    fun newInstance() = VegaEcuadorBCApproveWeighmentTypeFragment().putArgs {
    }
}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorBcApproveWeighmentTypeBinding.inflate(layoutInflater)
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
        vm.getFeatureMaster("BC Approval")
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
