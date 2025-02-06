package com.olam.warehouse.vegax.mtntcameroon.ui.weighbridge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcameroon.R
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonDeliveryPost
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntcameroon.databinding.FragmentCameroonDispatchMtntSummaryBinding
import com.olam.warehouse.vegax.mtntcameroon.databinding.ItemCameroonLotSummaryBinding
import com.olam.warehouse.vegax.mtntcameroon.ui.VegaCameroonMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntcameroon.ui.VegaCameroonMtntViewModel
import com.olam.warehouse.vegax.mtntcameroon.ui.VegaCameroonReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntcameroon.utils.WEIGHBRIDGE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaCameroonMtntSummaryFragment : BaseFragment(),
    VegaCameroonMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_cameroon_dispatch_mtnt_summary
    private lateinit var binding: FragmentCameroonDispatchMtntSummaryBinding
    private var callBack: VegaCameroonReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaCameroonMtntViewModel by viewModel()
    private lateinit var vegaCameroonDeliveryPost: VegaCameroonDeliveryPost
    private var vegaCocoaMtntWithLots: VegaCocoaMtntWithLots? = null

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaCameroonMtntSummaryFragment()
            .putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCameroonReplaceFragmentCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentCameroonDispatchMtntSummaryBinding.inflate(inflater)
        initExtra()
        initUi()

        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("mtntcameroon/ui/weighbridge/VegaCameroonMtntSummaryFragment")
            .title("Vega_Cameroon/Mtnt").with(tracker)
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btProceed, it, true)
        }
        vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
        vm.weighBridgeWithLotsSource.observe(viewLifecycleOwner, Observer {
            if (it != null) updateLocalDbData(it)
        })

        binding.tvTruckValue.text = summaryObj?.vehicleNumber
        binding.tvRemarkValue.text = summaryObj?.remarks
        binding.clSummary.tvStoNo.text = getString(R.string.obd_number)
        val times = summaryObj?.erdat?.split('(', ')')
        binding.clSummary.tvDateValue.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }
        binding.clSummary.tvStoNoValue.text = summaryObj?.delivery
        binding.clSummary.tvMaterialName.text = summaryObj?.materialName
        binding.clSummary.tvStoWeightValue.text = summaryObj?.deliveryQty.plus(" ").plus(summaryObj?.deliveryUOM)
        binding.btProceed.setOnClickListener { showConformationDialog() }
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        binding.tvRemarkValue.text = summaryObj?.remarks


    }

    private fun updateLocalDbData(weighBridge: VegaCocoaMtntWithLots) {
        vegaCocoaMtntWithLots = weighBridge
        setUpMaterialAdapter()
        dispatchLotsList.clear()
        setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
    }


    private fun setUpMaterialAdapter() {
        val list = ArrayList<VegaCoffeePurchaseOrderMaterialModel>()
        list.add(
            VegaCoffeePurchaseOrderMaterialModel(
                vm.dispatchWh.weighBridgeId,
                vm.dispatchWh.materialCode ?: "",
                vm.dispatchWh.materialName,
                vm.dispatchWh.netWeight,
                vm.dispatchWh.unitsOfMeasure
            )
        )
    }

    private fun setUpAdapter(list: List<VegaCocoaDispatchLots>) {
        val lots = list as MutableList
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_cameroon_lot_summary,
            ItemCameroonLotSummaryBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocation.text = item.storageLocationCode
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                        ?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                bindItem.tvEditedWeight.text =
                    item.editedWeight?.toDouble()?.formatThreeDigits().toString().plus(" ")
                        .plus(item.unitOfMeasure)
                bindItem.tvUnit.visibility = View.GONE
                bindItem.tvEditedWeight.visibility = View.VISIBLE
                bindItem.ivClose.setImageDrawable(bindItem.ivClose.context.getDrawable(R.drawable.ic_cameroon_edit_gray))
                bindItem.cbSelectAll.visibility = View.GONE
                bindItem.tvSelectAll.visibility = View.GONE
                bindItem.cbEndLot.visibility = View.GONE
                bindItem.tvEndLot.visibility = View.GONE
                bindItem.etWeight.visibility = View.GONE
                bindItem.ivClose.setOnClickListener { itemRemoved(item) }
            })
    }

    private fun postDelivery() {
        vegaCameroonDeliveryPost = VegaCameroonDeliveryPost(
            getCurrentKey(),
            getPlantDetails(),
            "",
            prepareDeliveryList(), WEIGHBRIDGE
        )
        vm.postDeliveryDetail(vegaCameroonDeliveryPost)
    }


    private fun prepareDeliveryList(): List<VegaCameroonMtntDeliveryDetail> {
        val list = ArrayList<VegaCameroonMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaCameroonMtntDeliveryDetail()
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.endLotFlag = item.isEndLot ?: false
            deliveryDetail.netWeight = item.editedWeight
            deliveryDetail.createdDate = summaryObj?.startTime
            deliveryDetail.purchaseDocNum = summaryObj?.purchaseDocNum
            deliveryDetail.purchaseDocDesc = summaryObj?.purchaseDocDesc
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.weighBridgeId = summaryObj?.weighBridgeId
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.startTime = summaryObj?.startTime
            deliveryDetail.endTime = summaryObj?.endTime
            deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
            deliveryDetail.pickingFlag = summaryObj?.pickingFlag ?: false
            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.deliveryFlag = summaryObj?.deliveryFlag ?: false
            deliveryDetail.deliveryItem = summaryObj?.deliveryItem
            deliveryDetail.delivery = summaryObj?.delivery
            deliveryDetail.year = year.toString()
            list.add(deliveryDetail)
        }
        return list
    }


    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaDeliveryPostResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    moveToSuccessPage(response.data?.data?.delivery)
                    vm.updateSyncStatus(vegaCocoaMtntWithLots!!)
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                    val delivery = response.data?.data
                    summaryObj?.pickingFlag = delivery?.pickingFlag ?: false
                    summaryObj?.deliveryFlag = delivery?.deliveryFlag ?: false
                    summaryObj?.delivery = delivery?.delivery
                    summaryObj?.storageLossFlag = delivery?.storageLossFlag ?: false
                    summaryObj?.deliveryItem = delivery?.deliveryItem ?: ""
                    vm.dispatchWh = summaryObj!!
                    vm.saveWeighBridgeDetails()
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun moveToSuccessPage(deliveryId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.mtnt_wb_success, deliveryId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showUpdateRemarkDialog() {
        showDialog(
            getString(R.string.update_remark_title),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    summaryObj?.remarks = remark
                    binding.tvRemarkValue.text = remark
                    vm.updateRemarks(remark, true, summaryObj?.weighBridgeId ?: "")
                }
            },
            true, summaryObj?.remarks ?: ""
        )
    }

    override fun itemRemoved(item: VegaCocoaDispatchLots) {
        activity?.onBackPressed()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
