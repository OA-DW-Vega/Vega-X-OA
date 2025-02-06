package com.olam.warehouse.vegax.mtntghana.ui.weighbridge

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
import com.olam.warehouse.master.vega.entity.VegaGhanaPurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.data.domain.model.TextNavListValues
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntDeliveryPost
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaDispatchMtntSummaryBinding
import com.olam.warehouse.vegax.mtntghana.databinding.ItemGhanaLotSummaryBinding
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntViewModel
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghana.utils.WEIGHBRIDGE
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaGhanaMtntSummaryFragment : BaseFragment(),
    VegaGhanaMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_ghana_dispatch_mtnt_summary
    private lateinit var binding: FragmentGhanaDispatchMtntSummaryBinding
    private var callBack: VegaGhanaReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaGhanaMtntViewModel by viewModel()
    private lateinit var vegaCoffeeDeliveryPost: VegaGhanaMtntDeliveryPost
    private var vegaCocoaMtntWithLots: VegaGhanaCocoaMtntWithLots? = null
    private var jsonData = mutableListOf<String>()
    private var textUpdate = ArrayList<String>()

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaGhanaMtntSummaryFragment()
            .putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaDispatchMtntSummaryBinding.inflate(inflater)
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

        binding.tvRemark.gone()
        binding.cvRemark.gone()
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

    private fun updateLocalDbData(weighBridge: VegaGhanaCocoaMtntWithLots) {
        vegaCocoaMtntWithLots = weighBridge
        setUpMaterialAdapter()
        dispatchLotsList.clear()
        setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
    }


    private fun setUpMaterialAdapter() {
        val list = ArrayList<VegaGhanaPurchaseOrderMaterialModel>()
        list.add(
            VegaGhanaPurchaseOrderMaterialModel(
                vm.dispatchWh.weighBridgeId,
                vm.dispatchWh.materialCode ?: "",
                vm.dispatchWh.materialName,
                vm.dispatchWh.netWeight,
                vm.dispatchWh.unitsOfMeasure
            )
        )
    }

    private fun setUpAdapter(list: List<VegaGhanaCocoaDispatchLots>) {
        val lots = list as MutableList
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_ghana_lot_summary,
            ItemGhanaLotSummaryBinding::inflate,
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
                bindItem.ivClose.setImageDrawable(bindItem.ivClose.context.getDrawable(R.drawable.ic_ghana_edit_gray))
                bindItem.cbSelectAll.visibility = View.GONE
                bindItem.tvSelectAll.visibility = View.GONE
                bindItem.cbEndLot.visibility = View.GONE
                bindItem.tvEndLot.visibility = View.GONE
                bindItem.etWeight.visibility = View.GONE
                bindItem.ivClose.setOnClickListener { itemRemoved(item) }
            })
    }

    private fun postDelivery() {
        vegaCoffeeDeliveryPost = VegaGhanaMtntDeliveryPost(
            getCurrentKey(),
            getPlantDetails(),
            "",
            prepareDeliveryList(),
            textUpdteList(),
            WEIGHBRIDGE,
            "",
            "",
            "",
            ""
        )
        vm.postDeliveryDetail(vegaCoffeeDeliveryPost)
    }

    private fun textUpdteList(): List<TextNavListValues> {
        val list = ArrayList<TextNavListValues>()
        textUpdate.forEach {
            if(it.toString().isNotEmpty()) {
                val textNavListValues = TextNavListValues()
                var textValue = (it.toString()).split("-")
                if (textValue[1].trim().equals("Z006")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.tvTruckValue.text.toString()
                } else {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = textValue[0].trim()
                }
                list.add(textNavListValues)
            }
        }
        return list
    }

    private fun prepareDeliveryList(): List<VegaGhanaMtntDeliveryDetail> {
        val list = ArrayList<VegaGhanaMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaGhanaMtntDeliveryDetail()
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
                getString(com.olam.warehouse.presentation.R.string.cancel),
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

    override fun itemRemoved(item: VegaGhanaCocoaDispatchLots) {
        activity?.onBackPressed()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
