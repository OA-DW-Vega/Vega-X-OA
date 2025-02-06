package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vegacameroon.model.QualityDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApprovePost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveQualityDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.*
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentVegaEcuadorApproveWeighbridgeListDetailsBinding
import com.olam.warehouse.vegax.bcapproveecuador.ui.VegaEcuadorBcApproveViewModel

import com.olam.warehouse.vegax.bcapproveecuador.utils.BC_QUALITY_DETAILS
import com.olam.warehouse.vegax.bcapproveecuador.utils.BC_SUMMARY_DETAILS

import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Roshna Parambil on 1/31/2022.
 */
class VegaEcuadorBcApproveWBListDetailsFragment : BaseFragment() {


    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    private var approveQualityList = ArrayList<VegaEcuadorApproveWbDetail>()
//    private var weighBridgeId = VegaEcuadorQualityApproveWeighBridgeId()
    private var wbDetails = VegaEcuadorBcApproveWBDetails()
    private var mListener: Callback? = null
    private var vegaBcApprovePostData = VegaQualityApproveEcuadorPostData()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()

    private lateinit var binding: FragmentVegaEcuadorApproveWeighbridgeListDetailsBinding

    override val layoutResourceId = R.layout.fragment_vega_ecuador_approve_weighbridge_list_details


    interface Callback {
        fun replaceQualityFragment(
            moveFrag: String,
            approveQualityList: ArrayList<VegaEcuadorApproveWbDetail>
        )
        fun replaceSummaryFragment(
            moveFrag: String,
            approveQualityList: ArrayList<VegaEcuadorApproveWbDetail>,
            wbDetails: VegaEcuadorBcApproveWBDetails
        )
    }
    companion object {
        fun newInstance(bundle: VegaEcuadorBcApproveWBDetails) = VegaEcuadorBcApproveWBListDetailsFragment()
            .putArgs {
                putParcelable("data", bundle)
            }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorApproveWeighbridgeListDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("bcapproveecuador/ui/details/VegaBcApproveCameroonDetailsFragment")
            .title("Vega_Cameroon/Approve").with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback) {
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
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btProceed, it, true)
        }
        wbDetails = arguments?.get("data") as VegaEcuadorBcApproveWBDetails

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialNumber.toString())
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
//        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })
//        binding.tvFcfaLabel.text =
//            with(UIUtils) { with(requireContext().resources.getString(R.string.final_price_per_unit)) { mandatoryStars() } }
//        binding.tvponumber.text=resources.getString(R.string.po_no).plus(" : ").plus(wbDetails.poNumber)

        binding.btProceed.setOnClickListener {
            wbDetails.unitPrice = binding.etPrice.text.toString()
            /*when {
                binding.etPrice.text.isNullOrEmpty() -> getString(R.string.price_vaidation)
                else -> mListener?.replaceSummaryFragment(BC_SUMMARY_DETAILS,approveQualityList,wbDetails)
            }*/
            mListener?.replaceSummaryFragment(BC_SUMMARY_DETAILS,approveQualityList,wbDetails)
        }
        binding.btQualityDetails.setOnClickListener {
            mListener?.replaceQualityFragment(BC_QUALITY_DETAILS,approveQualityList)
        }
        updateUIValues()
    }

    private fun updateUIValues (){
        binding.tvLotIdValue.text = wbDetails.batchNumber.toString()
        binding.tvGrnQtyValue.text = wbDetails.grnQty.toString()
        binding.tvGrnNoValue.text = wbDetails.grnNumber.toString()
        binding.tvGrnPriceValue.text = wbDetails.unitPrice.toString()
        binding.etPrice.onChange {
            try {
                val paidData = paidWeight.formatThreeDigits().replace(",", "")
                val totalVal = (wbDetails.unitPrice.toString().toDouble() * paidData.toDouble()) - it.toDouble()
                binding.tvFinalPriceValue.text = totalVal.formatThreeDigits()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaEcuadorBcApprovePostResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToSuccessPage(wbDetails.wbid.toString())
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    moveToFailurePage(wbDetails.wbid.toString(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaEcuadorApproveWbDetail>
                        binding.tvNetWeight.text = approveQualityList[0].qualityParams.B_PAID_WT
                        paidWeight = approveQualityList[0].qualityParams.B_PAID_WT.toDouble()
                        val paidData = paidWeight.formatThreeDigits().replace(",", "")
                        val totalVal = (wbDetails.unitPrice.toString().toDouble() * paidData.toDouble())
                        binding.tvFinalPriceValue.text = totalVal.formatThreeDigits()
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

/*
    private fun updateUIValues() {

        var refWeight: String? = "0"
        approveQualityList[0].qualityParameters?.forEach {
            when {
                it.sapQCName?.equals("CI_REFRACTION") == true -> {
                    binding.tvRefWeight.text = it.satNam
                    refWeight = it.satNam
                }
                it.sapQCName?.equals("CI_RCN_REGION") == true -> {
                    binding.tvOrigin.text = it.satNam
                }
                it.sapQCName?.equals("CI_RCN_KOR") == true -> {
                    binding.tvKor.text = it.satNam
                }
                it.sapQCName?.equals("CI_SLOC_KOR") == true -> {
                    binding.tvStorageLocation.text = it.satNam
                }
            }
        }


        binding.etPrice.onChange {
            try {
                val paidData = paidWeight.formatThreeDigits().replace(",", "")
                val totalVal = it.toDouble() * paidData.toDouble()
                binding.tvToatlValue.text = totalVal.formatThreeDigits()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
*/

/*
    private fun showConfirmDialog() {

        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_bc_approve_cameroon_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.visible()
        mDialogView.llApproval.gone()
        mDialogView.ivClose.setOnClickListener { mAlertDialog?.dismiss() }
        mDialogView.tvConfirmGrn.setOnClickListener {
//            val wbData = preparePostGrnData(wbDetails)
//            vm.postApproval(
//                VegaEcuadorBcApprovePost(
//                    key = getCurrentKey(),
//                    plant = getPlantDetails(),
//                    approvalDetailsList = prepareDeliveryList()
//                )
//            )
            vegaBcApprovePostData = VegaQualityApproveEcuadorPostData()
            vegaBcApprovePostData.weighBridgeId = wbDetails.wbid
            vegaBcApprovePostData.batchNumber = wbDetails.batchNumber
            vegaBcApprovePostData.grnQty = wbDetails.grnQty?.replace(" ","")
            vegaBcApprovePostData.finalApproval = "X"
            vegaBcApprovePostData.item = wbDetails.item
//            vegaBcApprovePostData.plant = wbDetails.plantId
            vegaBcApprovePostData.supplierCode = wbDetails.supplierCode
            vegaBcApprovePostData.materialCode = wbDetails.materialNumber
            vegaBcApprovePostData.sendingStorageLoc = "BFRM"
            vegaBcApprovePostData.uom = "KG"
//            vegaBcApprovePostData.receivingStorageLoc = receivingPlant

            var qualityDetails :ArrayList<QualityDetails> = ArrayList()
            qualityParameterList.forEach{
                var quality = QualityDetails()
                quality.descrChar = it?.descrChar
                quality.nameChar = it?.nameChar
                quality.qualityParameterValue = it?.qualityParameterValue
                qualityDetails.add(quality)
            }
            vm.postApproval(
                VegaEcuadorPostApprovalRequest(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    approvalDetails = vegaBcApprovePostData
                )
            )
//            vm.postGrn(VegaEcuadorQualityApproveGrnPost(key = getCurrentKey(), plant = getPlantDetails(), grnData = listOf(wbData)))
        }
        mAlertDialog?.show()
    }
*/

/*
    private fun prepareDeliveryList(): List<VegaEcuadorBcApproveDetails> {
        val list = ArrayList<VegaEcuadorBcApproveDetails>()
        for (item in finalBcApproveWblist) {
            val deliveryDetail = VegaEcuadorBcApproveDetails()
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.plant = selectedplantid
            deliveryDetail.autoTransfer = ""
            deliveryDetail.discount = item.discount
            deliveryDetail.finalApproval = item.finalApproval
            deliveryDetail.grnQty = item.grnQty?.trim()
            deliveryDetail.item = item.item
            deliveryDetail.msg = ""
            deliveryDetail.paidWeight = ""
            deliveryDetail.priceCharacter = ""
            deliveryDetail.qchar = item.qchar
            deliveryDetail.qualityDetails = ArrayList<VegaEcuadorBcApproveQualityDetails>()
            deliveryDetail.receivingStorageLoc = ""
            deliveryDetail.sendingStorageLoc = ""
            deliveryDetail.status = false
            deliveryDetail.supplierCode = item.supplierCode
            deliveryDetail.uom = item.unitsOfMeasure
            deliveryDetail.waers = item.waers
            deliveryDetail.weighBridgeId = item.wbid
            deliveryDetail.poNumber = item.poNumber

            list.add(deliveryDetail)
        }
        return list
    }
*/

   /* private fun callQualityFragment(wbidParams: VegaEcuadorQualityApproveWeighBridgeId) {
        val fragment = VegaBcApproveCameroonFragment()
        val args = Bundle()
        weighBridgeId = wbidParams
        args.putParcelable(APPROVE_DATA, weighBridgeId)
        fragment.arguments = args
        mListener?.onFragmentInteraction(fragment)
    }*/

    private fun moveToSuccessPage(wbId: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
//        intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
        intent.putExtra(AppUtils.SUB_TITLE, wbId)
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, "Approve Failed")
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }
}
