package com.olam.warehouse.vegax.approve.ui.details

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
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approve.R
import com.olam.warehouse.vegax.approve.data.domain.model.*
import com.olam.warehouse.vegax.approve.databinding.FragmentVegaApproveDetailsBinding
import com.olam.warehouse.vegax.approve.ui.OnFragmentInteractionListener
import com.olam.warehouse.vegax.approve.ui.VegaApproveViewModel
import com.olam.warehouse.vegax.approve.ui.quality.VegaApproveQualityFragment
import com.olam.warehouse.vegax.approve.utils.APPROVE_DATA
import com.olam.warehouse.vegax.approve.utils.APPROVE_QUALITY_DATA
import com.olam.warehouse.vegax.approve.utils.preparePostGrnData
import kotlinx.android.synthetic.main.cutsom_dialog_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
class VegaApproveDetailsFragment : BaseFragment() {


    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaApproveViewModel by viewModel()
    private var approveQuality = mutableListOf<VegaApproveQuality>()
    private var approveQualityList = ArrayList<VegaApproveQuality>()
    private var weighBridgeId = VegaApproveWeighBridgeId()
    private var wbDetails = VegaApproveWeighBridgeId()
    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var binding: FragmentVegaApproveDetailsBinding

    override val layoutResourceId = R.layout.fragment_vega_approve_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaApproveDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("approve/ui/details/VegaApproveDetailsFragment").title("Approve").with(tracker)
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
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        wbDetails = arguments?.get(APPROVE_DATA) as VegaApproveWeighBridgeId

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString())
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })
        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })
        binding.tvFcfaLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.final_price_per_unit)) { mandatoryStars() } }
        binding.btnProceed.setOnClickListener {
            wbDetails.unitPrice = binding.etPrice.text.toString()
            when {
                binding.etPrice.text.isNullOrEmpty() -> getString(R.string.price_vaidation)
                else -> showConfirmDialog()
            }
        }
        binding.btnQualityDetails.setOnClickListener {
            callQualityFragment(wbDetails)
        }

    }

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaApprovalResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToSuccessPage(wbDetails.weighBridgeId.toString())
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    //context?.toast(it.error.toString())
                    moveToFailurePage(wbDetails.weighBridgeId.toString(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    showConfirmApproveDialog(it.data)
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

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaApproveQuality>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaApproveQuality>
                        updateUIValues()
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

    private fun updateUIValues() {
        binding.tvTruckIDNo.text = getString(R.string.truck_id).plus(wbDetails.vehicleNumber)
        binding.tvTruckNo.text = wbDetails.vehicleNumber
        binding.tvTruckSupplier.text = wbDetails.supplierName
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        var refWeight: String? = "0"
        approveQualityList[0].qualityParameters.forEach {
            if (it.sapQCName?.equals("CI_REFRACTION")!!) {
                binding.tvRefWeight.text = it.satNam
                refWeight = it.satNam
            } else if (it.sapQCName?.equals("CI_RCN_REGION")!!) {
                binding.tvOrigin.text = it.satNam
            } else if (it.sapQCName?.equals("CI_RCN_KOR")!!) {
                binding.tvKor.text = it.satNam
            } else if (it.sapQCName?.equals("CI_SLOC_KOR")!!) {
                binding.tvStorageLocation.text = it.satNam
            }
        }

        if (!refWeight.equals("0")) {
            if (refWeight?.replace(",", "")!!.split(" ")[1].equals("MT")) {
                paidWeight =
                    wbDetails.netWeight.toDouble() - (refWeight!!.replace(",", "").split(" ")[0].toDouble() * 1000)
            } else if (refWeight?.replace(",", "")!!.split(" ")[1].equals("KG")) {
                paidWeight = wbDetails.netWeight.toDouble() - (refWeight!!.replace(",", "").split(" ")[0].toDouble())
            }
        } else paidWeight = wbDetails.netWeight.toDouble()


        binding.tvPaidWeight.text =
            paidWeight.formatThreeDigits().replace(",", "").plus(" ").plus(wbDetails.unitsOfMeasure)
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

    private fun showConfirmDialog() {

        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.visible()
        mDialogView.llApproval.gone()
        mDialogView.ivClose.setOnClickListener { mAlertDialog?.dismiss() }
        mDialogView.tvConfirmGrn.setOnClickListener {
            wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
            val wbData = preparePostGrnData(wbDetails)
            vm.postGrn(VegaGrnPost(key = getCurrentKey(), plant = getPlantDetails(), grnData = listOf(wbData)))
        }
        mAlertDialog?.show()
    }

    private fun showConfirmApproveDialog(data: GenericReqAndResp<VegaGrnResponse>?) {

        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.gone()
        mDialogView.llApproval.visible()
        mDialogView.ivClose.gone()
        mDialogView.tvConfirmApproval.setOnClickListener {
            val vegaApprovePostData = VegaApprovePostData()
            vegaApprovePostData.weighBridgeId = wbDetails.weighBridgeId
            vegaApprovePostData.paidWeight = binding.tvPaidWeight.text.toString().split(" ")[0]
            val messageNav: List<MessageNav> = emptyList()
            val charsNav: List<CharsNav> = emptyList()
            vegaApprovePostData.charsNav = charsNav
            vegaApprovePostData.messageNav = messageNav
            vm.postApproval(
                PostApprovalData(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    approvalDetails = vegaApprovePostData
                )
            )
        }
        if (data?.data != null) {
            mDialogView.tvGrnNo.text =
                data.message.plus("\n").plus(getString(R.string.grn_no)).plus(data.data.grnNumber)
        } else {
            mDialogView.tvGrnNo.text = data?.errors
        }
        mAlertDialog?.show()
    }
    private fun callQualityFragment(wbidParams: VegaApproveWeighBridgeId) {
        val fragment = VegaApproveQualityFragment()
        val args = Bundle()
        weighBridgeId = wbidParams
        args.putParcelable(APPROVE_DATA, weighBridgeId)
        args.putParcelableArrayList(APPROVE_QUALITY_DATA, approveQualityList)
        fragment.arguments = args
        mListener?.onFragmentInteraction(fragment)
    }

    private fun moveToSuccessPage(wbId: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
        intent.putExtra(AppUtils.SUB_TITLE, wbId)
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, "Approve Failed")
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }
}
