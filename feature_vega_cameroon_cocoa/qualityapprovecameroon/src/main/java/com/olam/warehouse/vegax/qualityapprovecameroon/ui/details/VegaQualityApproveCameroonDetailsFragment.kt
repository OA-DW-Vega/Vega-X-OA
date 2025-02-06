package com.olam.warehouse.vegax.qualityapprovecameroon.ui.details

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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
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
import com.olam.warehouse.vegax.qualityapprovecameroon.R
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.VegaQualityApproveCameroon
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.VegaQualityApproveCameroonGrnPost
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.VegaQualityApproveCameroonResponse
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.VegaQualityApproveCameroonWeighBridgeId
import com.olam.warehouse.vegax.qualityapprovecameroon.databinding.CutsomDialogQualtyApproveCameroonLayoutBinding
import com.olam.warehouse.vegax.qualityapprovecameroon.databinding.FragmentVegaQualtyApproveCameroonDetailsBinding
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.OnFragmentQualityApproveCameroonInteractionListener
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.VegaQualityApproveCameroonViewModel
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.quality.VegaQualityApproveCameroonFragment
import com.olam.warehouse.vegax.qualityapprovecameroon.utils.APPROVE_DATA
import com.olam.warehouse.vegax.qualityapprovecameroon.utils.preparePostGrnData
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
class VegaQualityApproveCameroonDetailsFragment : BaseFragment() {


    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaQualityApproveCameroonViewModel by viewModel()
    private var approveQualityList = ArrayList<VegaQualityApproveCameroon>()
    private var weighBridgeId = VegaQualityApproveCameroonWeighBridgeId()
    private var wbDetails = VegaQualityApproveCameroonWeighBridgeId()
    private var mListener: OnFragmentQualityApproveCameroonInteractionListener? = null

    private lateinit var binding: FragmentVegaQualtyApproveCameroonDetailsBinding

    override val layoutResourceId = R.layout.fragment_vega_qualty_approve_cameroon_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaQualtyApproveCameroonDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("qualityapprovecameroon/ui/details/VegaCameroonProcessingSelectTypeFragment")
            .title("Vega_Cameroon/Approve")
            .with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentQualityApproveCameroonInteractionListener) {
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
        wbDetails = arguments?.get(APPROVE_DATA) as VegaQualityApproveCameroonWeighBridgeId

        vm.getQualityDetails(wbDetails.charg.toString(), wbDetails.materialNumber.toString())
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })
        binding.tvFcfaLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.final_price_per_unit)) { mandatoryStars() } }
        binding.btnProceed.setOnClickListener {
            wbDetails.unitPrice = binding.etPrice.text.toString()
            wbDetails.basePrice = binding.etPrice.text.toString()
            when {
                binding.etPrice.text.isNullOrEmpty() -> getString(R.string.price_vaidation)
                else -> showConfirmDialog()
            }
        }
        binding.btnQualityDetails.setOnClickListener {
            callQualityFragment(wbDetails)
        }

    }

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>?) {
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


    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaQualityApproveCameroon>
                        updateUIValues()
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

    private fun updateUIValues() {

        var refWeight: String? = "0"
        approveQualityList[0].qualityParameters?.forEach {
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

//        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_qualty_approve_cameroon_layout, null)
        val mDialogView =
            CutsomDialogQualtyApproveCameroonLayoutBinding.inflate(LayoutInflater.from(activity?.applicationContext))
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView.root)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.visible()
        mDialogView.llApproval.gone()
        mDialogView.ivClose.setOnClickListener { mAlertDialog?.dismiss() }
        mDialogView.tvConfirmGrn.setOnClickListener {
            val wbData = preparePostGrnData(wbDetails)
            vm.postGrn(
                VegaQualityApproveCameroonGrnPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    grnData = listOf(wbData)
                )
            )
        }
        mAlertDialog?.show()
    }

    private fun callQualityFragment(wbidParams: VegaQualityApproveCameroonWeighBridgeId) {
        val fragment = VegaQualityApproveCameroonFragment()
        val args = Bundle()
        weighBridgeId = wbidParams
        args.putParcelable(APPROVE_DATA, weighBridgeId)
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
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }
}
