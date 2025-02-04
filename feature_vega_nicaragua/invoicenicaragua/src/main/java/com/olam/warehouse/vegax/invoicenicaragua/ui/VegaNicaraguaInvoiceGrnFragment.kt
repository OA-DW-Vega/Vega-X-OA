package com.olam.warehouse.vegax.invoicenicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.modal.Inventory
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.QualitativeParams
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnWithInventoryDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoiceGrnInventoryModal
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.GRN_LIST
import com.olam.warehouse.presentation.utils.UIUtils.TEMP_ID
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.utils.INVOICE_PRICE_CALCULATION_FRAG
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.databinding.FragmentVegaNicaraguaInvoiceGrnBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNicaraguaInvoiceGrnFragment : BaseFragment(), MultiSelectCallBackListener {

    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaNicaraguaInvoiceGrnBinding
    private var vendorData: VegaVendor = VegaVendor()
    private var grnDetails = GrnDetails()
    private var grnDetailsList = ArrayList<GrnDetails>()
    private var grnDetailsListFilter = ArrayList<GrnDetails>()
    private var qualityGradeWithDescList = ArrayList<QualitativeParams>()
    private val vm: VegaNicaraguaInvoiceViewModel by viewModel()
    private var tempId = ""
    private var wbid = ""
    private var basePrice = ""
    private var inventory = Inventory()
    private var inventoryInfo = VegaNicaraguaInvoiceGrnInventoryModal()
    private var inventoryList = ArrayList<Inventory>()
    private var selectedGrnCount = ArrayList<GrnDetails>()
    private var offlineTmpIds = ArrayList<String>()
    private var offlineGrns = ArrayList<String>()
    private var customDialog: VegaNicInvoiceMultiSelectDialog? = null

    interface CallBack {
        fun replaceFragment(moveFrag: String)
        fun replaceFragment(
            moveFrag: String,
            data: Bundle
        )
    }

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaInvoiceGrnFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_invoice_grn

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaInvoiceGrnBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("invoicenicaragua/ui/VegaNicaraguaInvoiceGrnFragment")
            .title("Invoice  GRN list")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        arguments?.let {
            vendorData = it.getParcelable(UIUtils.VENDOR_DATA)!!
            val data = it.getParcelable(TEMP_ID) ?: GrnDetails()
            wbid = data.wbid
            tempId = data.tempId.toString()
            basePrice = data.basePrice.toString()
        }
    }

    private fun initUI() {
        binding.spMaterial.hideKeyboard()

        updateMandatory()
        getQualityGradeDesc()

        binding.btnProceed.setOnClickListener {
            validateFields()
        }

        binding.tvGrn.setOnClickListener {
            updateGrnInfoUI(grnDetailsListFilter)

        }
        vm.invoiceOffline.observe(viewLifecycleOwner, Observer { updateOfflineData(it) })
        vm.getInvoiceOfflineData()
    }

    private fun updateOfflineData(it: List<VegaNicaraguaInvoiceDetails>?) {
        it?.map { it.tempId }?.let { it1 -> offlineTmpIds.addAll(it1) }
        offlineGrns.clear()
        offlineTmpIds.forEach { tmpwbid ->
            val grnItemJson = PreferenceHelper.get(tmpwbid, "")
            val grnList = Gson().fromJson<List<GrnDetails>>(grnItemJson)
            offlineGrns.addAll(grnList.map { it.grnNumber.toString() })
        }
    }

    private fun getQualityGradeDesc() {
        vm.getQualityGradeDescList.observe(this, androidx.lifecycle.Observer {
            qualityGradeWithDescList = ArrayList(it)
            getGrnInfo()
        })

        vm.getQualityGradesListWithDesc()
    }
    private fun getGrnInfo() {
        if (!vendorData.vendorCode.isNullOrEmpty()) {
            if (AppUtils.isOnline()) {
                vm.grnDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
                    response?.let {
                        when (it.status) {
                            Resource.Status.SUCCESS -> {
                                when (it.data?.success) {
                                    true -> {
                                        if (it.data?.data!!.size > 0) {
                                            val dataValue = it.data?.data!![0].grnDetails
                                            updateInventoryInfoUI(dataValue)
                                        } else {
                                            showDialog(getString(R.string.grn_info_not_found))
                                        }
                                    }
                                    else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                                }
                                hideLoading()
                            }
                            Resource.Status.LOADING -> showLoading()
                            Resource.Status.ERROR -> {
                                hideLoading()
                                showDialog(requireContext().resources.getString(R.string.vendor_info_not_found))
                            }
                        }
                    }
                })
                vm.getGrnDetailsByVendor(vendorData.vendorCode)
            } else {
                vm.grnDetailsOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    if (it.size > 0) {
                        updateInventoryInfoUIOffline(it)
                    } else {
                        showDialog(getString(R.string.grn_info_not_found))
                    }
                })
                vm.getGrnDetailsByVendorOffline("000".plus(vendorData.vendorCode))
            }

        } else {
            showDialog(requireContext().resources.getString(R.string.vendor_info_not_found))
        }


    }

    private fun updateInventoryInfoUIOffline(data: List<VegaNicaraguaGrnWithInventoryDetails>?) {
        grnDetailsList = ArrayList<GrnDetails>()
        val item = data?.filter { it.grn.grnType?.contains("ptbf") == true }
        item?.forEach {
            val grnDetail = GrnDetails()
            grnDetail.item = it.grn.item
            grnDetail.wbid = it.grn.wbid
            grnDetail.charg = it.grn.charg
            grnDetail.batchNumber = it.grn.batchNumber
            grnDetail.materialName = it.grn.materialName
            grnDetail.materialNumber = it.grn.materialNumber
            grnDetail.supplierName = it.grn.supplierName
            grnDetail.supplierCode = it.grn.supplierCode
            grnDetail.grnNumber = it.grn.grnNumber!!
            grnDetail.werks = it.grn.werks
            grnDetail.grn = it.grn.grn
            grnDetail.discount = it.grn.discount
            grnDetail.pchar = it.grn.pchar
            grnDetail.unitPrice = it.grn.unitPrice
            grnDetail.totalPrice = it.grn.totalPrice
            grnDetail.kpein = it.grn.kpein
            grnDetail.grnQty = it.grn.grnQty
            grnDetail.grnType = it.grn.grnType
            grnDetail.discountWeight = it.grn.discountWeight
            grnDetail.meins = it.grn.meins
            grnDetail.year = it.grn.year
            grnDetail.unitsOfMeasure = it.grn.unitsOfMeasure
            grnDetail.waers = it.grn.waers
            grnDetail.qchar = it.grn.qchar
            grnDetail.werksName = it.grn.werksName
            grnDetail.plantDesc = it.grn.plantDesc
            grnDetail.bprme = it.grn.bprme
            grnDetail.matkl = it.grn.matkl
            val invenData =
                it.inventory?.filter { it1 -> it1.materialCode.contains(it.grn.materialNumber.toString()) }?.toList()
            grnDetail.inventoryDetail =
                if (invenData?.isNotEmpty() == true) invenData.filter { it.qualityGrade?.isNotEmpty() == true }
                    .get(0) else VegaNicaraguaGRNInventoryDetails()

            grnDetailsList.add(grnDetail)

        }
        updateQualityMaterialInfo()
    }

    private fun updateInventoryInfoUI(data: List<GrnDetails>) {
        inventoryList = ArrayList<Inventory>()
        grnDetailsList = ArrayList<GrnDetails>()
        val grnData = data.filter { it.grnType?.contains("ptbf") == true }
        val inventoryInit = Inventory()
        inventoryInit.qualityGrade = getString(R.string.select_grn)
        inventoryList.add(inventoryInit)

        grnData.forEach {
            val grnDetail = GrnDetails()
            grnDetail.item = it.item
            grnDetail.wbid = it.wbid
            grnDetail.charg = it.charg
            grnDetail.batchNumber = it.batchNumber
            grnDetail.unitsOfMeasure = it.unitsOfMeasure
            grnDetail.year = it.year
            grnDetail.materialName = it.materialName
            grnDetail.materialNumber = it.materialNumber
            grnDetail.supplierName = it.supplierName
            grnDetail.supplierCode = it.supplierCode
            grnDetail.grnNumber = it.grnNumber!!
            grnDetail.werks = it.werks
            grnDetail.grn = it.grn
            grnDetail.discount = it.discount
            grnDetail.pchar = it.pchar
            grnDetail.unitPrice = it.unitPrice
            grnDetail.totalPrice = it.totalPrice
            grnDetail.kpein = it.kpein
            grnDetail.grnQty = it.grnQty
            grnDetail.grnType = it.grnType
            grnDetail.discountWeight = it.discountWeight
            grnDetail.meins = it.meins
            grnDetail.waers = it.waers
            grnDetail.qchar = it.qchar
            grnDetail.werksName = it.werksName
            grnDetail.plantDesc = it.plantDesc
            grnDetail.bprme = it.bprme
            grnDetail.matkl = it.matkl
            grnDetail.inventoryDetail =
                getBagParameters(prepareGrnInventoryDetails(it.inventoryRes?.inventory?.get(0)!!))

            grnDetailsList.add(grnDetail)

            inventoryList.add(it.inventoryRes?.inventory!!.get(0))
        }
        updateQualityMaterialInfo()

    }

    fun updateQualityMaterialInfo() {
        val grnData = ArrayList<GrnDetails>()
        val item = GrnDetails()
        item.materialName = getString(R.string.select_material)
        grnData.add(item)
        grnData.addAll(grnDetailsList)
        val materialData = grnData.distinctBy { it.materialNumber }.map { data -> data.materialName }
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, materialData)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spMaterial.adapter = locationAdapter
        var defaultposition = 0
        binding.spMaterial.setSelection(defaultposition)
        binding.spMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    val filterGrnData = grnDetailsList.filter { it.materialName.equals(materialData[position]) }
                    updateQualityGradeInfo(filterGrnData)
                } else {
                }
            }
        }
    }

    fun updateQualityGradeInfo(filterGrnData: List<GrnDetails>) {
        val grnData = ArrayList<GrnDetails>()
        val item = GrnDetails()
        val invenItem = VegaNicaraguaGRNInventoryDetails()
        invenItem.qualityGrade = getString(R.string.select_grade)
        item.inventoryDetail = invenItem
        grnData.add(item)

        filterGrnData.forEach { grnDetails ->
            qualityGradeWithDescList.forEach {
                if(grnDetails.inventoryDetail!!.qualityGrade.equals(it.paramName,true))
                {
                   grnDetails.qualityGradeDesc=it.paramDesc
                }
            }
        }

        grnData.addAll(filterGrnData)

        val gradeData =
            grnData.distinctBy { it.inventoryDetail?.qualityGrade }
                .filter { it.inventoryDetail?.qualityGrade?.isNotEmpty() == true }.map {
                    data ->
                if(data.inventoryDetail?.qualityGrade.equals( getString(R.string.select_grade))) {
                    data.inventoryDetail?.qualityGrade
                }else
                {
                    data.inventoryDetail?.qualityGrade + "-" + data.qualityGradeDesc
                }
            }

        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, gradeData)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spGrade.adapter = locationAdapter
        var defaultposition = 0
        binding.spGrade.setSelection(defaultposition)
        binding.spGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    binding.tvCertificate.gone()
                    binding.flCertification.gone()
                    val filterGradeGrnData =
                        filterGrnData.filter {
                            it.inventoryDetail?.qualityGrade?.replace("\\s+".toRegex(), " ")
                                .equals(gradeData[position]!!.split("-")[0].replace("\\s+".toRegex(), " "))
                        }
                    val gradeData = filterGradeGrnData.distinctBy { it.inventoryDetail?.certification }
                        .map { data -> data.inventoryDetail?.certification }
                    grnDetailsListFilter = ArrayList<GrnDetails>()
                    grnDetailsListFilter.addAll(filterGradeGrnData)
                    if (gradeData.any { it?.isNotEmpty() == true }) updateCertificationInfo(filterGradeGrnData) else {
//                        updateGrnInfoUI(filterGradeGrnData)
                        grnDetailsListFilter = ArrayList<GrnDetails>()
                        grnDetailsListFilter.addAll(filterGradeGrnData)
                    }
                } else {
                }
            }
        }
    }

    fun updateCertificationInfo(filterGradeGrnData: List<GrnDetails>) {
        binding.tvCertificate.visible()
        binding.flCertification.visible()
        val grnData = ArrayList<GrnDetails>()
        val item = GrnDetails()
        val invenItem = VegaNicaraguaGRNInventoryDetails()
        invenItem.certification = getString(R.string.select_certification)
        item.inventoryDetail = invenItem
        grnData.add(item)
        grnData.addAll(filterGradeGrnData)
        val certificationData =
            grnData.distinctBy { it.inventoryDetail?.certification }
                .filter { it.inventoryDetail?.certification?.isNotEmpty() == true }
                .map { data -> data.inventoryDetail?.certification }
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, certificationData)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spCertificate.adapter = locationAdapter
        var defaultposition = 0
        binding.spCertificate.setSelection(defaultposition)
        binding.spCertificate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    val filtercertificationGrnData =
                        filterGradeGrnData.filter { it.inventoryDetail?.certification.equals(certificationData[position]) }
//                    updateGrnInfoUI(filtercertificationGrnData)
                    grnDetailsListFilter = ArrayList<GrnDetails>()
                    grnDetailsListFilter.addAll(filtercertificationGrnData)
                } else {

                }
            }
        }
    }

    private fun getBagParameters(list: ArrayList<VegaNicaraguaGRNInventoryDetails>): VegaNicaraguaGRNInventoryDetails {
        var inventoryDetail = VegaNicaraguaGRNInventoryDetails()
        if (list.size > 0) {
            inventoryDetail = list[0]
            var bagsQuantity: String? = "0"
            var tareWeight: String? = "0"
            var humidity: String? = "0"
            var grossWeight: String? = "0"
            var qualityGrade: String? = ""
            var certification: String? = ""
            var exportable: String? = "0"

            list.forEach {
                it.value = it.value?.replace(",", "")
                qualityGrade = it.qualityGrade

                certification = it.certification

                if (it.qcName.equals("NISACOS")) {
                    bagsQuantity = it.value
                } else if (it.qcName.equals("NIPESBRT")) {
                    grossWeight = it.value
                } else if (it.qcName.equals("NIPESTAR")) {
                    tareWeight = it.value
                } else if (it.qcName.equals("NIRM0003")) {
                    humidity = it.value
                } else if (it.qcName.equals("NIRM0010")) {
                    exportable = it.value?.replace("%", "")?.trim()
                }

            }

            /*inventoryInfo.bagCount = bagsQuantity
         inventoryInfo.tareWeight = tareWeight
         inventoryInfo.grossWeight = grossWeight
         inventoryInfo.humidity = humidity
         inventoryInfo.qualityGrade = qualityGrade
         inventoryInfo.plantId = getPlantDetails().plantId
         inventoryInfo.plantName = getPlantDetails().plantName
         inventoryInfo.exportable = exportable
         inventoryInfo.certification = certification*/
            inventoryDetail.bagCount = if (bagsQuantity?.isNotEmpty() == true) bagsQuantity else "0"
            inventoryDetail.tareWeight = if (tareWeight?.isNotEmpty() == true) tareWeight else "0"
            inventoryDetail.grossWeight = if (grossWeight?.isNotEmpty() == true) grossWeight else "0"
            inventoryDetail.humidity = if (humidity?.isNotEmpty() == true) humidity else "0"
            inventoryDetail.qualityGrade = qualityGrade
            inventoryDetail.plantId = getPlantDetails().plantId
            inventoryDetail.plantName = getPlantDetails().plantName
            inventoryDetail.exportable = if (exportable?.isNotEmpty() == true) exportable else "0"
            inventoryDetail.certification = certification
        }
        return inventoryDetail
    }

    private fun prepareGrnInventoryDetails(inventory: Inventory): ArrayList<VegaNicaraguaGRNInventoryDetails> {

        val vegaInventoryDetails = ArrayList<VegaNicaraguaGRNInventoryDetails>()

        inventory.let { inventory ->
            inventory.inventoryQC?.forEach {

                val inventoryDetails = VegaNicaraguaGRNInventoryDetails()
                inventoryDetails.id = inventory.id

                inventory.warehouseLocation?.let { wareHouseLocation ->
                    inventoryDetails.warehouseId = wareHouseLocation.warehouse!!.warehouseId
                    inventoryDetails.warehouseName = wareHouseLocation.warehouse!!.warehouseName
                    inventoryDetails.plantId = wareHouseLocation.warehouse!!.plant!!.plantCode!!
                    inventoryDetails.plantName = wareHouseLocation.warehouse!!.plant!!.plantName!!
                    inventoryDetails.procureLocationCode = wareHouseLocation.procureLocationCode
                    inventoryDetails.procureLocationName = wareHouseLocation.procureLocationName

                }

                inventoryDetails.materialCode = inventory.materialCode!!
                inventoryDetails.lotId = inventory.lotId!!
                inventoryDetails.stockQty = inventory.stockQty
                inventoryDetails.grnDate = inventory.grnDate
                inventoryDetails.createdDateTime = inventory.createdDateTime
                inventoryDetails.createdBy = inventory.createdBy
                inventoryDetails.updatedAt = inventory.updatedAt
                inventoryDetails.updatedBy = inventory.updatedBy
                inventoryDetails.qualityGrade = inventory.qualityGrade
                inventoryDetails.qualityGrade = inventory.gradeDesc
                inventoryDetails.certification = inventory.certification

                inventoryDetails.qcId = it.qcId
                inventoryDetails.qcName = it.qcName!!
                inventoryDetails.dataType = it.dataType
                inventoryDetails.numberDecimals = it.numberDecimals
                inventoryDetails.entryObligatory = it.entryObligatory
                inventoryDetails.value = it.value

                vegaInventoryDetails.add(inventoryDetails)

            }

        }
        return vegaInventoryDetails
    }

    private fun updateGrnInfoUI(grnData: List<GrnDetails>) {

        grnDetailsListFilter = ArrayList<GrnDetails>()
        grnDetailsListFilter.addAll(grnData)
        val grnData1 =
            grnDetailsListFilter.distinctBy { it.grnNumber }.map { data -> data.grnNumber.toString() }.toList()
        showMultiSelectDialog(grnDetailsListFilter)
        /*val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, grnData1)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spGrn.adapter = locationAdapter
        var defaultposition = 0
        binding.spGrn.setSelection(defaultposition)
        binding.spGrn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                grnDetailsList.forEachIndexed { index, grnDetail ->
                    if (position > 0) {
                        binding.grnInfo.visibility = View.VISIBLE
                        if (grnData1[position] == grnDetail.grnNumber) {
                            grnDetails = grnDetail
                            binding.tvGrnMaterial.text = grnDetail.materialName
                            binding.tvGrnNetWeight.text = grnDetail.grnQty!!.trim() + " KG(S)"
                            binding.tvGrnNetPrice.text =
                                getString(R.string.c_doller) + " " + grnDetail.totalPrice!!.trim()

                            inventoryInfo.netWeight = grnDetail.grnQty
                            inventoryInfo.netPrice = grnDetail.totalPrice

                            if (grnDetail.inventoryDetail?.qualityGrade?.isNotEmpty()==true) {
                                binding.tvGrnQualityGrade.text = grnDetail.inventoryDetail?.qualityGrade
                            }
                            if (grnDetail.inventoryDetail?.certification?.isNotEmpty()==true) {
                                binding.tvGrnCertification.text = grnDetail.inventoryDetail?.certification
                            }

                        }
                    } else {
                        grnDetails = GrnDetails()
                        binding.grnInfo.visibility = View.GONE
                    }
                }
            }
        }
        if (wbid.isNotEmpty()) binding.spGrn.setSelection(grnData1.indexOf(wbid))*/
        /*val grnDetailsSelectedList = ArrayList<GrnDetails>()
        MaterialDialog(requireContext()).show {
            listItemsMultiChoice(items = grnData1) { dialog, index, items ->

                items.forEach { text ->
                    val data = grnDetailsListFilter.filter { it.grnNumber?.equals(text) == true }
                    grnDetailsSelectedList.addAll(data)
                }
                binding.tvGrn.text = items.toString().replace("[", "").replace("]", "")
                binding.grnInfo.visibility = View.VISIBLE
                val totalGrnQty = grnDetailsSelectedList.sumByDouble { it.grnQty?.toDouble() ?: 0.0 }
                val totalGrnPrice = grnDetailsSelectedList.sumByDouble { it.totalPrice?.toDouble() ?: 0.0 }
                val grnDetail = grnDetailsSelectedList[0]
                grnDetails = grnDetailsSelectedList[0]
                binding.tvGrnMaterial.text = grnDetail.materialName
                binding.tvGrnNetWeight.text = totalGrnQty.formatThreeDigits() + " KG(S)"
                binding.tvGrnNetPrice.text =
                    getString(R.string.c_doller) + " " + totalGrnPrice.formatThreeDigits()
                grnDetail.toatlGrnQty = totalGrnQty.formatThreeDigits()
                grnDetail.totalGrnPrice = totalGrnPrice.formatThreeDigits()
                inventoryInfo.netWeight = totalGrnQty.formatThreeDigits()
                inventoryInfo.netPrice = totalGrnPrice.formatThreeDigits()
                inventoryInfo.bagCount =
                    grnDetailsSelectedList.sumBy { it.inventoryDetail?.bagCount?.toInt() ?: 0 }.toString()
                inventoryInfo.tareWeight = grnDetailsSelectedList.sumByDouble {
                    it.inventoryDetail?.tareWeight?.split(" ")?.get(0)?.toDouble() ?: 0.0
                }
                    .formatThreeDigits()
                inventoryInfo.grossWeight = grnDetailsSelectedList.sumByDouble {
                    it.inventoryDetail?.grossWeight?.split(" ")?.get(0)?.toDouble() ?: 0.0
                }.formatThreeDigits()
                inventoryInfo.humidity =
                    grnDetailsSelectedList.sumByDouble {
                        it.inventoryDetail?.humidity?.split(" ")?.get(0)?.toDouble() ?: 0.0
                    }
                        .formatThreeDigits()
                inventoryInfo.qualityGrade = grnDetail.inventoryDetail?.qualityGrade
                inventoryInfo.plantId = getPlantDetails().plantId
                inventoryInfo.plantName = getPlantDetails().plantName
                inventoryInfo.exportable =
                    grnDetailsSelectedList.sumByDouble { it.inventoryDetail?.exportable?.toDouble() ?: 0.0 }
                        .formatThreeDigits()
                inventoryInfo.certification = grnDetail.inventoryDetail?.certification

                if (grnDetail.inventoryDetail?.qualityGrade?.isNotEmpty() == true) {
                    binding.tvGrnQualityGrade.text = grnDetail.inventoryDetail?.qualityGrade
                }
                if (grnDetail.inventoryDetail?.certification?.isNotEmpty() == true) {
                    binding.tvGrnCertification.text = grnDetail.inventoryDetail?.certification
                }
                selectedGrnCount = grnDetailsSelectedList
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.login.R.string.ok),
                    true
                )
            ) { dismiss() }

        }*/
    }

    private fun showMultiSelectDialog(grListFilter: ArrayList<GrnDetails>) {
        grListFilter.forEach {
            if (offlineGrns.contains(it.grnNumber)) it.isGrnInOffline = true
        }
        customDialog =
            VegaNicInvoiceMultiSelectDialog(
                grListFilter,
                activity!!,
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun validateFields() {
        when {

            (grnDetailsList.size <= 0) -> {
                showSnack(requireContext().resources.getString(R.string.grn_info_not_found))
            }

            (binding.tvGrn.text.isEmpty()) -> {
                showSnack(requireContext().resources.getString(R.string.grn_selection_error_info))
            }

            (binding.spMaterial.selectedItemPosition == 0) -> {
                showSnack(requireContext().resources.getString(R.string.select_material_error))
            }

            (binding.spGrade.selectedItemPosition == 0) -> {
                showSnack(requireContext().resources.getString(R.string.select_grade_error))
            }

            else -> {
                var cerList = ArrayList<String>()
                selectedGrnCount.forEach {
                    if (cerList.size > 0) {
                        if (!it.inventoryDetail?.certification.isNullOrEmpty()) {
                            if (!cerList.contains(it.inventoryDetail?.certification)) {
                                showSnack(requireContext().resources.getString(R.string.select_same_cert_error))
                                return
                            }
                        }
                    } else {
                        if (!it.inventoryDetail?.certification.isNullOrEmpty()) cerList.add(it.inventoryDetail?.certification.toString())
                    }
                }

                grnDetails.tempId = tempId
                grnDetails.basePrice = basePrice
                grnDetails.qualityGrade = binding.tvGrnQualityGrade.text.toString()
                var data = Bundle()
                data.putParcelable(UIUtils.VENDOR_DATA, vendorData)
                data.putParcelable(UIUtils.GRN_DATA, grnDetails)
                data.putParcelable(UIUtils.BAGS_DATA, inventoryInfo)
                data.putParcelableArrayList(GRN_LIST, selectedGrnCount)

                callBack?.replaceFragment(
                    INVOICE_PRICE_CALCULATION_FRAG, data
                )
            }
        }
    }

    fun getPlantDetails(): Plant {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    }

    private fun updateMandatory() {
        binding.tvQualityGrade.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.quality_grade)) { mandatoryStars() } }
        binding.tvMaterial.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.material)) { mandatoryStars() } }
        binding.tvLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.grn)) { mandatoryStars() } }
    }

    override fun updateSelectedGrn(selectedList: List<GrnDetails>) {
        val grnDetailsSelectedList = ArrayList<GrnDetails>()
        grnDetailsSelectedList.addAll(selectedList)
        val items = grnDetailsSelectedList.map { it.grnNumber }
        binding.tvGrn.text = items.toString().replace("[", "").replace("]", "")
        binding.grnInfo.visibility = View.VISIBLE
        val totalGrnQty = grnDetailsSelectedList.sumByDouble { it.grnQty?.toDouble() ?: 0.0 }
        val totalGrnPrice = grnDetailsSelectedList.sumByDouble { it.totalPrice?.toDouble() ?: 0.0 }
        val grnDetail = grnDetailsSelectedList[0]
        grnDetails = grnDetailsSelectedList[0]
        binding.tvGrnMaterial.text = grnDetail.materialName
        binding.tvGrnNetWeight.text = totalGrnQty.formatThreeDigits() + " KG(S)"
        binding.tvGrnNetPrice.text =
            getString(R.string.c_doller) + " " + totalGrnPrice.formatThreeDigits()
        grnDetail.toatlGrnQty = totalGrnQty.formatThreeDigits()
        grnDetail.totalGrnPrice = totalGrnPrice.formatThreeDigits()
        inventoryInfo.netWeight = totalGrnQty.formatThreeDigits()
        inventoryInfo.netPrice = totalGrnPrice.formatThreeDigits()
        inventoryInfo.bagCount =
            grnDetailsSelectedList.sumBy { it.inventoryDetail?.bagCount?.toInt() ?: 0 }.toString()
        inventoryInfo.tareWeight = grnDetailsSelectedList.sumByDouble {
            it.inventoryDetail?.tareWeight?.split(" ")?.get(0)?.toDouble() ?: 0.0
        }
            .formatThreeDigits()
        inventoryInfo.grossWeight = grnDetailsSelectedList.sumByDouble {
            it.inventoryDetail?.grossWeight?.split(" ")?.get(0)?.toDouble() ?: 0.0
        }.formatThreeDigits()
        inventoryInfo.humidity =
            grnDetailsSelectedList.sumByDouble {
                it.inventoryDetail?.humidity?.split(" ")?.get(0)?.toDouble() ?: 0.0
            }
                .formatThreeDigits()
        inventoryInfo.qualityGrade = grnDetail.inventoryDetail?.qualityGrade
        inventoryInfo.plantId = getPlantDetails().plantId
        inventoryInfo.plantName = getPlantDetails().plantName
        inventoryInfo.exportable =
            grnDetailsSelectedList.sumByDouble { it.inventoryDetail?.exportable?.toDouble() ?: 0.0 }
                .formatThreeDigits()
        inventoryInfo.certification = grnDetail.inventoryDetail?.certification

        if (grnDetail.inventoryDetail?.qualityGrade?.isNotEmpty() == true) {
            binding.tvGrnQualityGrade.text = grnDetail.inventoryDetail?.qualityGrade
        }
        if (grnDetail.inventoryDetail?.certification?.isNotEmpty() == true) {
            binding.tvGrnCertification.text = grnDetail.inventoryDetail?.certification
        }
        selectedGrnCount = grnDetailsSelectedList
    }

}
