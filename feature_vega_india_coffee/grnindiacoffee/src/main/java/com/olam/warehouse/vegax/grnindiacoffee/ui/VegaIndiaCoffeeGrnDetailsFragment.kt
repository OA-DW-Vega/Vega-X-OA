package com.olam.warehouse.vegax.grnindiacoffee.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnindiacoffee.R
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.*
import com.olam.warehouse.vegax.grnindiacoffee.databinding.FragmentVegaIndiaCoffeeGrnDetailsBinding
import com.olam.warehouse.vegax.grnindiacoffee.utils.*
import kotlinx.android.synthetic.main.cutsom_dialog_bc_approve_india_coffe_layout.view.*
import kotlinx.android.synthetic.main.vega_india_coffee_grn_cutsom_dialog_layout.view.ivClose
import kotlinx.android.synthetic.main.vega_india_coffee_grn_cutsom_dialog_layout.view.llGrn
import kotlinx.android.synthetic.main.vega_india_coffee_grn_cutsom_dialog_layout.view.tvConfirmGrn
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaIndiaCoffeeGrnDetailsFragment : BaseFragment(), VegaSingleSelectCommonListener {
    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaIndiaCoffeeGrnViewModel by viewModel()
    private var weighBridgeId = VegaGrnWeighBridgeId()
    private var wbDetails = VegaGrnWeighBridgeId()
    private var copyWbDetails = VegaGrnWeighBridgeId()
    private var wbPostDetails = VegaQualityWBDetails()
    private var qualityQcPostList = arrayListOf<VegaQualityWBDetails>()
    private var currentMaterial: String = ""
    private var defaultStorageLocation: String = ""
    private var admixtureValue: String = ""
    private var qualitycallBack: QualityDetailsCallBack? = null
    private var approveQualityList = ArrayList<VegaIndiaCoffeeGRNQuality>()
    private var grnList = ArrayList<VegaIndiaCoffeeGrnPostData>()
    private var storageLocation: List<VegaCustomStLocation>? = null
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var filterpurchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var yearFilter = mutableListOf<String>()
    private var year = ""
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var procureType = ""
    private var qualitylist = arrayListOf<VegaQualityParams>()
    private var offlineDataList = mutableListOf<VegaGrnWeighBridgeId>()
    private var weighBridgeList = mutableListOf<VegaGrnWeighBridgeId>()
    private var weighBridgeUnFilteredList = mutableListOf<VegaGrnWeighBridgeId>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()

    private lateinit var binding: FragmentVegaIndiaCoffeeGrnDetailsBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_grn_details

    interface QualityDetailsCallBack {
        fun replaceQualityDetailsFragment(
            moveFrag: String,
            wbDetails: VegaGrnWeighBridgeId,
            approveQualityList: ArrayList<VegaIndiaCoffeeGRNQuality>
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        (menu.findItem(com.olam.warehouse.presentation.R.id.search)).isVisible = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeGrnDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnsesame/ui/details/VegaNigeriaSesameGrnDetailsFragment").title("GRN Ecuador")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId) = VegaIndiaCoffeeGrnDetailsFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        qualitycallBack = context as QualityDetailsCallBack
    }

    private fun initUI() {

        if (isOnline()) {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateWeighBrideUI(it) })
            vm.getWeighBridgeList()
        } else {
            vm.weighBridgeLocal.observe(viewLifecycleOwner, Observer { updateUIWithLocalData(it) })
            vm.getWeighBridgeDetail()
        }

        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()

        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })

        wbDetails = arguments?.getParcelable(GRN_DATA)!!
        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString())
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.quality.observe(viewLifecycleOwner, Observer {
            try {
                updateQualityApproval(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        currentMaterial = wbDetails.materialCode.toString()
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

        binding.tvSelectProcurementLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_procurement_type)) { mandatoryStars() } }

        binding.tvSelectPOLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_po_text)) { mandatoryStars() } }

        binding.tvWaybillnoLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.waybill_no)) { mandatoryStars() } }

        binding.tvWarehouseReceiptLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse_receipt_no)) { mandatoryStars() } }

        binding.btQualityDetails.setOnClickListener { moveToQualityDetails() }

        binding.llSelectPO.gone()

        binding.btnProceed.setOnClickListener {
            if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
                //wbDetails.unitPrice = binding.tvSelectProcurement.text.toString()
                wbDetails.unitPrice = wbDetails.netWeight
                when {
                    binding.tvSelectProcurement.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_all_mandatory_fields))
                    binding.tvSelectPO.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_all_mandatory_fields))
                    binding.tvWaybillNo.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_all_mandatory_fields))
                    binding.tvWarehouseReceiptNo.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_all_mandatory_fields))
                    else -> showConfirmDialog()
                }
            } else showConfirmDialog()
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.GRN.role)
       // updateYearValues()

        binding.tvSelectPO.setOnClickListener {
            //if (year.isNotEmpty())
                showSingleSelectDialog(procureType)
        }

        binding.tvSelectProcurement.setOnClickListener {
            showProcurementTypeDialog()
        }
    }

    private fun showProcurementTypeDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.select_procurement_type)
            listItemsSingleChoice(R.array.procurementType) { _, index, text ->
                binding.tvSelectProcurement.text = text
                binding.tvSelectPO.text = ""
                when (index) {
                    0 -> {
                        binding.llSelectPO.visible()
                        procureType = FIXED
                    }
                    1 -> {
                        binding.llSelectPO.visible()
                        procureType = PTBF
                    }
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showSingleSelectDialog(currentFlag: String) {
        val list = java.util.ArrayList<String>()
        when (currentFlag) {
            PTBF -> {
                filterpurchaseOrderList.clear()
                val purchaseOrderList1 =
                     purchaseOrderList.filter { it.bsart == PTBF }.filter { it.materialNumber.contains(wbDetails.materialCode ?: "  ") }
                        .filter { it.supplier.trim().contains(wbDetails.supplierCode?.trim() ?: " ") }
                filterpurchaseOrderList.addAll(purchaseOrderList1)
                list.addAll(filterpurchaseOrderList.map { data ->
                    //data.poId.plus("-").plus(data.openQuantity).plus(wbDetails.unitsOfMeasure)
                    data.poId
                })
            }
            YEAR -> {
                list.addAll(yearFilter)
            }
            FIXED -> {
                filterpurchaseOrderList.clear()
                val purchaseOrderList1 =
                    // purchaseOrderList.filter { it.year == year.trim() }.filter { it.poType == PTBF }
                    purchaseOrderList.filter { it.bsart == FIXED }.filter { it.materialNumber.contains(wbDetails.materialCode ?: "  ") }
                    .filter { it.supplier.trim().contains(wbDetails.supplierCode?.trim() ?: " ") }
                filterpurchaseOrderList.addAll(purchaseOrderList1)
                list.addAll(filterpurchaseOrderList.map { data ->
                    //data.poId.plus("-").plus(data.openQuantity).plus(wbDetails.unitsOfMeasure)
                    data.poId
                })
            }
        }
        if(list.size>0){
            MaterialDialog(requireContext()).show {
                title(R.string.select_purchase_order)
                listItemsSingleChoice(items = list) { _, index, text ->
                    binding.tvSelectPO.text = text
                    val item = filterpurchaseOrderList.singleOrNull { it.poId == text }
                    wbDetails.purchaseDocDesc = item?.ebelp
                }
                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
            }
        }else{
            showSnack(getString(R.string.no_po_available))
        }
        /*customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFlag,
                list,
                activity!!,
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)*/
    }

  /*  private fun updateYearValues() {
        val current: Int = Calendar.getInstance().get(Calendar.YEAR)
        binding.tvSelectPO.text = current.toString()
        year = current.toString()
        for (i in current.minus(1)..current) {
            yearFilter.add(i.toString())
        }
    }*/

    private fun updateWeighBrideUI(data: Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                   // hideLoading()
                    val weighBridgeUnFiltered =it.data?.data
                    if (weighBridgeUnFiltered?.size!! > 0) {
                        weighBridgeUnFiltered.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }.contains(wb.weighBridgeId)) {
                                weighBridgeUnFilteredList.add(wb)
                            }
                        }
                    }

                    val weighBridge =
                        it.data?.data?.filter {it.qcStatus == "X" &&  it.grnNumber.isNullOrEmpty() }
                    if (weighBridge?.size!! > 0) {
                        weighBridge.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }.contains(wb.weighBridgeId)) {
                                weighBridgeList.add(wb)
                            }
                        }
                    } else {
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }
            }
        }
    }

    private fun updateUIWithLocalData(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridge =
            data?.filter { it.qcStatus.isNullOrEmpty() && it.grnNumber.isNullOrEmpty() }
        if (weighBridge?.size!! > 0) {
            weighBridgeList = weighBridge as MutableList<VegaGrnWeighBridgeId>
        }
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            YEAR -> {
                year = data
                binding.tvSelectProcurement.text = data
            }
            PO -> {
                binding.tvSelectPO.text = data
                val split = data.split("-")
                val item = filterpurchaseOrderList.singleOrNull { it.poId == split[0] }
                wbDetails.purchaseDocNum = item?.poId
                wbDetails.purchaseDocDesc = item?.ebelp
                // wbDetails.purchaseDocQty = item?.openQuantity
                wbDetails.unitsOfMeasure = item?.meins ?: ""
            }
        }
    }

    private fun moveToQualityDetails() {

        qualitycallBack?.replaceQualityDetailsFragment(GRN_QUALITY_DETAILS, wbDetails, approveQualityList)
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val defaultStorageLoc = configItems.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        var isExist = false
        val isMaterial = defaultStorageLoc.map { it.materialCode }.contains(currentMaterial.removeRange(0, 6))
        defaultStorageLoc.forEach {
            if (currentMaterial.contains(it.materialCode) && !it.materialCode.isEmpty() && !isExist && isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            } else if (it.materialCode.isEmpty() && !isExist && !isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            }
        }
        vm.storageLocation.observe(viewLifecycleOwner, Observer {
        })
        vm.fetchStorageLocation(defaultStorageLocation)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    //hideLoading()
                    qualitylist.clear()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaIndiaCoffeeGRNQuality>
                        approveQualityList.forEach {
                            it.qualityParameters.forEach {
                                if (it.sapQCName == "ZNG_ADMIXTURE") {
                                    admixtureValue = it.satNam!!
                                }
                                val item = VegaQualityParams()
                                item.qualityParameterName = it.qualityParameterName
                                item.sapQCName = it.sapQCName
                                item.satNam = it.satNam
                                qualitylist.add(item)


                            }
                        }
                       /*// val grnPost = mutableListOf<VegaQualityParams>()
                        if (it1.isNotEmpty()) qualitylist1 = it1 as ArrayList<VegaQualityParams>
                        qualitylist1.forEach {
                            val item = VegaQualityParams()
                            item.qualityParameterName = it.qualityParameterName
                            item.sapQCName = it.sapQCName
                            item.satNam = it.satNam
                            qualitylist.add(item)
                        }*/


                        updateUIValues()
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updatePurchaseOrderUI(response: Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                purchaseOrderList = it.toMutableList()
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }

    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    /*moveToSuccessPage(
                        it.data?.data?.grnNumber.toString(),
                        //it.data?.data?.weighBridgeId.toString(),
                        //it.data?.data?.batchNumber.toString(),
                         binding.tvParamsWeighBID.text.toString(),
                         binding.tvProcurementType.text.toString(),
                        it.data?.data?.poNumber.toString(),
                        it.data?.data?.encodedImageContent.toString()
                    )*/
                    vm.updateGrnNoToQuality(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString()
                    )
                    vm.updateGrnSuccess(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString(),
                        getString(R.string.grn_success),
                        4
                    )

                    showConfirmApproveDialog(it.data?.data?.grnNumber.toString())

                    //moveToApprovalSuccessPage(wbId)

                    //showCreateGRNSuccessDialog();

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateQualityApproval(response: Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            it.data?.data?.currentWbid?.let { it1 ->
                               // it.data?.data?.charg?.let { it2 ->
                                    //it.data?.data?.grnNumber?.let { it3 ->
                                       //moveToSuccessPage(it1,it2,it3,"","")
                                        moveToApprovalSuccessPage(it.data?.data?.charg!!)
                                    //}
                               // }
                            }
                        }
                        else -> {
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")

                    //saveWB(weighBridgeDetails.weighBridgeId.toString(), batchNo.toString(), it.error.toString(), 3)
                    //saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                    //toast("${it.error}")
                }
            }
        }
    }

    private fun updateUIValues() {
        binding.tvParamsWeighBID.text = wbDetails.weighBridgeId
        binding.tvTruckSupplier.text = wbDetails.supplierCode.plus("-").plus(wbDetails.supplierName)
        binding.tvTruckNo.text = wbDetails.vehicleNumber
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvMaterial.text = wbDetails.materialName
        binding.tvPlantName.text = wbDetails.plantId

        vm.custonLocation.observe(this, Observer {
            storageLocation = it

            storageLocation?.forEach { item ->
                if (item.procureLocationCode.contains(wbDetails.storageLocationCode.toString(), true)) {
                    //binding.tvStorageLocation.text =wbDetails.storageLocationCode.plus("-").plus(item.procureLocationName.toString())
                    binding.tvStorageLocation.text =wbDetails.storageLocationCode
                }
            }
        })
        vm.getCustomLocations()


        val times = wbDetails.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        paidWeight = wbDetails.grossWeight!!.toDouble()

        if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
            binding.grnDetailsTitle.text = getString(R.string.grn)
            binding.tvProcurementType.text = wbDetails.batchNumber

            vm.getPOList()
           /* binding.etPrice.onChange {
                try {
                    val paidData = paidWeight.formatThreeDigits().replace(",", "")
                    val totalVal = it.toDouble() * paidData.toDouble()
                    binding.tvTotalValue.text = totalVal.formatThreeDigits()
                    wbDetails.totalPrice = totalVal.formatThreeDigits()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }*/
        } else {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
            binding.tvProcurementType.text = getString(R.string.fixed_purchase)
            binding.tvPoNumber.text = wbDetails.purchaseDocNum
            binding.llPoNumber.visible()
            binding.llPriceDetails.gone()
        }

    }

    private fun showConfirmDialog() {

        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.vega_india_coffee_grn_cutsom_dialog_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
                .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.ivClose.setOnClickListener { mAlertDialog?.dismiss() }
        mDialogView.tvConfirmGrn.setOnClickListener {
            mAlertDialog?.dismiss()

            wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
            wbDetails.purchaseDocNum = binding.tvSelectPO.text.toString()
            wbDetails.purchaseType = binding.tvSelectProcurement.text.toString()
            wbDetails.billOfLading = binding.tvWaybillNo.text.toString()
            wbDetails.warehouseRecieptNum = binding.tvWarehouseReceiptNo.text.toString()

            var filteredPOList =
                purchaseOrderList.filter { it.poId == binding.tvSelectPO.text.toString() }
            var totalbags: Int = 0
            approveQualityList.forEach {
                it.qualityParameters.forEach {
                    if (it.sapQCName.equals("Z_ACCEPTED_BAGS") || it.sapQCName.equals("Z_REJECTED_BAGS")) {
                        val intAcceptedBags: Int = (it.satNam.toString()).toInt()
                        totalbags = totalbags + intAcceptedBags
                    }
                }
            }

            val filteredWBList =  weighBridgeUnFilteredList.filter { it.weighBridgeId==binding.tvParamsWeighBID.text.toString() }
            if(filteredPOList.size==1 && filteredWBList.size==1){
                val filteredBagTypeList =  bagTypeList.filter {  it.bagType==wbDetails.bagType }
                if(filteredBagTypeList.get(0).bagType.equals(CROP_GUNNY_BAG) && wbDetails.challan.equals("1")) {
                    wbDetails.pmat2Count = totalbags.toString()
                    wbDetails.pmat2Type = filteredBagTypeList.get(0).bagMaterialCode
                }

                val wbData =
                    preparePostGrnData(wbDetails, admixtureValue, binding.tvDate.text.toString())
                if (isOnline()) {
                    vm.postGrn(
                        VegaIndiaCoffeeGrnPost(
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            grnData = listOf(wbData),
                            qualityDetails = preparePostGrnData1(qualitylist)
                        )
                    )
                } else {
                    saveData()
                }

            } else if(filteredPOList.size==1 && filteredWBList.size==2){
                val filteredBagTypeList =  bagTypeList.filter {  it.bagType==wbDetails.bagType }
                if((filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG") && wbDetails.challan.equals(""))) {
                    showSnack(getString(R.string.select_po_error_msg))
                }
            }else if(filteredPOList.size==2 && filteredWBList.size==2){
                val filteredBagTypeList =  bagTypeList.filter {  it.bagType==wbDetails.bagType }
                if((filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG") && wbDetails.challan.equals(""))) {
                    copyWbDetails = wbDetails.copy()
                    copyWbDetails.pmat2Count = ""
                    copyWbDetails.pmat2Type = ""
                    copyWbDetails.item = "00002"
                    val VegaGrnWeighBridgeId = (filteredWBList.filter { it.bagType.equals(filteredBagTypeList.get(0).bagType) }.filter { it.qcStatus == "" && it.grnNumber.isNullOrEmpty() })
                    //copyWbDetails.materialCode=filteredBagTypeList.get(0).bagMaterialCode
                    copyWbDetails.materialCode=VegaGrnWeighBridgeId.get(0).materialCode
                    copyWbDetails.netWeight=VegaGrnWeighBridgeId.get(0).netWeight
                    val VegaEcuadorPurchaseOrder = filteredPOList.filter { it.material.equals(copyWbDetails.materialCode) }
                    copyWbDetails.purchaseDocDesc = VegaEcuadorPurchaseOrder.get(0).ebelp
                    val wbData = preparePostGrnData(wbDetails,admixtureValue,binding.tvDate.text.toString())
                    val copiedWBData = preparePostGrnData(copyWbDetails,admixtureValue,binding.tvDate.text.toString())
                    grnList.add(wbData)
                    grnList.add(copiedWBData)
                    if (isOnline()) {
                        vm.postGrn(
                                VegaIndiaCoffeeGrnPost(
                                        key = getCurrentKey(),
                                        plant = getPlantDetails(),
                                        grnData = grnList,
                                        qualityDetails = preparePostGrnData1(qualitylist)
                                )
                        )
                    } else {
                        saveData()
                    }
                }
            } else if(filteredPOList.size==2 && filteredWBList.size==1){
                val filteredBagTypeList =  bagTypeList.filter {  it.bagType==wbDetails.bagType }
                if((! filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG"))) {
                    showSnack(getString(R.string.select_different_po))
                } else if(( (filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG") && wbDetails.challan.equals("1"))) ) {
                    showSnack(getString(R.string.select_gunny_error_msg))
                }else if(( (filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG") && wbDetails.challan.equals(""))) ) {
                    showSnack(getString(R.string.select_gunny_error_msg))
                }
            }
        }
        mAlertDialog?.show()



        /*val dialog = MaterialDialog(requireContext())
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.vega_india_coffee_grn_cutsom_dialog_layout)
        //val tvConfirmGrn = dialog.findViewById(R.id.tvConfirmGrn) as TextView
        binding.root.tvConfirmGrn.setOnClickListener {
            dialog.dismiss()
            wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
            val wbData = preparePostGrnData(wbDetails,admixtureValue)
            if (isOnline()) {
                vm.postGrn(
                        VegaIndiaCoffeeGrnPost(
                                key = getCurrentKey(),
                                plant = getPlantDetails(),
                                grnData = listOf(wbData)
                        )
                )
            } else {
                saveData()
            }
        }
        dialog.show()*/


        /*MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.confirm),
                    isPositive = true
                )
            ) {
                wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                val wbData = preparePostGrnData(wbDetails, admixtureValue)
                if (isOnline()) {
                    vm.postGrn(
                        VegaIndiaCoffeeGrnPost(
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            grnData = listOf(wbData)
                        )
                    )
                } else {
                    saveData()
                }
            }
            negativeButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.cancel),
                    isPositive = false
                )
            ) {
                dismiss()
            }
        }*/
    }

    private fun saveData() {
        wbDetails.isOfflineData = true
        if (!wbDetails.wbTempId.contains("TMP")) {
            wbDetails.isNotWBID = true
            wbDetails.grnNumber = getTmpId()
        }
        vm.updateGRNPrice(wbDetails)
        moveToSuccessPage(wbDetails.weighBridgeId.toString(), "", "",  "")
    }

    private fun moveToSuccessPage(
        grn: String,
        wbId: String,
        batchNo: String,
        encodedImageContent: String?
    ) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isOnline()) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success))
        } else
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success_offline))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            "WB ID : ".plus(wbId).plus("\n LOT ID : ").plus(batchNo).plus("\n GRN No : ").plus(grn)
        )

        intent.putExtra(AppUtils.PRINT_ENABLE, true)

        val grnDco = ArrayList<String>()
        grnDco.add(encodedImageContent ?: "")
        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, grnDco)
        intent.putExtra("fromsesamegrn", true)

        startActivity(intent)
        requireActivity().finish()

    }

    private fun moveToApprovalSuccessPage(lotId: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
        intent.putExtra(AppUtils.SUB_TITLE, lotId)
        intent.putExtra(
            AppUtils.SUB_TITLE,
            "\n ".plus((getString(R.string.lot_id))).plus(lotId))
        startActivity(intent)
        requireActivity().finish()
    }
    private fun showConfirmApproveDialog(grnNo: String) {

        val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_bc_approve_india_coffe_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.gone()
        mDialogView.llApproval.visible()
        mDialogView.tvGrnNo.text = getString(R.string.procurement_completed).plus("\n").plus(getString(R.string.grn_no)).plus(grnNo)
        mDialogView.ivClose.gone()

        mDialogView.tvConfirmApproval.setOnClickListener {
            mAlertDialog?.dismiss()
            wbPostDetails.batchNumber = wbDetails.batchNumber

            wbPostDetails.bagCount = wbDetails.bagCount
            wbPostDetails.bagType = wbDetails.bagType
            wbPostDetails.bagWeight = wbDetails.bagWeight
            wbPostDetails.grossWeight = wbDetails.grossWeight
            wbPostDetails.item = "00001"
            wbPostDetails.materialCode = wbDetails.materialCode
            wbPostDetails.netWeight = wbDetails.netWeight
            wbPostDetails.plant = wbDetails.plantId
            wbPostDetails.supplierCode = wbDetails.supplierCode
            wbPostDetails.storageLocationCode = wbDetails.storageLocationCode.toString()
            wbPostDetails.unitsOfMeasure = "KG"
            wbPostDetails.vehicleNumber = wbDetails.vehicleNumber
            wbPostDetails.weighBridgeId = wbDetails.weighBridgeId.toString()
            wbPostDetails.weighBridgeType = wbDetails.weighBridgeType
            wbPostDetails.deliveryItem = wbDetails.deliveryItem

            wbPostDetails.finalApproval = "X"
            wbPostDetails.qualityFlag=true
            wbPostDetails.appName = "BC"
            /* wbPostDetails.LOBM_UDCODE = "01       A"
             wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }*/
            val qualityDetails: ArrayList<VegaQuality> = ArrayList()
            approveQualityList.forEach {
                it.qualityParameters.forEach {
                    val quality = VegaQuality()
                    quality.descrChar = ""
                    quality.nameChar = it.sapQCName.toString()
                    quality.qualityParameterValue = it.satNam
                    qualityDetails.add(quality)
                }
            }
            val usageDecision = VegaQuality()
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    A"

            qualityDetails.add(usageDecision)

            wbPostDetails.qualityDetails = qualityDetails
            wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }

            vm.postQualityParams(
                VegaCameroonQcPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityQcPostList
                )
            )

        }

        mAlertDialog?.show()
    }
    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.grn_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }
}
