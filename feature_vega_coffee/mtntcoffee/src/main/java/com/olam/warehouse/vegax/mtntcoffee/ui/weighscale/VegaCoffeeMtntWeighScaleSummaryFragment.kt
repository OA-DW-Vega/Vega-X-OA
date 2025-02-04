package com.olam.warehouse.vegax.mtntcoffee.ui.weighscale

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.mtntcoffee.R
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeDeliveryPost
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntcoffee.databinding.FragmentCoffeeMtntWsSummaryBinding
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeMtntViewModel
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntcoffee.utils.WEIGHSCALE
import kotlinx.android.synthetic.main.item_coffee_lot_summary.view.*
import kotlinx.android.synthetic.main.item_material_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaCoffeeMtntWeighScaleSummaryFragment : BaseFragment(), VegaCoffeeMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_coffee_mtnt_ws_summary
    private lateinit var binding: FragmentCoffeeMtntWsSummaryBinding
    private var callBack: VegaCoffeeReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaCoffeeMtntViewModel by viewModel()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private lateinit var vegaCoffeeDeliveryPost: VegaCoffeeDeliveryPost
    private var vegaCocoaMtntWithLots: VegaCocoaMtntWithLots? = null
    var isPostCreated: Boolean = false

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaCoffeeMtntWeighScaleSummaryFragment().putArgs {
            putParcelable("summaryData", data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCoffeeMtntWsSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        vm.weighScaleDeliveryPost.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {

        vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
        vm.weighBridgeWithLotsSource.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
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
        binding.header.tvTruckNoValue.text = summaryObj?.vehicleNumber
        binding.tvRemarkValue.text = summaryObj?.remarks
        binding.header.tvDestValue.text =
            summaryObj?.storageLocationCode.plus("-").plus(summaryObj?.plantName)
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
        setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
        calculateAndUpdateWeightToDispatch()
        vm.bagItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { getBagList(it) })
        vm.getBagItems()
    }


    private fun setUpMaterialAdapter() {
        binding.header.rvMaterialList.setUp(
            vm.materialModelList,
            R.layout.item_material_layout,
            { it, pos ->
                tvMaterialName.text = it.materialName
                tvStoWeightValue.text =
                    it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(it.uom)
                tvDispatchWeightValue.text = it.dispatchWeight.plus(" KG")
            })
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
             summaryObj?.vehicleNumber,
            summaryObj?.driverName,
            summaryObj?.driverPhoneNumber,
            prepareDeliveryList(), WEIGHSCALE
        )
        vm.postWeighScaleDeliveryDetails(vegaCoffeeDeliveryPost)
    }


    private fun prepareDeliveryList(): List<VegaCoffeeMtntDeliveryDetail> {
        val list = ArrayList<VegaCoffeeMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaCoffeeMtntDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.netWeight = item.editedWeight
            deliveryDetail.vehicleNumber = summaryObj?.vehicleNumber
            deliveryDetail.endLotFlag = item.isEndLot ?: false
            deliveryDetail.createdDate = summaryObj?.startTime
            vm.materialModelList.forEach {
                if (item.materialCode == it.materialCode) {
                    deliveryDetail.purchaseDocNum = it.purchaseOrderNum
                    deliveryDetail.purchaseDocDesc = it.purchaseOrderDesc
                }
            }
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.startTime = summaryObj?.startTime
            deliveryDetail.endTime = summaryObj?.endTime
            deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
            deliveryDetail.weighBridgeId = item.weighScaleWbId ?: ""
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.isPgiFlag = item.isPgiFlag
            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.year = year.toString()
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            /*var startTime = ""
            var endTime = ""*/
            bagItems.forEach { item1 ->
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                /*if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
                if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()*/
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
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
            list.add(deliveryDetail)
        }
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
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    private fun showConformationBackNav() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_back)
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
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

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCoffeeMtntDeliveryDetail>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                isPostCreated = true
                if (response.data?.success == true) {
                    var docNo = listOf<String>()
                    docNo =
                        response.data?.data?.filter { !it.documentNum.isNullOrEmpty() }
                            ?.map { it.documentNum.toString() }
                            ?: listOf()
                    moveToSuccessPage(prepareSuccessMessage(response.data?.data), docNo)
                    vm.updateSyncStatus(vegaCocoaMtntWithLots!!)
                } else {
                    UIUtils.showErrorDialog(requireContext(), response.data?.message ?: "")
                    val success = response.data?.data ?: ArrayList()
                    for (item in success) {
                        dispatchLotsList.single { it.batchNumber == item.batchNumber }.apply {
                            weighScaleWbId = item.weighBridgeId
                            pickingFlag = item.pickingFlag
                            deliveryFlag = item.deliveryFlag
                            deliveryItem = item.deliveryItem
                            delivery = item.delivery
                            isPgiFlag = item.isPgiFlag
                            storageLossFlag = item.storageLossFlag
                        }
                    }
                    vm.lotList.clear()
                    vm.dispatchWh = summaryObj!!
                    vm.lotList.addAll(dispatchLotsList)
                    vm.saveWeighBridgeAndLotDetails()
                    vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareSuccessMessage(data: List<VegaCoffeeMtntDeliveryDetail>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                message =
                    "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ").plus(message.plus(it.msgList[0]).plus("\n"))
            }
            return message
        }
        return ""
    }

    private fun moveToSuccessPage(deliveryId: String?, docNo: List<String>) {
        val documentNo = if (docNo.size > 0) docNo.toString().replace("[", "").replace("]", "") else ""
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch_offline)
        )
        /*intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))*/
        if (!documentNo.isEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.mtnr_ws_success, deliveryId).plus(
                    getString(
                        R.string.mtnr_ws_success_docu_no,
                        documentNo
                    )
                )
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.mtnr_ws_success, deliveryId))
        startActivity(intent)
        requireActivity().finish()
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
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }
}
