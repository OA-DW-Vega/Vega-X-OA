package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorBcApproveListDetailsBinding
import com.olam.warehouse.vegax.bcapproveecuador.utils.GRN_QUALITY_DETAILS
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaEcuadorBcApproveListDetailsFragment : BaseFragment(){
    override val layoutResourceId: Int =
        R.layout.fragment_vega_ecuador_bc_approve_list_details
    private lateinit var binding: FragmentVegaEcuadorBcApproveListDetailsBinding
    private var callBack: VegaEcuadorBcApproveListFragment.CallBack? = null
    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    private var receivingData = VegaEcuadorBcApproveWBDetails()
    private var approveQualityList = ArrayList<VegaEcuadorApproveWbDetail>()
    private var qualitycallBack: QualityDetailsCallBack? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorBcApproveListDetailsBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }

    interface QualityDetailsCallBack {
        fun replaceQualityDetailsFragment(
            moveFrag: String,
            approveQualityList: ArrayList<VegaEcuadorApproveWbDetail>
        )
    }


    companion object {
        fun newInstance(bundle: VegaEcuadorBcApproveWBDetails) = VegaEcuadorBcApproveListDetailsFragment()
            .putArgs {
                putParcelable("data", bundle)
            }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        qualitycallBack = context as QualityDetailsCallBack
    }
    private fun initUI() {
        receivingData = arguments?.getParcelable("data")!!
        vm.getQualityDetails(receivingData.batchNumber.toString(), receivingData.materialNumber.toString())
        binding.tvponumber.text=resources.getString(R.string.po_no).plus(" : ").plus(receivingData.poNumber)
        binding.btProceed.setOnClickListener {   activity?.onBackPressed() }
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
        // Added for Quality Details Button
//        binding.btQualityDetails.setOnClickListener { moveToQualityDetails() }
    }

    private fun moveToQualityDetails() {
        qualitycallBack?.replaceQualityDetailsFragment(GRN_QUALITY_DETAILS,approveQualityList)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaEcuadorApproveWbDetail>
                        if (!approveQualityList.isEmpty())
                        {
                        binding.tvProcurementTypeValue.text=receivingData.plantDesc
                        binding.tvPONumberValue.text=approveQualityList.get(0).qualityParams.B_SECONDARY_REFR
                        binding.tvGrossValue.text=approveQualityList.get(0).qualityParams.B_MOIST
                        binding.tvNoBagsValue.text=approveQualityList.get(0).qualityParams.B_GRNPRICE1

                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }
}
