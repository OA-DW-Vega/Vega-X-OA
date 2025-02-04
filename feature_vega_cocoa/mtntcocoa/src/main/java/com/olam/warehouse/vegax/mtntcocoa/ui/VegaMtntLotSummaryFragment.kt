package com.olam.warehouse.vegax.mtntcocoa.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaDeliveryPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaSummary
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentDispatchMtntSummaryBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaMtntLotSummaryFragment : BaseFragment(), ItemRemoveListener {

    private var dispatchLotsList = arrayListOf<VegaCocoaDispatchLots>()
    private var deliveryList = arrayListOf<VegaCocoaDispatchWB>()
    override val layoutResourceId: Int = R.layout.fragment_dispatch_mtnt_summary
    private lateinit var binding: FragmentDispatchMtntSummaryBinding
    private var callBack: CallBack? = null
    private var summaryObj: VegaCocoaSummary? = null
    private val vm: VegaCocoaMtntViewModel by viewModel()
    private var isSplit = false
    private var isBinFormation: Boolean = false
    private var isBinformFlag: Boolean = false

    companion object {
        fun newInstance(data: VegaCocoaSummary) = VegaMtntLotSummaryFragment().putArgs {
            putParcelable("summaryData", data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentDispatchMtntSummaryBinding.inflate(inflater)

        initExtra()
        initUi()
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
        vm.getConfigItems(UserRoles.MTNT.role)
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/VegaMtntLotSummaryFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    private fun initUi() {
        binding.tvTruckValue.text = summaryObj?.purchaseOrder?.vehicleNumber
        binding.tvRemarkValue.text = summaryObj?.purchaseOrder?.remarks
        binding.tvDestValue.text =
            summaryObj?.purchaseOrder?.storageLocationCode.plus("-")
                .plus(summaryObj?.purchaseOrder?.plantName)
        val times = summaryObj?.purchaseOrder?.erdat?.split('(', ')')
        binding.tvDateValue.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }

        binding.tvStoNoValue.text =
            summaryObj?.purchaseOrder?.purchaseDocNum.plus("-").plus(summaryObj?.purchaseOrder?.purchaseDocDesc)
        binding.tvMaterialName.text = summaryObj?.purchaseOrder?.materialName
        binding.tvStoWeightValue.text =
            summaryObj?.purchaseOrder?.netWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                .plus(summaryObj?.purchaseOrder?.unitsOfMeasure)
        binding.btProceed.setOnClickListener {
            if (isBinFormation) {
                if (dispatchLotsList.size == 1) {
                    dispatchLotsList.get(0).let {
                        if (it.weight!!.toDouble() > it.editedWeight!!.toDouble()) {
                            if (validate(binding.etBatchNo.text.toString())) showConformationDialog()
                            else showSnack(getString(R.string.batch_no_empty))
                        } else {
                            showConformationDialog()
                        }
                    }
                } else if (dispatchLotsList.size > 1) {
                    if (validate(binding.etBatchNo.text.toString())) showConformationDialog()
                    else showSnack(getString(R.string.batch_no_empty))
                } else {
                    showConformationDialog()
                }
            }
            else
            showConformationDialog()
            }
        val linearLayoutManager = activity?.let { LinearLayoutManager(it) }
        linearLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        binding.rvLots.layoutManager = linearLayoutManager
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        setUpAdapter()

    }

    private fun setUpAdapter() {
        val list = summaryObj?.lots as ArrayList
        dispatchLotsList = list
        val adapter =
            VegaCocoaMtntLotAdapter(list, listener = this, isThirdPartyMaterial = summaryObj?.isThirdParty ?: false)
        binding.rvLots.adapter = adapter
    }

    private fun postDelivery() {
        var batchNumber = ""

        if(dispatchLotsList.size==1) {
            dispatchLotsList.get(0).let {
                if (it.weight!!.toDouble() > it.editedWeight!!.toDouble()) {
                    batchNumber=binding.etBatchNo.text.toString().toUpperCase()
                }

            }
        } else if (dispatchLotsList.size > 1) {
            batchNumber = binding.etBatchNo.text.toString().toUpperCase()
        } else {

        }
        vm.postDeliveryDetail(
            VegaCocoaDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                batchNumber,
                isSplit,
                deliveryList,
                binformFlag = isBinformFlag
            )
        )
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    prepareDeliveryList()
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
                vm.updateSuccessStatus(
                    summaryObj?.purchaseOrder ?: VegaCocoaDispatchWB(),
                    true,
                    4,
                    response.data?.data?.msg ?: "",
                    summaryObj?.lots ?: ArrayList()
                )
                moveToSuccessPage(response.data?.data?.delivery)
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
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun prepareDeliveryList() {
        deliveryList.clear()
        dispatchLotsList.forEach { item ->
            val vegaCocoaDispatchWB = summaryObj?.purchaseOrder?.copy()
            vegaCocoaDispatchWB?.batchNumber = item.batchNumber
            vegaCocoaDispatchWB?.netWeight = item.editedWeight
            vegaCocoaDispatchWB?.purchaseDocNum = summaryObj?.purchaseOrder?.purchaseDocNum
            vegaCocoaDispatchWB?.purchaseDocDesc = summaryObj?.purchaseOrder?.purchaseDocDesc
            vegaCocoaDispatchWB?.unitsOfMeasure = item.unitOfMeasure
            vegaCocoaDispatchWB?.storageLocationCode = item.storageLocationCode
            vegaCocoaDispatchWB?.recStorageLocationCode = item.storageLocationCode
            vegaCocoaDispatchWB?.materialCode = item.materialCode
            vegaCocoaDispatchWB?.materialName = item.materialName
            // vegaCocoaDispatchWB?.transportVendorID = item.vendor?:""
            vegaCocoaDispatchWB?.turnAroundTime = summaryObj?.duration ?: "0"
            vegaCocoaDispatchWB?.deliveryItem = summaryObj?.purchaseOrder?.deliveryItem ?: ""
            deliveryList.add(vegaCocoaDispatchWB ?: VegaCocoaDispatchWB())
        }
    }

    private fun showUpdateRemarkDialog() {
        showDialog(
            getString(R.string.update_remark_title),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    summaryObj?.remark = remark
                    binding.tvRemarkValue.text = remark
                    vm.updateRemarks(remark, true, summaryObj?.purchaseOrder?.weighBridgeId ?: "")
                }
            },
            true, summaryObj?.purchaseOrder?.remarks ?: ""
        )
    }

    override fun itemRemoved(item: VegaCocoaDispatchLots) {
        activity?.onBackPressed()
    }
    private fun validate(batchNo: String): Boolean {
        return !batchNo.isEmpty()
    }
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_SELECTION_MULTI_WEIGHBRIDGE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            isBinFormation = true

                            if (dispatchLotsList.size == 1) {
                                dispatchLotsList.get(0).let {
                                    if (it.weight!!.toDouble() > it.editedWeight!!.toDouble()) {
                                        binding.llBatchNo.visible()
                                        isSplit = true
                                        isBinformFlag = true
                                    } else {
                                        binding.llBatchNo.gone()
                                        isSplit = false
                                        isBinformFlag = false
                                    }
                                }
                            } else if (dispatchLotsList.size > 1) {
                                isBinformFlag = true
                                isSplit = false
                                binding.llBatchNo.visible()
                            } else {
                                isSplit = false
                                isBinformFlag = false
                                binding.llBatchNo.gone()
                            }
                        }
                        it.applicable?.contains("N")!! -> isBinFormation = false
                    }
                }

            }
                }
    }

}
