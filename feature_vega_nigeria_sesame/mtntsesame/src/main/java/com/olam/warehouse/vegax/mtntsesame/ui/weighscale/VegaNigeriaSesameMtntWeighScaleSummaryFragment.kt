package com.olam.warehouse.vegax.mtntsesame.ui.weighscale

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
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.mtntsesame.R
import com.olam.warehouse.vegax.mtntsesame.data.domain.model.*
import com.olam.warehouse.vegax.mtntsesame.databinding.FragmentNigeriaSesameMtntWsSummaryBinding
import com.olam.warehouse.vegax.mtntsesame.databinding.ItemNigeriaSesameLotSummaryBinding
import com.olam.warehouse.vegax.mtntsesame.databinding.ItemNigeriaSesameMaterialLayoutBinding
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameMtntViewModel
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntsesame.utils.JSON_PROCESS_TYPE_LIST
import com.olam.warehouse.vegax.mtntsesame.utils.convertKgToMT
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaNigeriaSesameMtntWeighScaleSummaryFragment : BaseFragment(),
    VegaNigeriaSesameMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_nigeria_sesame_mtnt_ws_summary
    private lateinit var binding: FragmentNigeriaSesameMtntWsSummaryBinding
    private var callBack: VegaNigeriaSesameReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaNigeriaSesameMtntViewModel by viewModel()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private lateinit var vegaCoffeeDeliveryPost: VegaNigeriaSesameMtntDeliveryPost
    private lateinit var vegaSesameDeliveryPost: VegaNigeriaSesameMtntMergedDeliveryPost
    private var vegaCocoaMtntWithLots: VegaCocoaMtntWithLots? = null
    var isPostCreated: Boolean = false
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null
    private var grnProcessType :String? =""
    private var jsonData = mutableListOf<String>()
    private var deliveryDetails = ArrayList<VegaNigeriaSesameMtntDispatchLotsMerge>()
    private var model: VegaEcuadorDispatch = VegaEcuadorDispatch()

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaNigeriaSesameMtntWeighScaleSummaryFragment().putArgs {
            putParcelable("summaryData", data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaSesameReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaSesameMtntWsSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        vm.wsdeliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {
        context?.let {
            UIUtils.getActionBtnChangedView(binding.tvTitle, it, false)
            UIUtils.getActionBtnChangedView(binding.btProceed, it, true)
        }
        vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
        vm.weighBridgeWithLotsSource.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchWh = it.dispatch
                vm.lotList.clear()
                vm.lotList.addAll(it.lineItems)
                vm.materialModelList.clear()
                vm.materialModelList.addAll(it.materialList)
                vegaCocoaMtntWithLots = it
                updateLocalDbData(it)
            }
        })

        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer  {
            thirdPartyMaterials = it
        })
        vm.getThirdPartyMaterials()

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        binding.header.tvTruckNoValue.text = summaryObj?.vehicleNumber
        binding.tvRemarkValue.text = summaryObj?.remarks
        binding.header.tvDestValue.text =
            summaryObj?.recPlantId.plus("-").plus(summaryObj?.plantName)
        binding.header.tvDateValue.text = summaryObj?.erdat
        binding.header.tvStoNumberValue.text =
            summaryObj?.purchaseDocNum.plus("-").plus(summaryObj?.purchaseDocDesc)
        binding.header.tvContactValue.text = summaryObj?.driverPhoneNumber
        binding.header.tvDriverNameValue.text = summaryObj?.driverName

        binding.btProceed.setOnClickListener { showConformationDialog() }
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        binding.tvRemarkValue.text = summaryObj?.remarks
    }

    private fun updateLocalDbData(weighBridge: VegaCocoaMtntWithLots) {
        dispatchLotsList.clear()
        if (weighBridge.lineItems.size == 1) {
            binding.tvText.visibility = View.GONE
        } else
            binding.tvText.visibility = View.VISIBLE
        setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
        calculateAndUpdateWeightToDispatch()
        vm.bagItems.observe(viewLifecycleOwner, Observer { getBagList(it) })
        vm.getBagItems()
    }


    private fun setUpMaterialAdapter() {
        binding.header.rvMaterialList.setUpAdapter(vm.materialModelList,
            R.layout.item_nigeria_sesame_material_layout,
            ItemNigeriaSesameMaterialLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvStoWeightValue.text =
                    it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus("KG")
                bindItem.tvDispatchWeightValue.text = it.dispatchWeight.plus(" KG")
            })
    }

    private fun setUpAdapter(list: List<VegaCocoaDispatchLots>) {
        val lots = list as MutableList
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_nigeria_sesame_lot_summary,
            ItemNigeriaSesameLotSummaryBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocation.text = item.storageLocationCode
                val weight = item.weight?.toDouble()
                val enteredWeight = item.editedWeight?.toDouble()
                val totalLoss = weight?.minus(enteredWeight!!)
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("KG")
                bindItem.tvGradeValue.text = item.materialName
                bindItem.tvEditedWeight.text =
                    item.editedWeight?.toDouble()?.formatThreeDigits().toString().plus(" ")
                        .plus("KG")

                if (item.isEndLot == true) {
                    bindItem.tvTotalWeightLoss.text =
                        totalLoss?.formatThreeDigits().plus(" ").plus("KG")
                } else
                    bindItem.tvTotalWeightLoss.text = "NA"

                bindItem.tvUnit.visibility = View.GONE
                bindItem.tvEditedWeight.visibility = View.VISIBLE
                bindItem.ivClose.setImageDrawable(bindItem.ivClose.context.getDrawable(R.drawable.ic_coffee_edit_gray))
                bindItem.cbSelectAll.visibility = View.GONE
                bindItem.tvSelectAll.visibility = View.GONE
                bindItem.cbEndLot.visibility = View.GONE
                bindItem.tvEndLot.visibility = View.GONE
                bindItem.etWeight.visibility = View.GONE
                bindItem.ivClose.setOnClickListener { itemRemoved(item) }
            })
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        var processTypeList = mutableListOf<VegaNigeriaSesameMtntProcessType>()
        jsonData.forEach {
            if (it.contains(JSON_PROCESS_TYPE_LIST)) {
                val processType = gson.fromJson(it, VegaNigeriaSesameMtntProcessTypeModel::class.java)
                grnProcessType = processType.PROCESS_TYPE_LIST[0].MTNT
            }
        }

    }

    private fun generateBatchNumber():String {
        var productTypeCode = ""

        thirdPartyMaterials?.forEach { item ->
            if (item.materialName.toString().contains(dispatchLotsList[0].materialName.toString(), true)) {
                productTypeCode = item.typeCode.toString()
            }
        }
        var year = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(1)
        return dispatchLotsList[0].plantId?.takeLast(2).plus(dispatchLotsList[0].storageLocationCode?.takeLast(1))
            .plus(year).plus(grnProcessType).plus(productTypeCode.takeLast(1))
    }

    private fun postDelivery() {
        val count = PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
        if (count != 0 && DateUtils.isFirstDaysOfMonth(requireContext(), count)) {
            UIUtils.showPostingDateDialog(requireContext(), count, object : UIUtils.DialogClick {
                override fun onPositive(date: String) {
                    var postingDate =
                        DateUtils.oneFormatToOtherFormat(date, "dd-MMM-yyyy", "yyyy-MM-dd'T'HH:mm")
                    var batchId = generateBatchNumber()
                    vegaSesameDeliveryPost = VegaNigeriaSesameMtntMergedDeliveryPost(
                        getCurrentKey(),
                        getPlantDetails(),
                        false,
                        false,
                        false,
                        false,
                        summaryObj?.vehicleNumber,
                        summaryObj?.driverPhoneNumber,
                        summaryObj?.driverName,
                        prepareMergedDeliveryList(postingDate)
                    )
                    vm.postWeighScaleDeliveryDetails123(vegaSesameDeliveryPost)
                }
            })
        }else {
            var batchId = generateBatchNumber()
            vegaSesameDeliveryPost = VegaNigeriaSesameMtntMergedDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                false,
                false,
                false,
                false,
                summaryObj?.vehicleNumber,
                summaryObj?.driverPhoneNumber,
                summaryObj?.driverName,
                prepareMergedDeliveryList("")
            )
            vm.postWeighScaleDeliveryDetails123(vegaSesameDeliveryPost)
        }

    }
    private fun prepareMergedDeliveryList(postingDate:String) : List<VegaNigeriaSesameMtntDispatchLotsMerge> {
        var list = ArrayList<VegaNigeriaSesameMtntDispatchLotsMerge>()
        var lotList = ArrayList<VegaEcuadorDispatchLots>()

        val deliveryDetail = VegaNigeriaSesameMtntDispatchLotsMerge()
        deliveryDetail.batchNumber = generateBatchNumber()
        deliveryDetail.delivery = false
        deliveryDetail.deliveryId = ""
        deliveryDetail.mergeStatus = ""
        deliveryDetail.message = ""
        deliveryDetail.pgi = false
        deliveryDetail.picking = false
        for(item in dispatchLotsList){

            var lot = VegaEcuadorDispatchLots()
            lot.batchNumber = item.batchNumber
            lot.materialCode = item.materialCode
            lot.materialName = item.materialName
            lot.noOfBags = item.noOfBags
            lot.storageLocationCode = item.storageLocationCode
            lot.plantId = item.plantId
            lot.plantName = item.plantName
            lot.unitsOfMeasure = item.unitOfMeasure
            lot.recPlantId = summaryObj?.recPlantId
            lot.recStorageLocationCode = summaryObj?.recStorageLocationCode
            lot.netWeight = convertKgToMT(item.editedWeight, item.unitOfMeasure.toString())
            lot.startTime = summaryObj?.startTime.toString()
            lot.endTime = summaryObj?.endTime.toString()
            lot.turnAroundTime = summaryObj?.turnAroundTime.toString()
            lot.remarks = summaryObj?.remarks.toString()
            lot.editedWeight = convertKgToMT(item.editedWeight, item.unitOfMeasure.toString())
            lot.wbTempId = item.weighBridgeId
            lot.grossWeight = convertKgToMT(item.weight, item.unitOfMeasure.toString())
            lot.endLotFlag = item.isEndLot!!
            lot.postingDate=postingDate

            vm.materialModelList.forEach {
                if (item.materialCode == it.materialCode) {
                    lot.purchaseDocNum = it.purchaseOrderNum
                    lot.purchaseDocDesc = it.purchaseOrderDesc
                }
            }
            lotList.add(lot)
        }
        deliveryDetail.lots = lotList
        list.add(deliveryDetail)
        return list
    }

    private fun calculateAndUpdateWeightToDispatch() {
        val materialWeightMap = HashMap<String, String>()
        dispatchLotsList.forEach {
            val data = materialWeightMap[it.materialCode]
            val editWeight =
                if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()
            if (data != null) {
                val sum = data.toDouble().plus(editWeight!!)
                materialWeightMap[it.materialCode] = sum.toString()
            } else materialWeightMap[it.materialCode] = editWeight.toString()
        }
        vm.materialModelList.forEach { it.dispatchWeight = materialWeightMap[it.materialCode] }
        setUpMaterialAdapter()
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

    private fun showConformationBackNav() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_back)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.updateSyncStatus(vegaCocoaMtntWithLots!!)
                    activity?.finish()
                },
                { dismiss() })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaNigeriaSesameMergedDeliveryPostResponse>>) {

        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let {
                    if (true) {
                        prepareSuccessData(
                            response.data?.data?.message ?: "",
                            true,
                            response.data?.message.toString()
                        )
                    } else {
                        model.binFormation = response.data?.data?.binFormation!!
                        model.delivery = response.data?.data?.delivery!!
                        model.pgi = response.data?.data?.pgi!!
                        model.picking = response.data?.data?.picking!!
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
        moveToSuccessPage(deliveryId)
    }

    private fun moveToSuccessPage(deliveryId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        vm.deleteBagDetails()
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

    private fun prepareSuccessMessage(data: List<VegaNigeriaSesameMergedDeliveryPostResponse>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                message = ""
            }
            return message
        }
        return ""
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
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

    fun backNav() {
        showConformationBackNav()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
