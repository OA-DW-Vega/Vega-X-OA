package com.olam.warehouse.vegax.qualityapprovenigeria.ui.details

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
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovenigeria.R
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovenigeria.databinding.FragmentVegaQualtyApproveNigeriaDetailsBinding
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.OnFragmentQualityApproveNigeriaInteractionListener
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.VegaQualityApproveNigeriaViewModel
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.quality.VegaQualityApproveNigeriaFragment
import com.olam.warehouse.vegax.qualityapprovenigeria.utils.APPROVE_DATA
import com.olam.warehouse.vegax.qualityapprovenigeria.utils.preparePostGrnData
import kotlinx.android.synthetic.main.cutsom_dialog_qualty_approve_nigeria_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
class VegaQualityApproveNigeriaDetailsFragment : BaseFragment() {


    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaQualityApproveNigeriaViewModel by viewModel()
    private var approveQuality = mutableListOf<VegaQualityApproveNigeria>()
    private var approveQualityList = ArrayList<VegaQualityApproveNigeria>()
    private var weighBridgeId = VegaQualityApproveNigeriaWeighBridgeId()
    private var wbDetails = VegaQualityApproveNigeriaWeighBridgeId()
    private var mListener: OnFragmentQualityApproveNigeriaInteractionListener? = null

    private lateinit var binding: FragmentVegaQualtyApproveNigeriaDetailsBinding

    override val layoutResourceId = R.layout.fragment_vega_qualty_approve_nigeria_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaQualtyApproveNigeriaDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualityapproveNigeria/ui/details/VegaNigeriaProcessingSelectTypeFragment").title("Vega_Nigeria/Approve")
            .with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentQualityApproveNigeriaInteractionListener) {
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
        wbDetails = arguments?.get(APPROVE_DATA) as VegaQualityApproveNigeriaWeighBridgeId

        vm.getQualityDetails(wbDetails.charg.toString(), wbDetails.materialNumber.toString())
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
//        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })
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

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaQualityApproveNigeriaResponse>>?) {
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
                    //context?.toast(it.error.toString())
                    moveToFailurePage(wbDetails.wbid.toString(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse>>) {
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

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaQualityApproveNigeria>
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
/*        binding.tvTruckIDNo.text = getString(R.string.truck_id).plus(wbDetails.vehicleNumber)
        binding.tvTruckNo.text = wbDetails.vehicleNumber
        binding.tvTruckSupplier.text = wbDetails.supplierName
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)*/
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

/*        if (!refWeight.equals("0")) {
            if (refWeight?.replace(",", "")!!.split(" ")[1].equals("MT")) {
                paidWeight =
                    wbDetails.netWeight.toDouble() - (refWeight!!.replace(",", "").split(" ")[0].toDouble() * 1000)
            } else if (refWeight?.replace(",", "")!!.split(" ")[1].equals("KG")) {
                paidWeight = wbDetails.netWeight.toDouble() - (refWeight!!.replace(",", "").split(" ")[0].toDouble())
            }
        } else paidWeight = wbDetails.netWeight.toDouble()*/


/*        binding.tvPaidWeight.text =
            paidWeight.formatThreeDigits().replace(",", "").plus(" ").plus(wbDetails.unitsOfMeasure)*/
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

        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_qualty_approve_nigeria_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.visible()
        mDialogView.llApproval.gone()
        mDialogView.ivClose.setOnClickListener { mAlertDialog?.dismiss() }
        mDialogView.tvConfirmGrn.setOnClickListener {
//            wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
            val wbData = preparePostGrnData(wbDetails)
            vm.postGrn(VegaQualityApproveNigeriaGrnPost(key = getCurrentKey(), plant = getPlantDetails(), grnData = listOf(wbData)))
        }
        mAlertDialog?.show()
    }

    private fun showConfirmApproveDialog(data: GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse>?) {

        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_qualty_approve_nigeria_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.gone()
        mDialogView.llApproval.visible()
        mDialogView.ivClose.gone()
        /*mDialogView.tvConfirmApproval.setOnClickListener {
            val vegaApprovePostData = VegaQualityApproveNigeriaPostData()
            vegaApprovePostData.weighBridgeId = wbDetails.wbid
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
        }*/
        if (data?.data != null) {
            mDialogView.tvGrnNo.text =
                data.message.plus("\n").plus(getString(R.string.grn_no)).plus(data.data.grnNumber)
        } else {
            mDialogView.tvGrnNo.text = data?.errors
        }
        mAlertDialog?.show()
    }
    private fun callQualityFragment(wbidParams: VegaQualityApproveNigeriaWeighBridgeId) {
        val fragment = VegaQualityApproveNigeriaFragment()
        val args = Bundle()
        weighBridgeId = wbidParams
        args.putParcelable(APPROVE_DATA, weighBridgeId)
//        args.putParcelableArrayList(APPROVE_QUALITY_DATA, approveQualityList)
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
