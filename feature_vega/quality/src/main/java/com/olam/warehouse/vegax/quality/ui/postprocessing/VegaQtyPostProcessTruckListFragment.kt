package com.olam.warehouse.vegax.quality.ui.postprocessing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaPostProcessQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.quality.R
import com.olam.warehouse.vegax.quality.databinding.FragmentVegaPostQualityTruckListBinding
import com.olam.warehouse.vegax.quality.ui.VegaQualityViewModel
import kotlinx.android.synthetic.main.item_vega_post_quality_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaQtyPostProcessTruckListFragment : BaseFragment() {

    private val vm: VegaQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_post_quality_truck_list
    private lateinit var mListener: VegaQtyPostProcessTruckListFragment.OnWeighBridgeListener
    private lateinit var binding: FragmentVegaPostQualityTruckListBinding

    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(wbDetails: VegaQualityWBDetails?)
        fun setQualityWBList(it: List<VegaQualityWBDetails>?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    companion object {
        fun newInstance() = VegaQtyPostProcessTruckListFragment().putArgs {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaPostQualityTruckListBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("odreceiving/ui/postprocessing/VegaQtyPostProcessTruckListFragment")
            .title("Quality")
            .with(tracker)
    }


    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        vm.postProcessQualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }


    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let { updateDetails(it) }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateDetails(poList: List<VegaPostProcessQualityWBDetails>) {
        val datalist = poList.filter { !it.weighBridgeId.isEmpty() }
        var data = datalist as MutableList<VegaPostProcessQualityWBDetails>
        when {
            datalist.isEmpty() -> {
                binding.tvError.text = getString(R.string.post_process_not_available)
                binding.tvError.visibility = View.VISIBLE
                binding.rvWeighbridge.visibility = View.GONE
            }
            else -> {
                binding.tvError.visibility = View.GONE
                binding.rvWeighbridge.visibility = View.VISIBLE
                data = data.sortedByDescending {
                    it.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                        DateUtils.getUTCDateTime(
                            it1,
                            App.getAppContext()
                        )
                    }
                } as MutableList<VegaPostProcessQualityWBDetails>
            }
        }

        binding.rvWeighbridge.setUp(data, R.layout.item_vega_post_quality_details, { item, pos ->
            tvLotNo.text = item.batchNumber
            tvProcessOrderNo.text = item.batchNumber
            tvStroageLocation.text = item.bagWeight
            tvWeight.text = item.bagWeight
            tvDate.text = item.erdat
        }, {
            //callBack?.replaceFragment(GRADESELECTION, this, "")
        })
    }
}
