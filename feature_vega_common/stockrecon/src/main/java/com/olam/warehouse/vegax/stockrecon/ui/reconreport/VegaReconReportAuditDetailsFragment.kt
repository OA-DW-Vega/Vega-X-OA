package com.olam.warehouse.vegax.stockrecon.ui.reconreport

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportAuditDetails
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportBundleData
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportAuditDetailsBinding
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportAuditListBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_AUDIT_LIST
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_VIEW_PHOTO
import com.olam.warehouse.vegax.stockrecon.utils.SINGLE_SPACE
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaReconReportAuditDetailsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_recon_report_audit_details
    private lateinit var binding: FragmentReconReportAuditDetailsBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    var bundleData = VegaReconReportBundleData()


    companion object {
        fun newInstance(bundleData: VegaReconReportBundleData) = VegaReconReportAuditDetailsFragment().putArgs {
            putParcelable(BUNDLE_DATA, bundleData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReconReportAuditDetailsBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        initExtra()
        observer()
        clickListener()
    }

    private fun initExtra() {
        bundleData = arguments?.getParcelable<VegaReconReportBundleData>(BUNDLE_DATA) as VegaReconReportBundleData
    }

    private fun clickListener() {
        binding.btnViewPhoto.setOnClickListener { moveToViewPhotoPage(false) }
        binding.ivPrint.setOnClickListener { moveToViewPhotoPage(true) }
    }

    private fun observer() {
        /*the below lines to call audit details API*/
        bundleData.selectedReconDetails?.id?.let { reconId ->
            bundleData.auditDetails?.id?.let { auditId ->
                vm.getReconReportAuditDetails(
                    reconId,
                    auditId
                )
            }
        }
        vm.reconReportAuditDetail.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaReconReportAuditDetails>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        setUIvalues(data)
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

    /*the below method to set all values, which is fetched from audit details api*/
    private fun setUIvalues(auditDetails: VegaReconReportAuditDetails) {
        binding.tvReconIdValue.setText("Rec Id : ${auditDetails.reconId}")
        binding.tvLotNoValue.setText(auditDetails.lotNumber)
        binding.tvBagTypeValue.setText(getBagType(auditDetails))
        binding.tvNoOfBagsValue.setText(auditDetails.sysNoOfBags)
        binding.tvHalfBagCountValue.setText(getHalfBagCount(auditDetails))
        binding.tvFullBagCountValue.setText(getFullBagCount(auditDetails))
        binding.tvSpillageValue.setText(if (auditDetails.spillage) "Yes" else "No")
        binding.tvDamagedCountValue.setText(auditDetails.noOfDamagedBags)
        binding.tvStorageLocationValue.setText(auditDetails.storageLocation)
        binding.tvRemarksValue.setText(auditDetails.remarks)
        binding.tvSystemWeightValue.setText(
            auditDetails.systemNetWeight.plus(SINGLE_SPACE).plus(auditDetails.unitOfMeasure)
        )
        binding.tvAuditWeightValue.setText(
            auditDetails.stockAuditWeight.plus(SINGLE_SPACE).plus(auditDetails.unitOfMeasure)
        )
        binding.tvWeightLossValue.setText(
            auditDetails.weightGainLoss.plus(SINGLE_SPACE).plus(auditDetails.unitOfMeasure)
        )
    }

    private fun moveToViewPhotoPage(isReport: Boolean) {
        /*The below line is set isReport flag,
        because in next page we have identify whether
        we have to show report image or normal image */
        bundleData.isReport = isReport
        callBack?.replaceFragment(RECON_REPORT_VIEW_PHOTO, bundleData)
    }

    private fun getBagType(auditDetails: VegaReconReportAuditDetails): String {
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


    private fun getHalfBagCount(auditDetails: VegaReconReportAuditDetails): String {
        return (auditDetails.noOfHalfBags1?.toInt()!! + auditDetails.noOfHalfBags2?.toInt()!! + auditDetails.noOfHalfBags3?.toInt()!!).toString()
    }

    private fun getFullBagCount(auditDetails: VegaReconReportAuditDetails): String{
        return (auditDetails.noOfFullBags1?.toInt()!! + auditDetails.noOfFullBags2?.toInt()!! + auditDetails.noOfFullBags3?.toInt()!!).toString()
    }
}
