package com.olam.warehouse.vegax.localsalescameroon.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesLotWithbags
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalescameroon.R
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesOrderModel
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalescameroon.databinding.FragmentCameroonSalesAddLotBinding
import com.olam.warehouse.vegax.localsalescameroon.databinding.ItemCameroonSalesLotBinding
import com.olam.warehouse.vegax.localsalescameroon.databinding.ItemCameroonSalesMaterialListBinding
import com.olam.warehouse.vegax.localsalescameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaCameroonSalesAddLotFragment : BaseFragment(), CameroonRecyclerViewItemClickListener {

    private var callBack: CallBack? = null
    private val vm: VegaCameroonSalesViewModel by viewModel()
    private var dispatchType = ""
    private var salesOrderList = mutableListOf<VegaCameroonSalesOrderModel>()
    private var materialList = ArrayList<String>()
    private var materialListItems = ArrayList<materialList>()
    private var soNumbers = ArrayList<String>()
    private var isEditableLot = true
    private var isMultipleLot = false
    private var isAlreadyLoading = false
    private var isReceived = false
    private var addWeightPosition = 0
    private var customDialog: CustomCameroonSingleSelectDialog? = null
    private var materialNameList = ArrayList<String>()
    private var editLotId: String = ""
    private var editLotMaterial: String = ""
    var isMissedPallet = false

    private var currentKey = getCurrentKey()
    private var gradeList = mutableListOf<VegaQualitative>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var certicateList = mutableListOf<VegaQualitative>()
    private var certicateFilterList = mutableListOf<VegaQualitative>()
    private var materialCode: String = ""
    private var dispatchLotsList = mutableListOf<VegaNicaraguaGRNInventoryDetails>()
    private var productList = emptyList<VegaMaterial>()


    override val layoutResourceId: Int = R.layout.fragment_cameroon_sales_add_lot
    private lateinit var binding: FragmentCameroonSalesAddLotBinding
    private var isCompliantMaterial: Boolean = false


    companion object {
        fun newInstance(dispatch: VegaCoffeeSalesOrder, dispatchType: String) =
            VegaCameroonSalesAddLotFragment().putArgs {
                putParcelable(SALES_ITEM, dispatch)
                putString(SALES_TYPE, dispatchType)
            }
    }

    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, salesType: String)
        fun replaceFragment(fragment: String, lot: VegaCoffeeSalesLots)
        fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, materialList: ArrayList<String>)
        fun replaceFragmentSummary(
            fragment: String,
            model: VegaCoffeeSalesOrder,
            materialItems: ArrayList<materialList>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCameroonSalesAddLotBinding.inflate(inflater)
        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvQualityGrade.visibility = View.VISIBLE
            binding.tvQualityGradeValue.visibility = View.VISIBLE
            //binding.ivgDown.visibility = View.VISIBLE
            binding.tvCertification.visibility = View.VISIBLE
            binding.tvCertificationValue.visibility = View.VISIBLE
            //binding.ivsDown.visibility = View.GONE
        }
        initExtra()
        initUI()
        return binding.root
    }


    private fun initExtra() {
        vm.salesOrder = arguments?.getParcelable<VegaCoffeeSalesOrder>(SALES_ITEM) as VegaCoffeeSalesOrder
        dispatchType = arguments?.getString(SALES_TYPE) ?: ""
        vm.salesOrder.salesType = dispatchType
        vm.currentTempId = vm.salesOrder.salesTempId
        binding.tvstoValue.isEnabled = vm.currentTempId.isEmpty()
        certificate = ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("localsalescameroon/ui/VegaCameroonSalesAddLotFragment")
            .title("Vega_Cameroon/Local Sales").with(tracker)
    }

    private fun initUI() {
        showTTDialog()
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        disableAddLotsButtons()
        vm.dispatchSalesOrderModel.observe(viewLifecycleOwner, Observer { updateSalesOrder(it) })
        //vm.getPurchaseOrder()
        vm.dispatchSalesItem.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) updateSalesItem(it) else updateSalesItem(
                    VegaCoffeeSalesOrderWithLots()
                )
            })
        vm.allProduct.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    productList = it.filter { if(isCompliantMaterial) it.complainceFlag.equals(Constants.COMPLAINT) else it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                }
            })

        var titleString = ""
        when (dispatchType) {
            SALES_TYPE_WEIGHSCALE -> titleString = getString(R.string.weighscale)
            SALES_TYPE_ANTICIPATED -> titleString = getString(R.string.anticipated)
            SALES_TYPE_WEIGHBRIDGE -> titleString = getString(R.string.weighbridge_weighscale)
        }
        binding.tvTitle.text = getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ").plus(titleString)
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_so), STO_NO) }

        if(currentKey.split("_")[1].contains("NI")){

            vm.grade.observe(viewLifecycleOwner, Observer {
                gradeList = it.toMutableList()
            })
            vm.certification.observe(viewLifecycleOwner, Observer {
                certicateList = it.toMutableList()
                certicateFilterList = it.toMutableList()
            })
            vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
                materialQualityGradeList = it.toMutableList()
            })
            binding.tvQualityGradeValue.setOnClickListener {
                showSingleSelectDialog(true,getString(R.string.select_grade), QUALITY_GRADE )
            }
            binding.tvCertificationValue.setOnClickListener {
                showSingleSelectDialog(true,getString(R.string.select_certificate), CERTIFICATION )
            }

        }

        binding.clInventory.setOnClickListener {

            if(currentKey.split("_")[1].contains("NI")){
                validateSelectFromInventory()
            }
            else {
                callBack?.replaceFragment(
                    INVENTORY_FRAG,
                    vm.salesOrder,
                    materialList
                )
            }
        }
        binding.btProceed.setOnClickListener {
            if (vm.lots.isNotEmpty())
                if (validateLotWeightWithMaterialWeight()) {
                    if (validateLotWeight()) {
                        if (validateZeroWeight()) {
                            if (editLotId.isEmpty() && dispatchType.equals(SALES_TYPE_WEIGHSCALE)) {
                                if (!isMissedPallet) {
                                    showRemarkDialog()
                                } else {
                                    activity?.toast(getString(R.string.weighment_mismatch))
                                }
                            } else {
                                if (dispatchType.equals(SALES_TYPE_WEIGHSCALE) && !isMissedPallet) {
                                    moveToSummary("")
                                } else if (dispatchType.equals(SALES_TYPE_ANTICIPATED)) {
                                    moveToSummary("")
                                } else {
                                    activity?.toast(getString(R.string.weighment_mismatch))
                                }
                            }

                        } else Toast.makeText(
                            activity,
                            getString(R.string.zero_weight_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else Toast.makeText(
                        activity,
                        getString(R.string.less_weight_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.less_weight_for_so_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            else Toast.makeText(
                activity,
                getString(R.string.prceed_error),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.btAdd.setOnClickListener {
            vm.currentScanLot = binding.etEnterContainer.text.toString()
            vm.validateLot(binding.etEnterContainer.text.toString())
            isReceived = false
        }

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (!isAlreadyLoading) {
                isAlreadyLoading = true
                if (it != null) {
                    showLotAlreadyExistDialog(true)
                    binding.etEnterContainer.setText("")
                    isAlreadyLoading = false
                } else
                    fetchLotDetails(
                        vm.currentScanLot,
                        materialList, vm.salesOrder.plantId ?: ""
                    )
            }
        })


        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_so), STO_NO) }
        when {
            dispatchType.equals(SALES_TYPE_WEIGHSCALE, true) -> {
                binding.btSave.visibility = View.VISIBLE
                binding.ivProceed.visibility = View.VISIBLE
            }
            else -> {
                binding.btSave.visibility = View.GONE
                binding.ivProceed.visibility = View.GONE
            }
        }
        binding.btSave.setOnClickListener {
            if (vm.lots.isNotEmpty())
                showConfirmSaveDialog()
            else Toast.makeText(
            activity,
            getString(R.string.prceed_error),
            Toast.LENGTH_SHORT
        ).show()
        }

    }

   private fun certificateMaterial():Boolean{
        var isCertifiedMaterial=false
        materialListItems.forEach {
            if(it.materialDesc?.contains("Certifi",true) == true){
             isCertifiedMaterial= true
            }
        }
        return isCertifiedMaterial
    }

  private  fun moveToInventoryFragment(){
        callBack?.replaceFragment(
            INVENTORY_FRAG,
            vm.salesOrder,
            materialList
        )
    }

    private fun validateSelectFromInventory(){
        if(binding.tvQualityGradeValue.text.isNotEmpty()){
            if(certificateMaterial() && binding.tvCertificationValue.text.isEmpty()){
                Toast.makeText(
                    activity,
                    getString(R.string.certificate_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else{
                moveToInventoryFragment()
            }
        } else{
            Toast.makeText(
            activity,
            getString(R.string.grade_empty),
            Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }


    private fun showRemarkDialog() {

        showDialog(getString(R.string.stop_loding_confirmation), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    vm.salesOrder.remarks = remark
                    moveToSummary(remark)
                }
            }

        }, true, vm.salesOrder.remarks.toString())
    }

    private fun moveToSummary(remark: String) {
        vm.salesOrder.salesType = dispatchType
        if (remark.isNotEmpty()) vm.salesOrder.remarks = remark
        vm.saveWeighBridgeAndLotDetails()
        vm.salesOrder.lotList = vm.lots
        getCurrentDate("END")
        val a = vm.salesOrder.endTime?.toLong() ?: 0
        val b = vm.salesOrder.startTime?.toLong() ?: 0
        val duration = a.minus(b)
        vm.salesOrder.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration).toString()
        callBack?.replaceFragmentSummary(SUMMARY, vm.salesOrder, materialListItems)
    }

    private fun showConfirmSaveDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.save_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    if (vm.salesOrder.salesTempId.isBlank()) {
                        val randomDouble = "TMP".plus(Random.nextLong().toString())
                        vm.salesOrder.salesTempId = randomDouble
                    }
                    vm.saveWeighBridgeAndLotDetails()
                    Toast.makeText(activity, getString(R.string.local_save_msg), Toast.LENGTH_SHORT)
                        .show()
                    activity?.finish()
                },
                { dismiss() })
        }
    }

    private fun updateSalesOrder(response: Resource<GenericReqAndResp<List<VegaCameroonSalesOrderModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    salesOrderList = it.data?.data?.filter { productList.map { it.materialCode }.contains(if(it.salesOrderList.isNotEmpty())it.salesOrderList.get(0).materialNumber?.takeLast(12) else "" )  } as MutableList<VegaCameroonSalesOrderModel>
                    soNumbers.clear()
                    soNumbers.addAll(salesOrderList.map { it.salesOrderId })
                    if (vm.salesOrder.salesTempId.isNotEmpty())
                        clickOnItem(vm.salesOrder.saleOrderId, true, STO_NO)

                    /*if(currentKey.split("_")[1].contains("NI")){
                        dispatchLotsList.clear()
                        val dataValue = it.data?.data!!
                        val lotAll = prepareStocksToInventoryDeatils(dataValue, productList)
                        dispatchLotsList = lotAll
                    }*/

                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun disableAddLotsButtons() {
        binding.clScan.isEnabled = false
        binding.clInventory.isEnabled = false
        binding.etEnterContainer.isEnabled = false
        binding.btAdd.isEnabled = false
        ViewCompat.setBackgroundTintList(
            binding.clScan,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.grey
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.clInventory,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.grey
            )
        )
    }

    private fun enableAddLotButtons() {
        binding.clScan.isEnabled = true
        binding.clInventory.isEnabled = true
        binding.etEnterContainer.isEnabled = true
        binding.btAdd.isEnabled = true
        ViewCompat.setBackgroundTintList(
            binding.btAdd,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )

        ViewCompat.setBackgroundTintList(
            binding.clScan,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.dark_green_1
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.clInventory,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.dark_green_1
            )
        )
    }


    private fun updateSalesItem(it: VegaCoffeeSalesOrderWithLots) {
        if (it.lineItems.size > 0) {
            var data = mutableListOf<VegaCoffeeSalesLotWithbags>()
            if (vm.salesOrder.salesTempId.isNotEmpty()) {
                data =
                    it.lineItems.filter { it1 -> it1.salesLot.salesTempId.equals(vm.salesOrder.salesTempId) } as MutableList<VegaCoffeeSalesLotWithbags>
            } else {
                if (it.salesOrder.salesType.equals(SALES_TYPE_ANTICIPATED)) {
                    vm.salesOrder = it.salesOrder
                    data =
                        it.lineItems.filter { it2 -> it2.salesLot.salesType.equals(SALES_TYPE_ANTICIPATED) } as MutableList<VegaCoffeeSalesLotWithbags>
                } else if (it.salesOrder.salesType.equals(SALES_TYPE_WEIGHSCALE)) {
                    val data1 = it.lineItems.filter { it2 -> it2.salesLot.salesType.equals(SALES_TYPE_WEIGHSCALE) }
                        .filter { it.salesLot.saleOrderId.equals(binding.tvstoValue.text.toString()) }
                    val tempIds = data1.map { it.salesLot.salesTempId }

                    if (tempIds.size == 1) {
                        vm.salesOrder.salesTempId = tempIds[0]
                        data = data1 as MutableList<VegaCoffeeSalesLotWithbags>
                    } else {
                        if (vm.currentTempId.isNotEmpty()) {
                            vm.salesOrder.salesTempId = vm.currentTempId
                            data =
                                data1.filter { it.salesLot.salesTempId.equals(vm.currentTempId) } as MutableList<VegaCoffeeSalesLotWithbags>
                        } else data = mutableListOf()
                    }
                } else
                    data = mutableListOf()
            }
            setUpAdapter(data)
        } else setUpAdapter(mutableListOf())
    }


    private fun getCurrentDate(type: String) {
        if (SALES_TYPE_WEIGHBRIDGE != dispatchType) {
            val stamp = Timestamp(System.currentTimeMillis())
            val date = Date(stamp.time)
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val dat = dateFormat.format(date)
            when (type) {
                "START" -> vm.salesOrder.startTime = getCurrentTimeInMills().toString()
                "END" -> vm.salesOrder.endTime = getCurrentTimeInMills().toString()
            }
        }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (!isReceived) {
                        isReceived = true
                        isAlreadyLoading = false
                        response.data?.data?.let { it1 ->
                            when (it1.size == 1) {
                                true -> updateAdapter(it1 as ArrayList<VegaCoffeeSalesLots>)
                                else -> chooseOneLotDialog(it1)
                            }

                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    if (!isReceived) {
                        isReceived = true
                        isAlreadyLoading = false
                        showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    }
                }
            }
        }
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, currentFalg: String) {
        var list = ArrayList<String>()
        when (currentFalg) {

            STO_NO -> list = soNumbers
            QUALITY_GRADE -> {
                val gradeListFilter = mutableListOf<VegaQualitative>()
//                gradeListFilter.addAll(gradeList)
                materialQualityGradeList.forEach { qualityGrade ->
                    gradeListFilter.addAll(gradeList.filter {
                        it.charValue.split(" ").get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                    })
                }
                list = gradeListFilter.map { it.charValue.plus("-").plus(it.descValue) } as ArrayList<String>
            }
            CERTIFICATION -> {
                list = certicateFilterList.map { it.charValue } as ArrayList<String>
            }
        }
        customDialog =
            CustomCameroonSingleSelectDialog(
                title,
                isWh,
                list,
                currentFalg,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean, currentFalg: String) {
        customDialog?.dismiss()
        when (currentFalg) {
            STO_NO -> {

                vm.salesOrder.salesTempId = ""
                enableProceed(true)
                val selectedSales = salesOrderList.filter { it.salesOrderId == data } as ArrayList
                binding.tvstoValue.text = data
                vm.salesOrder.saleOrderId = data
                vm.getDispatchSalesItem(data, vm.salesOrder.salesType, vm.salesOrder.salesTempId)
                updateSODetails(selectedSales[0])
                binding.tvQualityGradeValue.text = ""
                binding.tvCertificationValue.text = ""

            }
            QUALITY_GRADE -> {
                //if( vm.mtnt.vendorCode?.isNotEmpty()==true) {
                grade = data.split("-")[0].trim()

                /*var certificate = vm.salesOrder.lotList.filter {
                    it.grade.toString().equals(data.split("-")[0])
                }.map { it.certificate }
                var crList = certicateList.filter { certificate.contains(it.charValue) }*/
                certicateFilterList = certicateList
                binding.tvQualityGradeValue.text = data.split("-")[0].trim()
                binding.tvQualityGradeValue.hideKeyboard()
                binding.tvCertificationValue.text = ""
                /*vm.localsales.certification = ""
                var gradeData = materialQualityGradeList.filter {
                    it.gradeCode == vm.localsales.qualityGrade?.split(" ")
                        ?.get(vm.localsales.qualityGrade?.split(" ")!!.size - 1) ?: 0
                }.get(0)
                // vm.mtnt.bagTareWeight=gradeData.tareWeight
                vm.localsales.bagType = gradeData.bagType*/
            }
            CERTIFICATION -> {
                //vm.localsales.certification = data
                binding.tvCertificationValue.text = data.trim()
                binding.tvCertificationValue.hideKeyboard()
                certificate = data.trim()
            }
        }
    }

    private fun updateSODetails(vegaCoffeeSalesOrderModel: VegaCameroonSalesOrderModel) {
        val selectedSales = vegaCoffeeSalesOrderModel.salesOrderList
        materialListItems.clear()
        materialListItems.addAll(selectedSales)
        if (selectedSales.size > 0) {
            binding.tvCustomerValue.text = selectedSales[0].soldToPartyName
            vm.salesOrder.customerId = selectedSales[0].soldToPartyCode
            vm.salesOrder.customerName = selectedSales[0].soldToPartyName
            vm.salesOrder.createdDate = selectedSales[0].createdDate
            vm.salesOrder.salesItem = selectedSales[0].salesItemNum
            enableSave(true)
            setUpMaterialAdapter(selectedSales)
            enableAddLotButtons()
        }
    }

    private fun setUpMaterialAdapter(selectedSales: List<materialList>) {
        materialList.clear()
        materialList.addAll(selectedSales.map { it.materialNumber.toString() })
        binding.rvMaterialList.setUpAdapter(
            selectedSales as MutableList<materialList>,
            R.layout.item_cameroon_sales_material_list,
            ItemCameroonSalesMaterialListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterial.text = it.materialDesc
                if (currentKey.split("_")[1].contains("NI")) {
                    materialCode = it.materialNumber.toString()
                    getQualityGrades()
                }
                bindItem.tvSoWeight.text =
                    it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ").plus(it.meins)
                when (it.meins) {
                    "KG" -> {
                        bindItem.tvSoWeight.text =
                            it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ")
                                .plus(it.meins)
                    }
                    "MT" -> {
                        bindItem.tvSoWeight.text =
                            convertMtToKg(it.openQuantity.toString()).toDouble()
                                .formatThreeDigits()
                                .plus(" ").plus("KG")
                    }
                }
            })
    }

    private fun chooseOneLotDialog(lots: List<VegaCoffeeSalesLots>) {
        val lotItem = lots.map {
            getString(com.olam.warehouse.login.R.string.lot_no).plus(" : ").plus(it.batchNumber).plus("\n")
                .plus(getString(R.string.weight)).plus(" : ").plus(it.weight).plus(" ").plus(it.unitOfMeasure)
                .plus("\n").plus(getString(R.string.st_location)).plus(" : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                updateAdapter(arrayListOf(lots[index]))
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(vegaCoffeeSalesLots: ArrayList<VegaCoffeeSalesLots>) {
        if (vm.salesOrder.salesTempId.isBlank()) {
            val randomDouble = "TMP".plus(Random.nextLong().toString())
            vm.salesOrder.salesTempId = randomDouble
        }
        vegaCoffeeSalesLots.forEachIndexed { index, it ->
            it.slPostion = if (it.slPostion == 0) index else it.slPostion
            it.saleOrderId = vm.salesOrder.saleOrderId
            it.salesTempId = vm.salesOrder.salesTempId
            it.salesType = vm.salesOrder.salesType
            if (!vm.lots.any { item -> item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode) }) vm.lots.add(
                it
            )
        }

        if (binding.rvLots.adapter?.itemCount == 0) getCurrentDate("START")
        vm.saveWeighBridgeAndLotDetails()
        binding.etEnterContainer.setText("")
        enableProceed(true)

    }

    private fun setUpAdapter(lineItems: MutableList<VegaCoffeeSalesLotWithbags>) {
        vm.lots = lineItems.map { it.salesLot } as java.util.ArrayList<VegaCoffeeSalesLots>
        if (vm.lots.size > 0) enableProceed(true)
        var lineItems1 = mutableListOf<VegaCoffeeSalesLotWithbags>()
        if (editLotId.isNotEmpty()) {
            binding.tvstoValue.isEnabled = false
            disableAddLotsButtons()
            lineItems1 =
                lineItems.filter {
                    it.salesLot.batchNumber.equals(editLotId) && it.salesLot.materialCode.equals(
                        editLotMaterial
                    )
                } as MutableList<VegaCoffeeSalesLotWithbags>
        } else {
            if (vm.currentTempId.isEmpty()) binding.tvstoValue.isEnabled = true
            enableAddLotButtons()
            lineItems1 = lineItems
        }
        val bagItems = lineItems1.map { it.lineItems }
        isMissedPallet = false
        bagItems.forEachIndexed { index, it ->
            val weighment = it.size
            val palletCount = if (weighment > 0 && it[0].noOfPallet?.isNotEmpty() == true) it[0].noOfPallet?.toInt() else 0
            if (palletCount != 0 && weighment != palletCount) isMissedPallet = true
        }
        binding.rvLots.setUpAdapter(
            lineItems1.sortedByDescending { it.salesLot.slPostion }.toMutableList(),
            R.layout.item_cameroon_sales_lot,
            ItemCameroonSalesLotBinding::inflate,
            { it, pos, bindItem ->
                val lot = it.salesLot

                when (lot.salesType) {
                    SALES_TYPE_WEIGHSCALE -> {
                        val bagList = it.lineItems.filter {
                            it.batchNumber.equals(lot.batchNumber) && it.materialCode.equals(lot.materialCode) && it.salesTempId.equals(
                                lot.salesTempId
                            )
                        }
                        if (bagList.size > 0) {
                            var editedWt = ""
                            if (lot.unitOfMeasure.equals("MT"))
                                editedWt = bagList.sumByDouble { it.netWeight.toDouble() }.div(1000)
                                    .formatThreeDigits()
                            else
                                editedWt = bagList.sumByDouble { it.netWeight.toDouble() }
                                    .formatThreeDigits()
                            lot.editedWeight = editedWt
                        } else lot.editedWeight = "0"
                        bindItem.flSalesLot.visible()
                        bindItem.flAnticipatedSales.gone()
                        bindItem.tvScaleLotValue.text = lot.batchNumber
                        bindItem.tvScaleGradeValue.text = lot.materialName
                        bindItem.tvStLocationValue.text = lot.storageLocationCode
                        // Display the Values in KG only
                        when (lot.unitOfMeasure) {
                            "KG" -> {
                                bindItem.tvScaleDispatchValue.text =
                                    lot.editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                                        .plus(lot.unitOfMeasure)
                                bindItem.tvScaleWeightValue.text =
                                    lot.weight?.toDouble()?.formatThreeDigits().plus(" ")
                                        .plus(lot.unitOfMeasure)
                            }
                            "MT" -> {
                                bindItem.tvScaleDispatchValue.text =
                                    bagList.sumByDouble { it.netWeight.toDouble() }
                                        .formatThreeDigits()
                                        .plus(" ").plus("KG")
                                bindItem.tvScaleWeightValue.text =
                                    convertMtToKg(lot.weight.toString()).toDouble()
                                        .formatThreeDigits()
                                        .plus(" ").plus("KG")
                            }
                        }

                        bindItem.tvAddWeight.setOnClickListener { view ->
                            callBack?.replaceFragment(ADD_WEIGHT, lot)

                        }
                        if (editLotId.isNotEmpty()) bindItem.ivScaleClose.gone() else bindItem.ivScaleClose.visible()
                        bindItem.ivScaleClose.setOnClickListener { view ->
                            showConformationDialog(
                                lot,
                                view
                            )
                        }
                        bindItem.cbScaleStorageLoss.isChecked = lot.endLotFlag ?: false
                        bindItem.clEndLot.setOnClickListener { view ->
                            lot.endLotFlag = !lot.endLotFlag!!
                            bindItem.cbScaleStorageLoss.isChecked = lot.endLotFlag ?: false
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                            vm.saveWeighBridgeAndLotDetails()
                        }
                        vm.lots.remove(lot)
                        vm.lots.add(lot)
                    }
                    SALES_TYPE_ANTICIPATED -> {
                        bindItem.flSalesLot.gone()
                        bindItem.flAnticipatedSales.visible()
                        bindItem.tvLotId.text = lot.batchNumber
                        bindItem.tvStLocationValue1.text = lot.storageLocationCode
                        bindItem.tvGradeValue.text = lot.materialName
                        bindItem.tvWeightValue.text =
                            lot.weight?.toDouble()?.formatThreeDigits().plus(" ")
                                .plus(lot.unitOfMeasure)
                        bindItem.cbSelectAll.isChecked = lot.isChecked ?: false
                        bindItem.etWeight.setText(lot.editedWeight)
                        bindItem.llSelectAll.setOnClickListener { view ->
                            lot.isChecked = !lot.isChecked!!
                            if (lot.isChecked!!) {
                                bindItem.cbSelectAll.isChecked = true
                                lot.editedWeight = lot.weight?.toDouble()?.formatThreeDigits()

                            } else {
                                bindItem.cbSelectAll.isChecked = false
                                lot.editedWeight = "0"
                            }
                            bindItem.etWeight.setText(lot.editedWeight)
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                        }
                        bindItem.cbStorageLoss.isChecked = lot.endLotFlag ?: false
                        bindItem.llEndLot.setOnClickListener { view ->
                            lot.endLotFlag = !lot.endLotFlag!!
                            bindItem.cbStorageLoss.isChecked = lot.endLotFlag ?: false
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                            vm.saveWeighBridgeAndLotDetails()
                        }


                        if (editLotId.isNotEmpty()) bindItem.ivClose.gone() else bindItem.ivClose.visible()
                        bindItem.ivClose.setOnClickListener { view ->
                            showConformationDialog(
                                lot,
                                view
                            )
                        }
                        bindItem.etWeight.onChange {
                            if (it.isNotEmpty()) {
                                val editVal: String?
                                val dot = it.get(0).toString()
                                editVal = if (dot == ".") {
                                    if (it.length == 1) "0.0" else "0".plus(it)
                                } else it

                                lot.editedWeight = editVal
                                val come: Int? =
                                    editVal.toDouble().compareTo(lot.weight?.toDouble() ?: 0.0)
                                if (come ?: 0 <= 0) {
                                    lot.isLowerWeight = true
                                    bindItem.etWeight.error = null
                                    vm.lots.forEach { item ->
                                        if (lot.batchNumber.equals(item.batchNumber) && lot.materialCode.equals(
                                                item.materialCode
                                            )
                                        ) item.editedWeight =
                                            editVal
                                    }
                                } else {
                                    lot.isLowerWeight = false
                                    bindItem.etWeight.error =
                                        bindItem.etWeight.context.getString(R.string.less_weight_error)
                                }
                            } else {
                                lot.editedWeight = "0"
                            }
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                        }
                    }
                }

            }, itemClick = {

            })
    }


    fun weightUpdated(item: VegaCoffeeSalesLots) {
    }

    private fun showConformationDialog(lot: VegaCoffeeSalesLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.lots.remove(lot)
                    vm.removeLotDetails(lot)
                    vm.saveWeighBridgeAndLotDetails()
                },
                { dismiss() })
        }
    }

    private fun showLotAlreadyExistDialog(isExist: Boolean) {
        MaterialDialog(requireContext()).show {
            message(if (isExist) R.string.already_added else R.string.details_lose)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
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

    private fun enableEnterLotId(enable: Boolean) {
        binding.etEnterContainer.isEnabled = ((vm.lots.isEmpty() || isMultipleLot) && enable)
        binding.clScan.isEnabled = ((vm.lots.isEmpty() || isMultipleLot) && enable)
    }

    private fun updateUIWithLocalData() {

    }

    private fun validateLotWeight(): Boolean {
        val selected = vm.lots.filter { !it.isLowerWeight }
        return selected.isEmpty()
    }

    private fun validateLotWeightWithMaterialWeight(): Boolean {
        var valueExceed = false
        var materialUOM = ""
        var lotsUOM = "KG"
        materialListItems.forEach { mat ->
            materialUOM = mat.meins.toString()
            val weight = vm.lots.filter { it.materialCode.equals(mat.materialNumber) }
            if (weight.size > 0) lotsUOM = weight[0].unitOfMeasure.toString()
            when {
                materialUOM.equals("MT") && lotsUOM.equals("MT") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    } > mat.openQuantity?.toDouble() ?: 0.0) valueExceed = true
                materialUOM.equals("MT") && lotsUOM.equals("KG") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    }.div(1000) > mat.openQuantity?.toDouble() ?: 0.0) valueExceed = true
                materialUOM.equals("KG") && lotsUOM.equals("MT") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    } > mat.openQuantity?.toDouble()?.div(1000) ?: 0.0) valueExceed = true
                materialUOM.equals("KG") && lotsUOM.equals("KG") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    } > mat.openQuantity?.toDouble() ?: 0.0) valueExceed = true
            }

        }
        return !valueExceed
    }

    private fun fetchLotDetails(lotId: String, materialList: List<String>, whId: String) {
        binding.etEnterContainer.hideKeyboard()
        showLoading()
        vm.getLotDetails(lotId, materialList, whId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    vm.currentScanLot = it
                    vm.validateLot(it)
                }
            }
        }
    }

    private fun removeScanLot(isRemove: Boolean) {
        binding.clScan.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.tvOr.visibility = if (isRemove) View.GONE else View.VISIBLE
    }

    private fun removeManualLot(isRemove: Boolean) {
        binding.etEnterContainer.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.btAdd.visibility = if (isRemove) View.GONE else View.VISIBLE
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

    private fun enableSave(enable: Boolean) {
        binding.btSave.isEnabled = enable
        when (enable) {
            true -> binding.btSave.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.blue_light)
            )
            /*ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )*/
            else -> binding.btSave.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.light_grey)
            )
        /*ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )*/
        }
    }

    fun updateLotList(lots: ArrayList<VegaCoffeeSalesLots>) {
        updateAdapter(lots)
    }


    private fun validateZeroWeight(): Boolean {
        val selected = vm.lots.filter { (it.editedWeight ?: "0").toDouble() <= 0 }
        return selected.isEmpty()
    }

    fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots) {
        editLotId = lotId.batchNumber
        editLotMaterial = lotId.materialCode
        vm.getDispatchSalesItem(model.saleOrderId, model.salesType, model.salesTempId)
    }

    fun getBack() {
        editLotId = ""
        editLotMaterial = ""
        vm.getDispatchSalesItem(vm.salesOrder.saleOrderId, vm.salesOrder.salesType, vm.salesOrder.salesTempId)
    }

    private fun getQualityGrades() {
        if (materialCode.length.equals(12))
            materialCode = "000000".plus(materialCode)
        else materialCode
        vm.getGrades(materialCode)
        vm.getCertification(materialCode)
        vm.getMaterialQualityGrades(materialCode)
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
                        isComplaint, isThirdParty,isFarmerLessTransaction ->  if(isComplaint) isCompliantMaterial = true else false
                    vm.getAllProduct()
                    vm.getPurchaseOrder()
                },
                { dismiss() },
                false)
        }

    }

}
