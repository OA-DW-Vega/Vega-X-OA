package com.olam.warehouse.vegax.stockrecon.ui.reconreport

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportAuditDetails
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportBundleData
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportAuditListBinding
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportReconListBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemAuditListBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemReconReportAuditListBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_AUDIT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.SINGLE_SPACE
import com.olam.warehouse.vegax.stockrecon.utils.getMaterialNameFromMaterialList
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaReconReportAuditListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_recon_report_audit_list
    private lateinit var binding: FragmentReconReportAuditListBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    var bundleData = VegaReconReportBundleData()
    var vegaPackingMaterialList = mutableListOf<VegaMaterial>()


    companion object {
        fun newInstance(bundleData: VegaReconReportBundleData) = VegaReconReportAuditListFragment().putArgs {
            putParcelable(BUNDLE_DATA, bundleData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReconReportAuditListBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        initExtra()
        setUIvalues()
        observer()
    }

    private fun initExtra() {
        bundleData = arguments?.getParcelable<VegaReconReportBundleData>(BUNDLE_DATA) as VegaReconReportBundleData
    }

    private fun setUIvalues() {
        binding.tvDateRangeValue.setText(bundleData.reconListDetails?.dateRange)
        binding.tvWarehouseValue.setText(bundleData.plantAndDate?.plant)
        binding.tvRecReportIdValue.setText(bundleData.selectedReconDetails?.id)
        binding.tvNoOfLotsValue.setText(bundleData.reconListDetails?.totalNoOfLots)
        binding.tvTotalRecWeightValue.setText(
            bundleData.reconListDetails?.totalSystemWeight.plus(SINGLE_SPACE)
                .plus(bundleData.reconListDetails?.unitOfMeasure)
        )
        binding.tvTotalWeightLossValue.setText(
            bundleData.reconListDetails?.totalGainLoss.plus(SINGLE_SPACE)
                .plus(bundleData.reconListDetails?.unitOfMeasure)
        )
    }

    private fun observer() {
        /*The below things is to get all audit list from api*/
        bundleData.selectedReconDetails?.id?.let { vm.getReconReportAuditList(it) }
        vm.reconReportAuditList.observe(viewLifecycleOwner, Observer { updateUI(it) })

        /*The below observer is to fetch all materials details,
        because to set material name in audit list*/
        vm.getVegaMaterials()
        vm.vegaMaterials.observe(
            viewLifecycleOwner,
            Observer { vegaPackingMaterialList = it as MutableList<VegaMaterial> })

    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        setAuditListAdapter(data as MutableList<VegaStockReconGetAllAuditData>)
                    }
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun setAuditListAdapter(list: MutableList<VegaStockReconGetAllAuditData>) {
        binding.rvAudits.setUpAdapter(
            list,
            R.layout.item_recon_report_audit_list,
            ItemReconReportAuditListBinding::inflate,
            { it, pos, bindingItem ->
                val item = list.get(pos)
                bindingItem.tvLotIdValue.setText(item.lotNumber)
                /*The below line is to set material name from packing material lise,
                because material name is not available in api*/
                bindingItem.tvMaterialValue.setText(item.material?.let { it1 ->
                    getMaterialNameFromMaterialList(
                        vegaPackingMaterialList,
                        it1
                    )
                })
                bindingItem.tvSystemWeightValue.setText(
                    item.systemNetWeight.plus(SINGLE_SPACE).plus(item.unitOfMeasure)
                )
                bindingItem.tvWeightLossValue.setText(item.weightGainLoss.plus(SINGLE_SPACE).plus(item.unitOfMeasure))
                bindingItem.tvNoOfBagsValue.setText(item.sysNoOfBags)
                bindingItem.tvTypeOfBagValue.setText(getBagType(item))
            }, itemClick = {
                bundleData.auditDetails = this
                moveToAuditDetailsPage()
            })
    }

    private fun moveToAuditDetailsPage() {
        callBack?.replaceFragment(RECON_REPORT_AUDIT_DETAILS, bundleData)
    }

    private fun getBagType(auditDetails: VegaStockReconGetAllAuditData): String {
        /*in some case, we have multiple bag,
        so checking the bag one by one and setting the bag value*/
        var bagType = ""
        if (auditDetails.bagType1?.isNotEmpty() == true) {
            bagType = auditDetails?.bagType1 ?: ""
        }
        if (auditDetails.bagType2?.isNotEmpty() == true) {
            bagType = auditDetails.bagType1.plus(",").plus(auditDetails.bagType2)
        }
        if (auditDetails.bagType3?.isNotEmpty() == true) {
            bagType = auditDetails.bagType1.plus(",").plus(auditDetails.bagType2).plus(",").plus(auditDetails.bagType3)
        }
        return bagType
    }

}
