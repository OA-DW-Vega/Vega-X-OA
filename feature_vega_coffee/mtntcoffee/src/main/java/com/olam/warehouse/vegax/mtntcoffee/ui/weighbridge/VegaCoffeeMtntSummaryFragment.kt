package com.olam.warehouse.vegax.mtntcoffee.ui.weighbridge

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
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcoffee.R
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeDeliveryPost
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntcoffee.databinding.FragmentCoffeeDispatchMtntSummaryBinding
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeMtntViewModel
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntcoffee.utils.WEIGHBRIDGE
import kotlinx.android.synthetic.main.item_coffee_lot_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaCoffeeMtntSummaryFragment : BaseFragment(),
    VegaCoffeeMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_coffee_dispatch_mtnt_summary
    private lateinit var binding: FragmentCoffeeDispatchMtntSummaryBinding
    private var callBack: VegaCoffeeReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaCoffeeMtntViewModel by viewModel()
    private lateinit var vegaCoffeeDeliveryPost: VegaCoffeeDeliveryPost
    private var vegaCocoaMtntWithLots: VegaCocoaMtntWithLots? = null

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaCoffeeMtntSummaryFragment()
            .putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCoffeeDispatchMtntSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {

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
        binding.rvLots.setUp(lots, R.layout.item_coffee_lot_summary, { item, pos ->
            tvLotId.text = item.batchNumber
            tvStLocation.text = item.storageLocationCode
            tvWeightValue.text =
                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.unitOfMeasure)
            tvGradeValue.text = item.materialName
            tvEditedWeight.text =
                item.editedWeight?.toDouble()?.formatThreeDigits().toString().plus(" ").plus(item.unitOfMeasure)
            tvUnit.visibility = View.GONE
            tvEditedWeight.visibility = View.VISIBLE
            ivClose.setImageDrawable(ivClose.context.getDrawable(R.drawable.ic_coffee_edit_gray))
            cbSelectAll.visibility = View.GONE
            tvSelectAll.visibility = View.GONE
            cbEndLot.visibility = View.GONE
            tvEndLot.visibility = View.GONE
            etWeight.visibility = View.GONE
            ivClose.setOnClickListener { itemRemoved(item) }
        })
    }

    private fun postDelivery() {
        vegaCoffeeDeliveryPost = VegaCoffeeDeliveryPost(
            getCurrentKey(),
            getPlantDetails(),
            "",
          "",
            "",
            "",
            prepareDeliveryList(), WEIGHBRIDGE
        )
        vm.postDeliveryDetail(vegaCoffeeDeliveryPost)
    }


    private fun prepareDeliveryList(): List<VegaCoffeeMtntDeliveryDetail> {
        val list = ArrayList<VegaCoffeeMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaCoffeeMtntDeliveryDetail()
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
                view.context.getString(R.string.proceed),
                view.context.getString(com.olam.warehouse.presentation.R.string.cancel),
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
                    UIUtils.showErrorDialog(requireContext(), response.data?.message ?: "")
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
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
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
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }
}
