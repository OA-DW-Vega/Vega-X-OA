package com.olam.warehouse.odreceiving.ui.summary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingPost
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingResponse
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingSummaryWeighScalePalletBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_POST_DATA
import com.olam.warehouse.odreceiving.utils.getTmpId
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.SUB_TITLE
import com.olam.warehouse.presentation.utils.AppUtils.TITLE
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingSummaryWeighScalePalletFragment : BaseFragment() {

    private val vm: DOReceivingViewModel by viewModel()
    private var receivingData = DOReceiving()
    private var postData = mutableListOf<DOReceiving>()

    private lateinit var binding: FragmentDoReceivingSummaryWeighScalePalletBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_summary_weigh_scale_pallet

    companion object {
        fun newInstance(data: DOReceiving, postData: ArrayList<DOReceiving>?) =
            DOReceivingSummaryWeighScalePalletFragment().putArgs {
                putParcelable(RECEIVING_DATA, data)
                putParcelableArrayList(RECEIVING_POST_DATA, postData)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingSummaryWeighScalePalletBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/summary/DOReceivingSummaryWeighScalePalletFragment")
            .title("OD/Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        receivingData = arguments?.getParcelable<DOReceiving>(RECEIVING_DATA)!!
        postData = arguments?.getParcelableArrayList<DOReceiving>(RECEIVING_POST_DATA)!!

        val tare = vm.getTareWeight(postData)
        val totalTare = tare.plus(receivingData.palletWeight ?: 0.0)
        receivingData.tareWeight = totalTare.format()
        receivingData.bagWeight = tare
        receivingData.grossWeight =
            vm.getGrossWeight(postData).plus(receivingData.palletWeight ?: 0.0)
        receivingData.bagCount = vm.getBagCount(postData).toString()
        receivingData.netWeight = vm.getNetWeight(postData)

        val tareWeight = receivingData.tareWeight?.toDouble()?.format().plus(" Kg")
        val grossWeight = receivingData.grossWeight.format().plus(" Kg")
        binding.tvProductType.text = receivingData.materialName
        binding.tvSupplier.text = receivingData.supplierName
        binding.tvNoOfPallet.text = receivingData.palletCount
        binding.tvPalletWeight.text = receivingData.palletWeight?.format().plus(" Kg")
        binding.tvBagWeight.text = receivingData.bagWeight?.format().plus(" Kg")
        binding.tvTareWeight.text = tareWeight
        binding.tvGrossWeight.text = grossWeight
        binding.tvGross.text = grossWeight
        binding.tvTare.text = tareWeight
        binding.tvNetWeight.text = receivingData.netWeight.format().plus(" Kg")
        binding.tvReceivedOn.text = DateUtils.fromMillisToTimeString(DateUtils.getCurrentTimeInMills())

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(data: Resource<GenericReqAndResp<DOReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId, true)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    requireContext().toast("${it.error}")
                }
            }
        }
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
        hideLoading()
        receivingData.wbId = if (syncStatus) wbId ?: "" else ""
        receivingData.tmpWbId = wbId ?: ""
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        vm.saveReceiving(receivingData)
        if (!syncStatus) {
            postData.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(postData)
        }
        moveToSuccessPage(wbId)
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isOnline()) intent.putExtra(TITLE, getString(R.string.receiving_success)) else intent.putExtra(
            TITLE,
            getString(R.string.receiving_success_offline)
        )
        intent.putExtra(SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                { postReceiving() },
                { dismiss() })
        }
    }

    private fun postReceiving() {
        if (isOnline()) {
            vm.postReceivingData(DOReceivingPost(getCurrentKey(), getPlantDetails(), null,null, postData))
        } else {
            //prepareSuccessData(getTmpId(), false)
            prepareSuccessData(if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId, false)
        }
    }
}
