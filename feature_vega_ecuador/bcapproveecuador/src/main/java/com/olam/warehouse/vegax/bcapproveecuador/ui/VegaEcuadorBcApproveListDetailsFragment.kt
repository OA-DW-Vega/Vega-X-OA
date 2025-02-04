package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorBcApproveListBinding
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorBcApproveListDetailsBinding
import kotlinx.android.synthetic.main.fragment_vega_ecuador_bc_approve_list_details.*
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaEcuadorBcApproveListDetailsFragment : BaseFragment(){
    override val layoutResourceId: Int =
        R.layout.fragment_vega_ecuador_bc_approve_list_details
    private lateinit var binding: FragmentVegaEcuadorBcApproveListDetailsBinding
    private var callBack: VegaEcuadorBcApproveListFragment.CallBack? = null
    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    private var receivingData = VegaEcuadorBcApproveWBDetails()
    private var approveQualityList = ArrayList<VegaEcuadorApproveWbDetail>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorBcApproveListDetailsBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }


    companion object {
        fun newInstance(bundle: VegaEcuadorBcApproveWBDetails) = VegaEcuadorBcApproveListDetailsFragment()
            .putArgs {
                putParcelable("data", bundle)
            }

    }
    interface CallBack {
        fun replaceFragment(fragment: String, item: VegaEcuadorBcApproveWBDetails, salesType: String)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaEcuadorBcApproveListFragment.CallBack
    }
    private fun initUI() {
        receivingData = arguments?.getParcelable("data")!!
        vm.getQualityDetails(receivingData.batchNumber.toString(), receivingData.materialNumber.toString())
        binding.tvponumber.text=resources.getString(R.string.po_no).plus(" : ").plus(receivingData.poNumber)
        binding.btProceed.setOnClickListener {   activity?.onBackPressed() }
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
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
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }
}
