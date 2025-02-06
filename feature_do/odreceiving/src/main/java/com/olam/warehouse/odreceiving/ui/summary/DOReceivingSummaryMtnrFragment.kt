package com.olam.warehouse.odreceiving.ui.summary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingPost
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingResponse
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingSummaryMtnrBinding
import com.olam.warehouse.odreceiving.databinding.ItemDoReceivingSummaryMtnrBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_POST_DATA
import com.olam.warehouse.odreceiving.utils.WS01
import com.olam.warehouse.odreceiving.utils.getTmpId
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.SUB_TITLE
import com.olam.warehouse.presentation.utils.AppUtils.TITLE
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingSummaryMtnrFragment : BaseFragment() {

    private var receivingData = DOReceiving()
    private var postData = mutableListOf<DOReceiving>()
    private val vm: DOReceivingViewModel by viewModel()
    private var isWeighScale = false
    private var mTareWeight = 0.0
    private var mNetWeight = 0.0
    private var mGrossWeight = 0.0
    private var mBagCount = 0

    private lateinit var binding: FragmentDoReceivingSummaryMtnrBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_summary_mtnr

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingSummaryMtnrBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/summary/DOReceivingSummaryMtnrFragment").title("OD/Receiving")
            .with(tracker)
        initExtras()
        initUI()
    }

    companion object {
        fun newInstance(data: DOReceiving, postData: ArrayList<DOReceiving>?) =
            DOReceivingSummaryMtnrFragment().putArgs {
                putParcelable(RECEIVING_DATA, data)
                putParcelableArrayList(RECEIVING_POST_DATA, postData)
            }
    }

    private fun initExtras() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        postData = arguments?.getParcelableArrayList<DOReceiving>(RECEIVING_POST_DATA)!!
        isWeighScale = receivingData.wsGate == WS01
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        mTareWeight = vm.getTareWeight(postData)
        mGrossWeight = vm.getGrossWeight(postData)
        mNetWeight = vm.getNetWeight(postData)
        mBagCount = vm.getBagCount(postData)
        binding.tvMtnNo.text = receivingData.mtnCode
        binding.tvWarehouse.text = receivingData.plantId
        binding.tvReceivedOn.text = DateUtils.fromMillisToTimeString(getCurrentTimeInMills())
        binding.tvPalletCount.isGone = isWeighScale
        binding.tvBagCount.text = mBagCount.toString()
        binding.tvTotalWeight.text = mNetWeight.format().plus(" KG")
        binding.tvNetWeight.text = mNetWeight.format().plus(" KG")
        binding.tvTare.text = mTareWeight.format().plus(" KG")
        binding.tvGross.text = mGrossWeight.format().plus(" KG")
        binding.tvPalletCount.isGone = isWeighScale
        binding.tvPallet.isGone = isWeighScale
        setUpRecyclerView()

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

    private fun setUpRecyclerView() {
        if (postData.isNotEmpty()) {
            val map = postData.groupBy { it.charg }
            val lotIds = mutableListOf<String>()
            map.forEach { (key, _) -> lotIds.add(key) }
            binding.rvSummaryMtn.setUpAdapter(
                lotIds,
                R.layout.item_do_receiving_summary_mtnr,
                ItemDoReceivingSummaryMtnrBinding::inflate,
                { it, pos, bindingItem ->
                    val rec = map[it]
                    bindingItem.tvLotNo.text = it
                    bindingItem.tvBag.text =
                        rec?.sumBy { _rec -> _rec.bagCount?.toInt() ?: 0 }.toString()
                    bindingItem.tvPallet.isGone = isWeighScale
                    if (!isWeighScale) {
                        bindingItem.tvPallet.text =
                            rec?.sumBy { _rec -> _rec.palletCount?.toInt() ?: 0 }.toString()
                    }
                    bindingItem.tvWeight.text =
                        rec?.sumByDouble { _rec -> _rec.netWeight }?.format().toString()
                })
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_message)
            getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                { postReceiving() },
                { dismiss() })
        }
    }

    private fun postReceiving() {
        if (isOnline()) {
            vm.postReceivingMtnData(DOReceivingPost(getCurrentKey(),  getPlantDetails(), null, null, postData))
        } else {
            // prepareSuccessData(getTmpId(), false)
            prepareSuccessData(if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId, false)
        }
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
        hideLoading()
        receivingData.wbId = if (syncStatus) wbId ?: "" else ""
        receivingData.tmpWbId = wbId ?: ""
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        receivingData.netWeight = mNetWeight
        receivingData.tareWeight = mTareWeight.toString()
        receivingData.grossWeight = mGrossWeight
        receivingData.bagCount = mBagCount.toString()
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

}
