package com.olam.warehouse.vegax.qualityindiacoffee.ui.postprocessing

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
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.databinding.FragmentVegaIndiaCoffeePostQualityTruckListBinding
import com.olam.warehouse.vegax.qualityindiacoffee.ui.VegaIndiaCoffeeQualityViewModel
import kotlinx.android.synthetic.main.item_vega_india_coffee_post_quality_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaIndiaCoffeeQtyPostProcessTruckListFragment : BaseFragment() {

    private val vm: VegaIndiaCoffeeQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_post_quality_truck_list
    private lateinit var mListener: VegaIndiaCoffeeQtyPostProcessTruckListFragment.OnWeighBridgeListener
    private lateinit var binding: FragmentVegaIndiaCoffeePostQualityTruckListBinding

    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(wbDetails: VegaQualityWBDetails?)
        fun setQualityWBList(it: List<VegaQualityWBDetails>?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    companion object {
        fun newInstance() = VegaIndiaCoffeeQtyPostProcessTruckListFragment().putArgs {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaIndiaCoffeePostQualityTruckListBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }


    private fun initUI() {
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

        binding.rvWeighbridge.setUp(data, R.layout.item_vega_india_coffee_post_quality_details, { item, pos ->
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
