package com.olam.warehouse.vegax.dispatchecuador.ui.weighbridge

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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.model.*
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatchecuador.R
import com.olam.warehouse.vegax.dispatchecuador.databinding.FragmentEcuadorDispatchSummaryBinding
import com.olam.warehouse.vegax.dispatchecuador.databinding.ItemLotDispatchSummaryBinding
import com.olam.warehouse.vegax.dispatchecuador.databinding.ItemVegaEcuadorDispatchMaterialDetailsBinding
import com.olam.warehouse.vegax.dispatchecuador.ui.*
import com.olam.warehouse.vegax.dispatchecuador.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.dispatchecuador.utils.weighBridgeId
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaEcuadorMtntSummaryFragment : BaseFragment() {
    private var dispatchLotsList = arrayListOf<VegaEcuadorDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_ecuador_dispatch_summary
    private lateinit var binding: FragmentEcuadorDispatchSummaryBinding
    private val vm: VegaEcuadorDispatchViewModel by viewModel()
    private var model: VegaEcuadorDispatch = VegaEcuadorDispatch()
    private var callBack: CallBack? = null

    private var materialCodeList = arrayListOf<String>()
    private var mergedLotsMap = mutableMapOf<Int, ArrayList<VegaEcuadorWbDispatchLots>>()
    private var deliveryDetails = ArrayList<VegaEcuadorWbDispatchLotsMerge>()
    private var isLotEditable: Boolean = true
    private var dispatchWbLotsList = arrayListOf<VegaEcuadorWbDispatchLots>()

    interface CallBack {
        fun popAllFragmentsFromBackStack()
    }

    companion object {
        fun newInstance(model: VegaEcuadorDispatch, dispatchLotList: ArrayList<VegaEcuadorDispatchLots>) =
            VegaEcuadorMtntSummaryFragment().putArgs {
                putParcelableArrayList(DISPATCH_DATA_LIST, dispatchLotList)
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentEcuadorDispatchSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        vm.deliveryWbPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        dispatchLotsList = arguments?.getParcelableArrayList<VegaEcuadorDispatchLots>(DISPATCH_DATA_LIST)!!
        dispatchWbLotsList = prepareWbLotDetails(dispatchLotsList)
        model = arguments?.getParcelable(MODEL_BUNDLE)!!
        setupAdapter()
    }

    private fun prepareWbLotDetails(dispatchLotsList: ArrayList<VegaEcuadorDispatchLots>): ArrayList<VegaEcuadorWbDispatchLots> {
         var list = ArrayList<VegaEcuadorWbDispatchLots>()
        dispatchLotsList.forEach {
            var lot = VegaEcuadorWbDispatchLots()
            lot.weighBridgeId= weighBridgeId
            lot.batchNumber = it.batchNumber
            lot.materialCode = it.materialCode
            lot.materialName = it.materialName
            lot.plantId = it.plantId
            lot.plantName = it.plantName
            lot.storageLocationCode = it.storageLocationCode
            lot.netWeight = it.netWeight
            lot.editedWeight = it.editedWeight
            lot.isAdded = it.isAdded
            lot.pairId = it.pairId
            lot.binBatch = it.binBatch
            lot.isChecked = it.isChecked
            lot.processOrderNo = it.processOrderNo
            lot.deliveryItem = it.deliveryItem
            lot.xchpf = it.xchpf
            lot.noOfBags = it.noOfBags
            lot.slPostion = it.slPostion
            lot.isProgress = it.isProgress
            lot.remarks = it.remarks
            lot.turnAroundTime = it.turnAroundTime
            lot.startTime = it.startTime
            lot.endTime = it.endTime
            lot.purchaseDocNum = it.purchaseDocNum
            lot.purchaseDocDesc = it.purchaseDocDesc
            lot.deliveryStatus = it.deliveryStatus
            lot.encodedImageContent = it.encodedImageContent
            lot.imageUploadMsg = it.imageUploadMsg
            lot.unitsOfMeasure = it.unitsOfMeasure
            lot.recPlantId = it.recPlantId
            lot.recStorageLocationCode = it.recStorageLocationCode
            lot.isLowerWeight = it.isLowerWeight
            lot.delivery = it.delivery
            lot.grossWeight = it.grossWeight
            lot.endLotFlag = it.endLotFlag
            lot.postingDate= it.postingDate
            list.add(lot)
        }
        return list
    }

    private fun initUI() {
        binding.tvRemarkValue.text = model.remarks
        binding.tvDestValue.text = model.storageLocationCode.plus("-").plus(model.plantName)
        val times = model.erdat?.split('(', ')')
        binding.tvDateValue.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }

        binding.tvStoNoValue.text = model.purchaseDocNum
        binding.btProceed.setOnClickListener { showConfirmationDialog() }
        val linearLayoutManager = activity?.let { LinearLayoutManager(it) }
        linearLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        binding.rvLotList.layoutManager = linearLayoutManager
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        //setUpLotAdapter()
        setUpMaterialAdapter(model.purchaseOrders as MutableList<VegaEcuadorDispatchPurchaseOrders>)
    }

    private fun setUpMaterialAdapter(list: MutableList<VegaEcuadorDispatchPurchaseOrders>) {
        materialCodeList.clear()
        materialCodeList.addAll(list.map { it.materialCode }.distinct())
        binding.rvMaterial.setUpAdapter(
            list,
            R.layout.item_vega_ecuador_dispatch_material_details,
            ItemVegaEcuadorDispatchMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialValue.text = it.materialName
                bindItem.tvStoWeightValue.text =
                    it.menge?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(it.meins)
            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun setupAdapter() {
        binding.rvLotList.addItemDecoration(DividerSpaceItemDecorationWb(16, dispatchWbLotsList))
        binding.rvLotList.setUpAdapter(
            dispatchWbLotsList,
            R.layout.item_lot_dispatch_summary,
            ItemLotDispatchSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.netWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
                it.editedWeight?.toDoubleOrNull()?.let { double ->
                    bindItem.tvWeightToDispatch.text =
                        double.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
                }
                bindItem.ivEdit.visible()
                bindItem.ivEdit.setOnClickListener {
                    if (isLotEditable) callBack?.popAllFragmentsFromBackStack()
                }

            },
            itemClick = {

            })
    }

    private fun showConfirmationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    if (deliveryDetails.isEmpty())
                        prepareDeliveryList()
                    else {
                        if (AppUtils.isOnline())
                            vm.postWbDeliveryDetail(
                                VegaEcuadorWbDeliveryPost(
                                    getCurrentKey(),
                                    getPlantDetails(),
                                    model.binFormation,
                                    model.delivery,
                                    model.pgi,
                                    model.picking,
                                    deliveryDetails,
                                    "WEIGHBRIDGE"
                                )
                            )
                    }
                },
                { dismiss() })
        }
    }

    private fun showUpdateRemarkDialog() {
        showDialog(
            getString(R.string.update_remark_title),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    model.remarks = remark
                    binding.tvRemarkValue.text = remark
                }
            },
            true, model.remarks
        )
    }

    private fun prepareDeliveryList() {
        dispatchWbLotsList.forEach label@{
            it.purchaseDocNum = model.purchaseDocNum
            it.netWeight = it.editedWeight
            it.startTime = model.startTime
            it.endTime = model.endTime
            it.turnAroundTime = model.turnAroundTime
            it.remarks = model.remarks
            it.recPlantId = model.storageLocationCode
            model.purchaseOrders.forEach { item ->
                if (item.materialCode.equals(it.materialCode)) {
                    it.purchaseDocDesc = item.purchaseDocDesc.toString()
                    it.recStorageLocationCode = item.storageLocationCode
                }
            }
            if (it.pairId!! <= 0)
                return@label
            when (val mergedDispatchLotList = mergedLotsMap[it.pairId!!]) {
                null -> {
                    val list = ArrayList<VegaEcuadorWbDispatchLots>()
                    list.add(it)
                    mergedLotsMap[it.pairId!!] = list
                }
                else -> {
                    mergedDispatchLotList.add(it)
                    mergedLotsMap[it.pairId!!] = mergedDispatchLotList
                }
            }
        }
        if (AppUtils.isOnline()) createDeliveryDetails()
        else prepareSuccessData(model.wbTempId, false, "Data cached offline")
    }

    private fun createDeliveryDetails() {

        for ((key, dispatchWbLotsList) in mergedLotsMap) {
            val item = VegaEcuadorWbDispatchLotsMerge()

            item.lots = ArrayList<VegaEcuadorWbDispatchLots>()
            item.lots.addAll(dispatchWbLotsList)
            deliveryDetails.add(item)
        }

        val unpaired = dispatchWbLotsList.filter { it.pairId == 0 }
        unpaired.forEach {
            val item = VegaEcuadorWbDispatchLotsMerge()
            item.batchNumber = it.batchNumber

            val list = ArrayList<VegaEcuadorWbDispatchLots>(1)
            list.add(it)
            item.lots.addAll(list)
            deliveryDetails.add(item)
        }
        vm.postWbDeliveryDetail(
            VegaEcuadorWbDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                model.binFormation,
                model.delivery,
                model.pgi,
                model.picking,
                deliveryDetails,
                "WEIGHBRIDGE"
            )
        )
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let {
                    if (it.delivery && it.picking) {
                        println("nnnnnnnnn insidetrue")
                        prepareSuccessData(
                            response.data?.data?.message ?: "",
                            true,
                            response.data?.message.toString()
                        )
                    } else {
                        println("nnnnnnnnn insidefalse")
                        if (it.delivery || it.binFormation) {
                            isLotEditable = false
                            binding.ivEdit.gone()
                        }
                        model.binFormation = response.data?.data?.binFormation!!
                        model.delivery = response.data?.data?.delivery!!
                        model.pgi = response.data?.data?.pgi!!
                        model.picking = response.data?.data?.picking!!
                        /*deliveryDetails =
                            response.data?.data?.deliveryDetails as ArrayList<VegaEcuadorWbDispatchLotsMerge>*/
                        println("nnnnnnnnn $response.data?.data?.message")
                        showDispatchErrorDialog(
                            response.data?.data?.message ?: getString(R.string.dispatch_failed)
                        )
                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareSuccessData(deliveryId: String, syncStatus: Boolean, msg: String) {
        model.status = if (syncStatus) Status.MTNT_COMPLETED else Status.SYNC_PENDING
        model.wbTempId = if (!syncStatus) deliveryId else ""
        model.isSynced = syncStatus
        model.syncStatusMsg = msg
        model.deliveryId = deliveryId
        if (!syncStatus) {
            vm.saveDispatch(model)
            //vm.saveDispatchLotLineItems(dispatchLotsList)
        } else {
            vm.updateDeletedItem(model.wbTempId)
        }
        moveToSuccessPage(deliveryId)
    }

    private fun moveToSuccessPage(deliveryId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch)
            )
            intent.putExtra(AppUtils.SUB_TITLE, deliveryId)
        } else {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch_offline)
            )
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showDispatchErrorDialog(msg: String) {
        println("nnnnnnn insideerr")
        MaterialDialog(requireContext()).show {
            title(com.olam.warehouse.presentation.R.string.error)
            message(null, msg)
            cancelOnTouchOutside(false)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.retry),
                getString(R.string.cancel),
                {
                    vm.postWbDeliveryDetail(
                        VegaEcuadorWbDeliveryPost(
                            getCurrentKey(),
                            getPlantDetails(),
                            model.binFormation,
                            model.delivery,
                            model.pgi,
                            model.picking,
                            deliveryDetails,
                            "WEIGHBRIDGE"
                        )
                    )
                },
                { dismiss() })
        }
    }

    fun getLotEditableStatus(): Boolean {
        return isLotEditable
    }
}
