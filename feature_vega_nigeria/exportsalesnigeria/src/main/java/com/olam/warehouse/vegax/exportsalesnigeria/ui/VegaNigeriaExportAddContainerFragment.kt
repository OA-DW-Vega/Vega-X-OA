package com.olam.warehouse.vegax.exportsalesnigeria.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesnigeria.R
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.*
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.FragmentExportSalesNigeriaAddContainerBinding
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.ItemExportNigeriaSalesLotBinding
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.ItemExportSalesNigeriaMaterialListBinding
import com.olam.warehouse.vegax.exportsalesnigeria.utils.MOVE_ADD_LOT
import com.olam.warehouse.vegax.exportsalesnigeria.utils.MOVE_SUMMARY
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaNigeriaExportAddContainerFragment : BaseFragment() {

    private var containerNumberList = ArrayList<String>()
    override val layoutResourceId = R.layout.fragment_export_sales_nigeria_add_container
    private val vm: VegaNigeriaExportSalesViewModel by viewModel()
    private var callback: CallBack? = null
    private lateinit var binding: FragmentExportSalesNigeriaAddContainerBinding
    private var salesOrderList = listOf<VegaNigeriaExportSalesOrderModel>()
    private var materialList = ArrayList<String>()
    private var materialListItems = ArrayList<materialList>()
    private var textdetailListItems = ArrayList<VegaNigeriaExportSalesTextDetail>()
    private var soNumbers = ArrayList<String>()
    private var editContainerId: String = ""
    private var selectedContainerId: String = ""
    private var containerInventoryList = ArrayList<NigeriaContainerInventory>()
    private var jsonData = mutableListOf<String>()
    var releaseUrls = HashMap<String, String>()
    private var shiftList = mutableListOf<String>()
    private var tollingDetails = arrayListOf<VegaNigeriaSalesType>()
    private var isCompliantMaterial: Boolean = false
    private var productList = emptyList<VegaMaterial>()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String,
            data: Any,
            materialList: ArrayList<String>
        )

        fun replaceSummaryFragment(
            fragment: String,
            data: Any,
            materialList: ArrayList<materialList>,
            textdetailList: ArrayList<VegaNigeriaExportSalesTextDetail>
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentExportSalesNigeriaAddContainerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalesNigeria/ui/VegaNigeriaExportAddContainerFragment").title("Vega_Nigeria/Export Sales")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        showTTDialog()
        enableProceed(false)
        binding.etEnterContainer.isEnabled = false
        vm.dispatchSalesOrderModel.observe(viewLifecycleOwner, Observer { updateSalesOrder(it) })
        vm.inventory.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateShiftAndRemarks(it) })
        vm.getShiftRemarksItems(getCurrentKey())
        //vm.getPurchaseOrder()
//        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
//        binding.btAdd.setOnClickListener {
//            validateLot(binding.etEnterContainer.text.toString())
//        }
        /* binding.flContainer.setOnClickListener{
             validateLot()
         }*/

        vm.allProduct.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    productList = it.filter { if(isCompliantMaterial) it.complainceFlag.equals(Constants.COMPLAINT) else it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                }
            })
        binding.btProceed.setOnClickListener {
            var weight = 0.0
            textdetailListItems.clear()
            var isEmptyWeight = false
            vm.containerList.forEach {
                if (it.lots.size == 0 || it.lots.any { it.editedWeight?.toDouble() ?: 0.0 <= 0 }) isEmptyWeight =
                    true
                it.lots.forEach {
                    weight = weight.plus(it.editedWeight.toString().toDouble())
                }
            }

            tollingDetails.forEach { it ->

                if (it.BILLOFLADINGNUMBER!!.isNotEmpty()) {
                    var textdetailitems = VegaNigeriaExportSalesTextDetail()
                    textdetailitems.textId = it.BILLOFLADINGNUMBER
                    textdetailitems.textValue = binding.tvLandingNo.text.toString()
                    textdetailListItems.add(textdetailitems)
                }
                if (it.PORTOFLOADING!!.isNotEmpty()) {
                    var textdetailitems = VegaNigeriaExportSalesTextDetail()
                    textdetailitems.textId = it.PORTOFLOADING
                    textdetailitems.textValue = binding.tvPortLoading.text.toString()
                    textdetailListItems.add(textdetailitems)
                }
                if (it.BILLOFLADINGDATE!!.isNotEmpty()) {
                    var textdetailitems = VegaNigeriaExportSalesTextDetail()
                    textdetailitems.textId = it.BILLOFLADINGDATE
                    val srcDf: DateFormat = SimpleDateFormat("MM/dd/yyyy")
                    val date = srcDf.parse(binding.tvLandingDate.text.toString())
                    val destDf: DateFormat = SimpleDateFormat("yyyyMMdd")
                    var formatteddate = destDf.format(date)
                    textdetailitems.textValue = formatteddate
                    textdetailListItems.add(textdetailitems)
                }
                if (it.DELIVEREDQTY!!.isNotEmpty()) {
                    var textdetailitems = VegaNigeriaExportSalesTextDetail()
                    textdetailitems.textId = it.DELIVEREDQTY
                    textdetailitems.textValue = weight.toString()
                    textdetailListItems.add(textdetailitems)
                }
                if (it.PORTOFDISCHARGEDETAILED!!.isNotEmpty()) {
                    var textdetailitems = VegaNigeriaExportSalesTextDetail()
                    textdetailitems.textId = it.PORTOFDISCHARGEDETAILED
                    textdetailitems.textValue = binding.tvPortDischarge.text.toString()
                    textdetailListItems.add(textdetailitems)
                }
                if (it.SHIPPINGLINE!!.isNotEmpty()) {
                    var textdetailitems = VegaNigeriaExportSalesTextDetail()
                    textdetailitems.textId = it.SHIPPINGLINE
                    textdetailitems.textValue = binding.tvShipingLine.text.toString()
                    textdetailListItems.add(textdetailitems)
                }
                if (it.VESSELNAMEFLIGHTNO!!.isNotEmpty()) {
                    var textdetailitems = VegaNigeriaExportSalesTextDetail()
                    textdetailitems.textId = it.VESSELNAMEFLIGHTNO
                    textdetailitems.textValue = binding.tvvesselname.text.toString()
                    textdetailListItems.add(textdetailitems)
                }
            }
            if (!binding.tvvesselname.text.isNullOrEmpty() && !binding.tvShipingLine.text.isNullOrEmpty() && !binding.tvPortDischarge.text.isNullOrEmpty() && !binding.tvPortLoading.text.isNullOrEmpty() && !binding.tvLandingNo.text.isNullOrEmpty()) {
                if (!isEmptyWeight)
                    callback?.replaceSummaryFragment(
                        MOVE_SUMMARY,
                        vm.currentOT,
                        materialListItems,
                        textdetailListItems
                    )
                else
                    activity?.toast(getString(R.string.add_weights))
            } else
                activity?.toast(getString(R.string.enter_all_mandatory_fields))
        }

        vm.otContainer.observe(viewLifecycleOwner, Observer { updateContainer(it) })
        vm.validateContainer.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (vm.containerList.any { it1 -> it1.container.containerNumber.equals(it.containerNumber) })
                    activity?.toast(getString(R.string.conatiner_already_added))
                else {
                    activity?.toast(getString(R.string.conatiner_already_added_other))
                }
//                binding.etEnterContainer.setText("")
                vm.currentOldContainer = ""
                vm.currentNewContainer = ""
            } else {
                if (vm.currentNewContainer.isEmpty() && vm.currentOldContainer.isEmpty()) {
                    val container = VegaCoffeeExportSalesContainer()
                    container.saleOrderId = vm.currentOT
                    container.containerNumber = vm.currentContainer
                    vm.saveContainer(container)
                    vm.updateContainerStatus("Stuffing in Progress", container.containerNumber)

                } else {
                    vm.updateContainerId(vm.currentOldContainer, vm.currentNewContainer)
                    vm.currentOldContainer = ""
                    vm.currentNewContainer = ""
                }
                /*if (vm.containerList.size > 0) {
                    if (vm.containerList.any { it.container.containerNumber.equals(vm.currentContainer) })
                        activity?.toast(getString(R.string.conatiner_already_added))
                    else {
                        container.containerNumber = vm.currentContainer
                        vm.saveContainer(container)
                    }
                } else {
                    container.containerNumber = vm.currentContainer
                    vm.saveContainer(container)
                }*/
            }

        })
        binding.tvLandingDate.text =
            DateUtils.getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvLandingDate.setOnClickListener { getDatePickerDialog() }
    }
    private fun updateUIWithOnlineData(data: Resource<GenericReqAndResp<VegaNigeriaInventoryModel>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if(!it.data?.data?.message.equals("Getting the container details")){
//                        showErrorDialogWithFAQLink(requireContext(), it.data?.data?.message.toString())
                    }else{
                        containerInventoryList = it.data?.data?.containerDTOs?.filter{!it.status.equals("Deleted") } as ArrayList<NigeriaContainerInventory>
                        containerNumberList = containerInventoryList.map { it.containerNum } as ArrayList<String>
                        updateContainerListUI(containerNumberList)
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {}
            }
        }
    }

    private fun validateLot(containerId: String) {
       if(containerInventoryList.map { it.containerNum }.contains(containerId)) {
           vm.currentContainer = containerId
           vm.validateContainer(containerId)
       }
        else if(vm.currentOT.equals("")){
           showErrorDialogWithFAQLink(requireContext(), "Select OT before scanning the container")
       }
        else{
           showErrorDialogWithFAQLink(requireContext(), "Container id either does not exist or already in use")
       }
    }

    private fun updateContainer(containerList: VegaCoffeeExportOTWithContainer?) {
        if (containerList != null)
            updateContainerAdapter(containerList)
        else
            updateContainerAdapter(VegaCoffeeExportOTWithContainer())
    }

    private fun updateContainerAdapter(data: VegaCoffeeExportOTWithContainer) {
//        binding.etEnterContainer.setText("")
        val containerList = mutableListOf<ContainerWithLots>()
        if (data.lineItems.size > 0) {
            if (editContainerId.isNotEmpty()) {
                enableItems(false)
                containerList.addAll(data.lineItems.filter { it.container.containerNumber.equals(editContainerId) })
            } else {
                enableItems(true)
                containerList.addAll(data.lineItems)
            }
            binding.rvLots.visible()
            binding.tvNoData.gone()
            vm.containerList = data.lineItems as ArrayList<ContainerWithLots>
            enableProceed(true)
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
            enableProceed(false)
        }
        binding.rvLots.setUpAdapter(
            containerList,
            R.layout.item_export_nigeria_sales_lot,
            ItemExportNigeriaSalesLotBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvContainerId.text = it.container.containerNumber
                val lotCount = it.lots.size
//                val uom = if (lotCount > 0) it.lots[0].unitOfMeasure else ""
                val uom = "MT"
                val materialList = it.lots.map { it.materialName.toString() }.distinct()
                val totalWeight = it.lots.sumByDouble { it.editedWeight?.toDouble() ?: 0.0 }
                bindItem.tvLotsCount.text = if (lotCount != 0) lotCount.toString() else "-"
                bindItem.tvGradeValue.text =
                    if (materialList.size > 0) materialList.toString().replace("[", "")
                        .replace("]", "").replace(
                            ",",
                            "\n"
                        ) else "-"
                bindItem.tvWeightValue.text =
                    if (!totalWeight.equals(0.0)) totalWeight.toString().plus(" ")
                        .plus(uom) else "-"

                if (editContainerId.isNotEmpty()) bindItem.ivClose.gone() else bindItem.ivClose.visible()
                bindItem.ivClose.setOnClickListener { view -> showDeleteDialog(it, view) }
                bindItem.ivEdit.setOnClickListener { view ->
                    showContainerEditDialog(
                        it.container.containerNumber,
                        view
                    )
                }
            },
            {
                val item = this
                callback?.replaceFragment(MOVE_ADD_LOT, item.container, materialList)
            })

    }

    private fun showContainerEditDialog(
        oldContainerId: String,
        view: View
    ) {
        MaterialDialog(view.context).show {
            // message((R.string.edit_container_id))
            var containerId = ""
            input(waitForPositiveButton = false, hint = getString(R.string.enter_container_id)) { dialog, text ->
                val inputField = dialog.getInputField()
                val isValid = text.isNotEmpty()
                containerId = text.toString()
                inputField.error = if (isValid) null else getString(R.string.enter_container_id)
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
            }
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.currentOldContainer = oldContainerId
                    vm.currentNewContainer = containerId
                    if (editContainerId.isNotEmpty()) editContainerId = containerId
                    vm.validateContainer(containerId)
                    //vm.updateContainerId(oldContainerId, containerId)
                },
                { dismiss() })
        }
    }


    private fun showDeleteDialog(container: ContainerWithLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove_container))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.containerList.remove(container)
                    vm.removeConatinerWitfLots(container.container.containerNumber)
                    // Need to call API for changing status to New Container
                    vm.updateContainerStatus("New Container",container.container.containerNumber)
                },
                { dismiss() })
        }
    }

    fun enableItems(flag: Boolean) {
        binding.spStage.isEnabled = flag
        binding.etEnterContainer.isEnabled = flag
        binding.btAdd.isEnabled = flag
        binding.clScan.isEnabled = flag
    }

    private fun updateSalesOrder(response: Resource<GenericReqAndResp<List<VegaNigeriaExportSalesOrderModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.filter { productList.map { it.materialCode }.contains(if(it.salesOrderList.isNotEmpty())it.salesOrderList.get(0).materialNumber?.takeLast(12) else "" )  }?.let { it1 ->
                        salesOrderList = it1
                        updageSalesOrderUI(salesOrderList)
//                        if (AppUtils.isOnline()) {
//                            vm.getContainerInventory("New Container")
//                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateContainerListUI(containerList: ArrayList<String>) {
        binding.etEnterContainer.isEnabled = true
        var containerNumberList = ArrayList<String>()
        containerNumberList.add(getString(R.string.select_container))
        containerNumberList.addAll(containerList)
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_nigeria_export_grade, containerNumberList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.etEnterContainer.adapter = stageAdapter
        binding.etEnterContainer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    selectedContainerId = containerNumberList[position]
                    validateLot(selectedContainerId)
                }
                binding.etEnterContainer.setSelection(0)
            }
        }
    }


    private fun updageSalesOrderUI(soList: List<VegaNigeriaExportSalesOrderModel>) {
        val saleOrder = ArrayList<VegaNigeriaExportSalesOrderModel>()
        val stageInitItem = VegaNigeriaExportSalesOrderModel()
        stageInitItem.salesOrderId = getString(R.string.select_ot)
        saleOrder.add(stageInitItem)
        saleOrder.addAll(soList)
        val soListData = saleOrder.map { data -> data.salesOrderId }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_nigeria_export_grade, soListData)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    val data = saleOrder[position]
                    vm.currentOT = data.salesOrderId
                    vm.containerList = ArrayList()
                    vm.getOTWithContainer(data.salesOrderId)
                    setUpMaterialAdapter(data.salesOrderList)
                    vm.getContainerInventory("New Container")
                }
            }
        }
    }

    private fun setUpMaterialAdapter(selectedSales: List<materialList>) {
        materialList.clear()
        materialListItems.clear()
        materialListItems.addAll(selectedSales)
        materialList.addAll(selectedSales.map { it.materialNumber.toString() })
        binding.rvMaterialList.setUpAdapter(
            selectedSales as MutableList<materialList>,
            R.layout.item_export_sales_nigeria_material_list,
            ItemExportSalesNigeriaMaterialListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterial.text = it.materialDesc
                var soQuantityinKG = ""
                when (it.meins) {
                    "KG" -> soQuantityinKG = it.openQuantity.toString()
                    "MT" -> {
                        soQuantityinKG = (it.openQuantity.toString())
                    }
                }
                if (soQuantityinKG.isNotEmpty()) {
                    bindItem.tvSoWeight.text =
                        soQuantityinKG.toDouble().formatThreeDigits().plus(" ").plus("MT")
                }
            })
    }

    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    validateLot(it)
                }
            }
        }
    }

    private fun enableProceed(enable: Boolean) {
        binding.btProceed.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    fun getBack() {
        editContainerId = ""
        vm.getOTWithContainer(vm.currentOT)
    }

    fun editLotDetails(container: VegaCoffeeExportSalesContainer) {
        editContainerId = container.containerNumber
        vm.getOTWithContainer(container.saleOrderId)
    }

    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateTxt = binding.tvLandingDate.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        val DATE_FORMAT = "MM/dd/yyyy"
        val UTC = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    binding.tvLandingDate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

    }

    private fun updateShiftAndRemarks(miscellaneous: List<VegaCocoaMiscellaneous>) {
        tollingDetails.clear()
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()

        jsonData.forEach {
            if (it.contains("TEXT_UPDATE")) {
                if (it.isNotEmpty()) {
                    val shift = gson.fromJson(it, VegaNigeriaSalesTypeModel::class.java)
                    shift.TEXT_UPDATE.forEach {
                        var toll = VegaNigeriaSalesType()
                        toll.BILLOFLADINGNUMBER = it.BILLOFLADINGNUMBER
                        toll.PORTOFLOADING = it.PORTOFLOADING
                        toll.PORTOFDISCHARGEDETAILED = it.PORTOFDISCHARGEDETAILED
                        toll.BILLOFLADINGDATE = it.BILLOFLADINGDATE
                        toll.VESSELNAMEFLIGHTNO = it.VESSELNAMEFLIGHTNO
                        toll.DELIVEREDQTY = it.DELIVEREDQTY
                        toll.SHIPPINGLINE = it.SHIPPINGLINE
                        tollingDetails.add(toll)
                    }
                }
            }
        }

    }

    fun onbackpressed() {
        binding.tvvesselname.text.clear()
        binding.tvShipingLine.text.clear()
        binding.tvPortDischarge.text.clear()
        binding.tvPortLoading.text.clear()
        binding.tvLandingNo.text.clear()
    }
    private fun showTTDialog() {
        MaterialDialog(requireContext()).show {
            cancelOnTouchOutside(false)
            message(com.olam.warehouse.login.R.string.select_procurement_type)
            UIUtils.getTTDialogOnlyForMaterial(
                this,
                "",
                "",

                {
                        isComplaint, isThirdParty, isFarmerLessTransaction ->  if(isComplaint) isCompliantMaterial = true else false
                    vm.getAllProduct()
                    vm.getPurchaseOrder()

                },
                { dismiss() },
                false)
        }

    }
}
