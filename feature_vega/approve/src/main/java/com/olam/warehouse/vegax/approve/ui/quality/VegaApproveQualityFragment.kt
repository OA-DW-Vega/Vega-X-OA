package com.olam.warehouse.vegax.approve.ui.quality

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approve.R
import com.olam.warehouse.vegax.approve.data.domain.model.VegaApproveQuality
import com.olam.warehouse.vegax.approve.data.domain.model.VegaApproveQualityParams
import com.olam.warehouse.vegax.approve.data.domain.model.VegaApproveWeighBridgeId
import com.olam.warehouse.vegax.approve.databinding.FragmentVegaApproveQualityBinding
import com.olam.warehouse.vegax.approve.databinding.ItemVegaApproveQualityParamsBinding
import com.olam.warehouse.vegax.approve.ui.OnFragmentInteractionListener
import com.olam.warehouse.vegax.approve.ui.VegaApproveViewModel
import com.olam.warehouse.vegax.approve.utils.APPROVE_DATA
import com.olam.warehouse.vegax.approve.utils.APPROVE_QUALITY_DATA
import com.olam.warehouse.vegax.approve.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

class VegaApproveQualityFragment : BaseFragment() {

    private val vm: VegaApproveViewModel by viewModel()
    private var approveQuality = mutableListOf<VegaApproveQuality>()
    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var binding: FragmentVegaApproveQualityBinding

    override val layoutResourceId = R.layout.fragment_vega_approve_quality

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaApproveQualityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("approve/ui/quality/VegaApproveQualityFragment").title("Approve")
            .with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.btnOkApprove, it, true)
        }
        val wbid = arguments?.get(APPROVE_DATA) as VegaApproveWeighBridgeId
        approveQuality =
            arguments?.getParcelableArrayList<VegaApproveQuality>(APPROVE_QUALITY_DATA) as MutableList<VegaApproveQuality>
        binding.tvApproveParamsWeighBID.text = wbid.weighBridgeId

        binding.btnOkApprove.setOnClickListener { activity?.onBackPressed() }

        setUpAdapter(approveQuality)
    }


    private fun setUpAdapter(data: List<VegaApproveQuality>) {
        data.let {
            approveQuality = it as MutableList<VegaApproveQuality>
            var i = 0
            binding.rvApproveQuality.setUpAdapter(
                approveQuality.first().qualityParameters as MutableList<VegaApproveQualityParams>,
                R.layout.item_vega_approve_quality_params,
                ItemVegaApproveQualityParamsBinding::inflate,
                { it, pos, binding ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }
                    binding.tvQualityNameApprove.text = it.sapQCName
                    binding.tvUnitApprove.text = it.satNam
                },
                {

                })
        }
    }



}
