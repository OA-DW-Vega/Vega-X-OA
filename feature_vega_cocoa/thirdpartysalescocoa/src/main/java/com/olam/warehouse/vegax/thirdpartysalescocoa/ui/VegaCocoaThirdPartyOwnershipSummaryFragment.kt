package com.olam.warehouse.vegax.thirdpartysalescoffee.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartycocoa.R
import com.olam.warehouse.vegax.thirdpartycocoa.databinding.FragmentCocoaThirdPartyOwnershipSummaryBinding
import com.olam.warehouse.vegax.thirdpartycocoa.databinding.ItemCocoaThirdPartyAddLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryDetail
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.SAME_TP
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.TP_TO_OLAM
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.TP_TO_TP
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaCocoaThirdPartyOwnershipSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_cocoa_third_party_ownership_summary
    private lateinit var binding: FragmentCocoaThirdPartyOwnershipSummaryBinding
    private var callBack: VegaCocoaThirdPartyReplaceFragmentCallback? = null
    private val vm: VegaCocoaThirdPartyViewModel by viewModel()
    private lateinit var model: VegaCoffeeThirdPartyRequestModel
    private lateinit var vegaCoffeeDeliveryPost: VegaCoffeeTPDeliveryPost
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private lateinit var vegaCoffeeThirdPartyModelWithLots: VegaCoffeeThirdPartyModelWithLots
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private var thirdPartyMaterials: List<VegaMaterial> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCocoaThirdPartyReplaceFragmentCallback
    }

    companion object {
        fun newInstance(model: VegaCoffeeThirdPartyRequestModel) =
            VegaCocoaThirdPartyOwnershipSummaryFragment().putArgs {
                putParcelable("model", model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCocoaThirdPartyOwnershipSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("thirdpartysalescoffee/ui/VegaCocoaThirdPartyOwnershipSummaryFragment")
            .title("Third party sales Cocoa")
            .with(tracker)
        initExtra()
        initUI()
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateOwnershipUI(it) })
        vm.deliveryWSPost.observe(viewLifecycleOwner, Observer { updateSamePartyUI(it) })
    }

    private fun initExtra() {
        model = arguments?.getParcelable<VegaCoffeeThirdPartyRequestModel>("model") as VegaCoffeeThirdPartyRequestModel
        fetchLocalDBData()
    }

    private fun initUI() {
        if (model.transferType.equals(SAME_TP, true)) {
            binding.tvMaterial.text = getString(R.string.vendor)
            binding.tvStage.visibility = View.GONE
            binding.tvStageValue.visibility = View.GONE

        } else if (model.transferType.equals(TP_TO_OLAM, true)) {
            binding.tvMaterial.text = getString(R.string.vendor)
            binding.tvStage.visibility = View.GONE
            binding.tvStageValue.visibility = View.GONE
            binding.tvGrnPrice.visibility = View.VISIBLE
            binding.tvTotalGrnPrice.visibility = View.VISIBLE
            binding.tvTotalGrnValue.visibility = View.VISIBLE
            binding.tvGrnValue.visibility = View.VISIBLE
            binding.tvGrnValue.text = model.grnPrice
        }
        vm.thirdPartyInfo.observe(viewLifecycleOwner, Observer { if (it != null) updateLocalDBData(it) })
        binding.btnProceed.setOnClickListener { showConformationDialog() }

        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it
        })
    }


    private fun fetchLocalDBData() {
        vm.getThirdPartyLocalData(model.vendorWithTransferType)
        vm.bagItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { getBagList(it) })
        vm.getBagItems()
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

    private fun updateLocalDBData(data: VegaCoffeeThirdPartyModelWithLots) {
        vegaCoffeeThirdPartyModelWithLots = data
        binding.tvMaterialValue.text = data.model.fromVendorCode.plus("-").plus(data.model.fromVendorName)
        binding.tvStageValue.text = data.model.toVendorCode.plus("-").plus(data.model.toVendorName)
        binding.tvProceesValue.text = data.model.createDate
        binding.tvBomValue.text = data.model.materialName
        dispatchLotsList.addAll(data.lots!!)
        vm.lotList = dispatchLotsList as ArrayList<VegaCocoaDispatchLots>
        setUpAdapter(vm.lotList)
        if (model.transferType.equals(TP_TO_OLAM, true))
            binding.tvTotalGrnValue.text =
                vm.getTotalGrnPriceValue(data.model.grnPrice ?: "0", vm.getTotalTransferWeight())
    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        binding.rvLotList.setUpAdapter(
            list,
            R.layout.item_cocoa_third_party_add_lot,
            ItemCocoaThirdPartyAddLotBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocationValue1.text = item.storageLocationCode
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                        ?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                bindItem.tvEditedWeight.text = editedWeight.plus(" ")
                bindItem.tvEditedWeight.visibility = View.VISIBLE
                bindItem.etWeight.visibility = View.GONE
                bindItem.cbSelectAll.visibility = View.GONE
                bindItem.tvSelectAll.visibility = View.GONE
                bindItem.ivClose.setImageDrawable(bindItem.ivClose.context.getDrawable(R.drawable.ic_cocoa_tp_edit_gray))
                bindItem.ivClose.setOnClickListener { itemRemoved(item) }
            })
    }

    private fun postDelivery() {
        vegaCoffeeDeliveryPost = VegaCoffeeTPDeliveryPost(
            getCurrentKey(),
            getPlantDetails(),
            "",
            model.grnPrice.toString(),
            binding.tvTotalGrnValue.text.toString(),
            prepareDeliveryList()
        )
        if (model.transferType.equals(TP_TO_TP, true))
            vm.postDeliveryDetail(vegaCoffeeDeliveryPost, model.transferType)
        else
            vm.postDeliveryDetailForWeighScale(vegaCoffeeDeliveryPost, model.transferType)

    }


    private fun prepareDeliveryList(): List<VegaCoffeeTPDeliveryDetail> {
        val list = ArrayList<VegaCoffeeTPDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaCoffeeTPDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode }
            deliveryDetail.batchNumber = item.batchNumber
            if (model.transferType.equals(TP_TO_OLAM, true)) {
                val thMat = thirdPartyMaterials.filter { item.materialCode.contains(it.materialCode) }
                if (thMat.size > 0 && thMat[0].thirdPartyFlag.equals("X", true))
                    deliveryDetail.materialCode = thMat[0].thirdPartyMaterialCode
                else
                    deliveryDetail.materialCode = item.materialCode
            } else
                deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.netWeight = item.editedWeight
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.year = year.toString()
            deliveryDetail.toVendorCode = model.toVendorCode ?: ""
            deliveryDetail.weighBridgeId = item.weighBridgeId
            deliveryDetail.fromVendorCode = model.fromVendorCode
            deliveryDetail.grnPrice = model.grnPrice ?: ""

            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            bagItems.forEach { item1 ->
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }

            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems
            } else {
                deliveryDetail.huno2 = item.unitOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
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

    private fun updateOwnershipUI(response: Resource<GenericReqAndResp<VegaDeliveryPostResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    val batch = response.data?.data?.batchNumber
                    moveToSuccessPage(batch, response.data?.message.toString())
                    vm.updateSyncStatus(vegaCoffeeThirdPartyModelWithLots)
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun updateSamePartyUI(response: Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    moveToSuccessPage(response.data?.message, response.data?.message.toString())
                    vm.updateSyncStatus(vegaCoffeeThirdPartyModelWithLots)
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun moveToSuccessPage(deliveryId: String?, message: String) {
        val grnMsg = message.split("with Doc")
        val otherMsg = message.split("successfully")
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch)
        )
        if (TP_TO_TP.equals(model.transferType, true)) {
            intent.putExtra(AppUtils.PRINT_ENABLE, true)
            intent.putExtra("fromcoffee", true)
            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard())
        }
        if (grnMsg.size > 1)
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.tp_owner_success_grn, grnMsg[1].replace("\n\n", "\n"))
            )
        else {
            if (otherMsg.size > 1)
                intent.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(
                        R.string.tp_owner_success_material,
                        otherMsg[0].replace("1.", "").replace("Material Document", "", true).replace(
                            "posted",
                            ""
                        ).replace("created", "")
                    )
                )
            else
                intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.tp_owner_success, deliveryId))
        }

        startActivity(intent)
        requireActivity().finish()
    }

    private fun prepareLotCard(): ArrayList<VegaCoffeeSalesLots> {
        val lotList = arrayListOf<VegaCoffeeSalesLots>()
        vm.lotList.forEach {
            val lot = VegaCoffeeSalesLots()
            lot.batchNumber = it.batchNumber
            lot.editedWeight = it.editedWeight
            lot.unitOfMeasure = it.unitOfMeasure
            lot.materialName = it.materialName
            lot.materialCode = it.materialCode
            lot.storageLocationCode = it.storageLocationCode
            lotList.add(lot)
        }
        return lotList
    }


    private fun itemRemoved(item: VegaCocoaDispatchLots) {
        activity?.onBackPressed()
    }
}
