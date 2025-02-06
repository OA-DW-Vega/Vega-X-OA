package com.olam.warehouse.vegax.mtntnicaragua.ui.reprint

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.databinding.VegaNicaraguaMtnrTallysheetPrintBinding

import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.showErrorDialog
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaMtnrReprintList
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtnrReprintModel
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicTicketReprintBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaMtntReprintBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemVegaNicaraguaMtnrReprintBinding
import com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntViewModel
import com.olam.warehouse.vegax.mtntnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class VegaNicMtnrReprintFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_mtnt_reprint
    private lateinit var binding: FragmentVegaNicaraguaMtntReprintBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var callBack: VegaNicMtntReprintFragment.CallBack? = null
    private var receivingData = VegaReceiving()
    private var netWeight: String? = ""
    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var printableList = arrayListOf<VegaNicMtnrReprintModel>()
    private var printable = arrayListOf<VegaMtnrReprintList>()
    private var isMultipleAdd = false
    private var mtnrList = mutableListOf<VegaReceiving>()
    private var vendorList = arrayListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var tallyPrintKeys = ArrayList<String>()
    private var printticketArr: MutableList<VegaMtnrReprintList> = ArrayList()
    private var filteredprintticketArr = arrayListOf<VegaMtnrReprintList>()
    private var moduleno = ""
    private var batchno = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }
    companion object {
        fun newInstance() = VegaNicMtnrReprintFragment().putArgs {
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNicaraguaMtntReprintBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvGrnHeading.text = getString(R.string.re_print_mtnr)
    }
    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }
    private fun initUI() {
        try {
            vm.mtnrReprintList.observe(viewLifecycleOwner, Observer { updatePrintListdetails(it) })
            vm.getMtnrReprintList()
            vm.mtnrPrintDetails.observe(viewLifecycleOwner) {
                tallyPrintKeys.clear()
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        hideLoading()
                        if (it.data != null && it.data!!.data != null && it.data!!.data.isNotEmpty()) {
                            tallyPrintKeys.add(it.data!!.data)
                            showConfirmDialog()
                        } else
                            showErrorDialog(requireContext(), getString(R.string.no_data_found))
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(
                            requireActivity(),
                            getString(R.string.no_data_found)
                        )
                    }
                }
            }
            binding.tvPrint.setOnClickListener {
                vm.getMtnrPrintDetails(moduleno, batchno, MTNR_RECEIPT)
            }
        //--------------- old reprint model

          /*  vm.getProducts()
            vm.productList.observe(viewLifecycleOwner) {
                materialList.addAll(it)
                vm.getSuppliers()
            }
            vm.suppplier.observe(viewLifecycleOwner) {
                it?.let {
                    vendorList = it as ArrayList<VegaVendor>
                    vm.getGrnPrintDetails()
                }
            }
            vm.grnPrintDetails.observe(viewLifecycleOwner, { updateUi(it) })
            vm.bagItems.observeOnce(viewLifecycleOwner, Observer {
                weighDetails = it
                val printModel = VegaNicMtnrReprintModel()
                printModel.weighBridgeId = receivingData.weighBridgeId
                printModel.receivingData = receivingData
                printModel.weighDetails = weighDetails
                printableList.add(printModel)
            })
            binding.tvPrint.setOnClickListener { showConfirmDialog() }
*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun updatePrintListdetails(it: Resource<GenericReqAndResp<List<VegaMtnrReprintList>>>) {
        when (it.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (it.data?.data?.isNotEmpty()!!) {
                    printticketArr.clear()
                    val response = it.data?.data
                    printticketArr =
                        response?.filter { it.fileType.equals("Receipt") }
                            ?.sortedByDescending { desc -> desc.id } as MutableList<VegaMtnrReprintList>
                    setAdapter(printticketArr)
                } else {
                    binding.rvTransaction.gone()
                    binding.tvPrint.gone()
                    binding.tvNoData.visible()

                }

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                binding.rvTransaction.gone()
                binding.tvPrint.gone()
                binding.tvNoData.visible()
                showErrorDialogWithFAQLink(requireContext(), it.error.toString())
            }
            else -> {}
        }

    }
    private fun setAdapter(itemList: MutableList<VegaMtnrReprintList>) {
        if (itemList.isNotEmpty()) {
            if (itemList.size > 0) {
                binding.rvTransaction.visible()
                binding.tvPrint.visible()
                binding.tvNoData.gone()
            } else {
                binding.rvTransaction.gone()
                binding.tvPrint.gone()
                binding.tvNoData.visible()
            }
        } else {
            binding.rvTransaction.gone()
            binding.tvPrint.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUpAdapter(
            itemList,
            R.layout.fragment_vega_nic_ticket_reprint,
            FragmentVegaNicTicketReprintBinding::inflate,
            { it, pos, bindItem ->
                val oldpos = itemList.indexOf(itemList.find { it.isProgress == true })
                bindItem.tvGrnTempIdValue.text = it.moduleNo
                bindItem.tvGrnTempId.text = getString(R.string.grn_heading)
                bindItem.tvBatchNoValue.text = it.transactionNo
               // bindItem.tvDateValue.text = DateUtils.getDate(it.date.toLong(), "dd/MM/YYYY")
                bindItem.tvDateValue.text = DateUtils.getUTCDateTime(it.date, context = requireContext())
                bindItem.tvVendorValue.text = it.materialName
                bindItem.cbMtntItem.isChecked = it.isProgress
                bindItem.cvMtntItem.setOnClickListener { view ->
                    val oldpos = itemList.indexOf(itemList.find { it.isProgress == true })
                    it.isProgress = !it.isProgress
                    binding.tvPrint.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
                    binding.tvPrint.isEnabled = true
                    bindItem.cbMtntItem.isChecked = it.isProgress
                    if (it.isProgress) {
                        setPrintDatalist(it)
                        removeCheckedlist(pos, itemList, oldpos)
                    }
                }
            },
            itemClick = {
            })
    }
    private fun removeCheckedlist(pos: Int, list: MutableList<VegaMtnrReprintList>, oldpos: Int) {
        list.forEach { it.isProgress = false }
        list[pos].isProgress = true
        binding.rvTransaction.adapter?.notifyItemChanged(oldpos)
        binding.rvTransaction.adapter?.notifyItemChanged(pos)
        //setAdapter(list)
    }
    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    //generateBitMapKey()
                    DoAsync {
                        if (tallyPrintKeys.size > 0)
                            runOnUiThread {
                                showPreviewDialog()
                            }
                    }.execute()
                },
                { dismiss() })
        }
    }
    private fun showPreviewDialog() {
        showCustomLoading()
        DoAsync {
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(requireContext(), WifiMainActivity::class.java))
            }
        }.execute()

    }
    private fun setPrintDatalist(receivingData: VegaMtnrReprintList) {
        val printModel = VegaMtnrReprintList()
        printable.clear()
        printModel.transactionNo = receivingData.transactionNo
        printModel.materialName = receivingData.materialName

        printModel.date = DateUtils.getDate(receivingData.date.toLong(), "dd/MM/YYYY").toString()

        printable.add(printModel)
        moduleno = receivingData.id.toString()
        batchno = receivingData.transactionNo ?: ""

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(R.string.search_ticket)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setAdapter(printticketArr)
                        } else {
                            filteredprintticketArr.clear()
                            printticketArr.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.moduleNo!!.contains(text,true)) {
                                        filteredprintticketArr.add(qtyWb)
                                    }
                                }
                            }

                            setAdapter(filteredprintticketArr)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    /// old reprint model
    private fun generateBitMapKey() {
        showCustomLoading()
        DoAsync {
            val view =
                LayoutInflater.from(requireContext())
                    .inflate(
                        com.olam.warehouse.login.R.layout.vega_nicaragua_mtnr_tallysheet_print,
                        null
                    )
            val viewBinder = VegaNicaraguaMtnrTallysheetPrintBinding.bind(view)
            UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
            UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoRp)

            if (receivingData.inventoryDTO?.isNotEmpty() == true) {
                val inventoryDTO = receivingData.inventoryDTO!![0]
                receivingData.certificate = inventoryDTO.certification
                receivingData.gradeDesc = inventoryDTO.gradeDesc

                receivingData.transportVendorCode = inventoryDTO.vendorCode
                receivingData.transportVendorName = getVendorNameFromVendorList(vendorList, inventoryDTO.vendorCode)



                if (!receivingData.inventoryDTO!![0].inventoryQC
                        .isNullOrEmpty()
                ) {
                    var list = receivingData.inventoryDTO!![0].inventoryQC

                    list?.forEach { it1 ->
                        if (it1.qcName.equals("NICERTI")) {
                            receivingData.palletType = it1.value
                        }
                    }
                }
            }
            viewBinder.tvmtnrDeliverynumber.text = receivingData.delivery.toString()
            viewBinder.tvmtnrDeliverynumberRp.text = receivingData.delivery.toString()
            when (receivingData.certificate) {
                UIUtils.NICERTFD_SBUX -> {
                    viewBinder.mtnrCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                    viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                    viewBinder.mtnrCertificationLogoRp.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                    viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                }
                UIUtils.NICERTFD_UTZ -> {
                    viewBinder.mtnrCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                    viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                    viewBinder.mtnrCertificationLogoRp.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                    viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                }
                UIUtils.NICERTFD_RFA -> {
                    viewBinder.mtnrCertificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                    viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                    viewBinder.mtnrCertificationLogoRp.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                    viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                }
                else -> {
                    viewBinder.mtnrCertificationLogo.visibility = View.GONE
                    viewBinder.mtnrCertificationLogoRp.visibility = View.GONE
                }
            }


            viewBinder.origenValue.text = receivingData.origin.plus(" - ").plus(receivingData.supplierName)
            viewBinder.origenValueRp.text = receivingData.origin.plus(" - ").plus(receivingData.supplierName)

            viewBinder.dateandHourValue.text =
                DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
            viewBinder.dateandHourValueRp.text =
                DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")

            viewBinder.tvRemarksCopy.text = receivingData.remarks
            viewBinder.tvRemarksCopyRp.text = receivingData.remarks
            viewBinder.tvmtnrDocSequenceRp.text = receivingData.palletType
            viewBinder.tvmtnrDocSequence.text = receivingData.palletType
            viewBinder.tvSLossCopy.text = receivingData.tareWeight1
            viewBinder.tvSLossCopyRp.text = receivingData.tareWeight1

            if (receivingData.materialName!!.contains("Tolling", true)) {
                viewBinder.vendorValue.text = getVendorNameFromVendorList(vendorList, receivingData.supplierCode)
                viewBinder.vendorValueRp.text = getVendorNameFromVendorList(vendorList, receivingData.supplierCode)
            }

            viewBinder.truckNoValue.text =
                receivingData.transportVendorCode.plus(" - ").plus(receivingData.transportVendorName)
            viewBinder.truckNoValueRp.text =
                receivingData.transportVendorCode.plus(" - ").plus(receivingData.transportVendorName)

            receivingData.plantName = getPlantName(receivingData.plantId)
            viewBinder.recibidoValue.text = receivingData.plantId.plus(" - ").plus(receivingData.plantName)
            viewBinder.recibidoValueRp.text = receivingData.plantId.plus(" - ").plus(receivingData.plantName)

            viewBinder.conductorValue.text = receivingData.driverName
            viewBinder.conductorValueRp.text = receivingData.driverName

            viewBinder.locationValue.text = receivingData.vehNo
            viewBinder.locationValueRp.text = receivingData.vehNo

            if (receivingData.certificate!!.length > 0) {
                viewBinder.certificationValue.text = receivingData.certificate
                viewBinder.certificationValueRp.text = receivingData.certificate
            }
            viewBinder.tvBagValue.append(receivingData.bagCount)
            viewBinder.tvBagValueRp.append(receivingData.bagCount)

            viewBinder.tvTareWeightValue.text = receivingData.tareWeight
            viewBinder.tvTareWeightValueRp.text = receivingData.tareWeight
            viewBinder.tvGrossWeightValue.append(formatTwoDigString(receivingData.grossWeight.toString()))
            viewBinder.tvGrossWeightValueRp.append(formatTwoDigString(receivingData.grossWeight.toString()))

            viewBinder.tvNetWeightValue.append(formatTwoDigString(receivingData.netWeight))
            viewBinder.tvNetWeightValueRp.append(formatTwoDigString(receivingData.netWeight))

            if (receivingData.netWeight.isNotEmpty()) {
                var quintel = receivingData.netWeight.toDouble() / 46
                viewBinder.tvGrossQQsValue.append(quintel.formatThreeDigits())
                viewBinder.tvGrossQQsValueRp.append(quintel.formatThreeDigits())
            }

            viewBinder.tvGrossQQsValue.append(receivingData.batchNumber)
            viewBinder.tvGrossQQsValueRp.append(receivingData.batchNumber)


            viewBinder.tvTicketValue.append(receivingData.palletType)
            viewBinder.tvTicketValueRp.append(receivingData.palletType)

            if (receivingData.gradeDesc!!.isNotEmpty()) {
                viewBinder.tvQualityGradeValue.append(receivingData.gradeDesc)
                viewBinder.tvQualityGradeValueRp.append(receivingData.gradeDesc)

            } else {
                viewBinder.tvQualityGradeValue.append(getQualityDescriptionFromCode(receivingData.materialCode))
                viewBinder.tvQualityGradeValueRp.append(getQualityDescriptionFromCode(receivingData.materialCode))

            }
            viewBinder.tvMaterialname.append(receivingData.materialName)
            viewBinder.tvMaterialnameRp.append(receivingData.materialName)

            tallyPrintKeys.clear()
            tallyPrintKeys.add(
                bitmapToString(
                    getBitmapFromView(view, Color.WHITE)
                )
            )
            /*tallyPrintKeys.add(
                ""
            )*/
            runOnUiThread {
                showPreviewDialog()
            }
        }.execute()
    }
    private fun updateUi(response: Resource<GenericReqAndResp<List<VegaReceiving>>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let { it1 ->
                    if (it1.size > 0) {
                        val listItems = it1 as MutableList<VegaReceiving>
                        val mtnrItems = listItems.filter {
                            !it.grnNumber.isNullOrEmpty() && it.wtype == "STO"
                        }
                        if (mtnrItems.isNotEmpty()) {
                            mtnrItems.forEach {
                                try {
                                    if (it.inventoryDTO?.isNotEmpty() == true) {
                                        it.supplierCode = it.inventoryDTO!![0].vendorCode
                                    }

                                    it.supplierName =
                                        getVendorNameFromVendorList(vendorList, it.supplierCode.toString())


                                    it.materialName = getMaterialNameFromMaterialList(
                                        materialList,
                                        it.materialCode.toString()
                                    )
                                    it.taxId = getTaxIdFromVendorList(vendorList, it.supplierCode.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            mtnrList.addAll(mtnrItems)
                        }
                        setupAdapter(mtnrList)

                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
                setupAdapter(mtnrList)
            }
            else -> {}
        }
    }
    fun getVendorNameFromVendorList(supplierList: ArrayList<VegaVendor>, vendorCode: String?): String? {
        val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
        return if (vendor.size > 0) vendor[0].vendorName else ""
    }
    private fun getMaterialNameFromMaterialList(materialList: MutableList<VegaMaterial>, materialCode: String): String? {
        val material = materialList.filter { materialCode.contains(it.materialCode) }
        return if (material.size > 0) material[0].materialName else ""
    }
    fun getTaxIdFromVendorList(supplierList: ArrayList<VegaVendor>, vendorCode: String?): String? {
        val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
        return if (vendor.size > 0) vendor[0].taxNumber else ""
    }
    private fun getQualityDescriptionFromCode(gradeCode1: String?): String? {
        //   val gradeCode = if (gradeCode1?.length ?: 0 >= 4) gradeCode1?.takeLast(4).toString() else ""
        val desc = materialQualityGradeList.filter { it.materialCode.equals(gradeCode1) }.map { it.grade }
        return if (desc.isNotEmpty()) desc[0] else gradeCode1
    }
    private fun setupAdapter(itemList: MutableList<VegaReceiving>) {
        try {
            if (itemList.isNotEmpty()) {
                binding.rvTransaction.visible()
                binding.tvPrint.visible()
                binding.tvNoData.gone()
            } else {
                binding.rvTransaction.gone()
                binding.tvPrint.gone()
                binding.tvNoData.visible()
            }
            binding.rvTransaction.setUpAdapter(
                itemList.asReversed(),
                R.layout.item_vega_nicaragua_mtnr_reprint,
                ItemVegaNicaraguaMtnrReprintBinding::inflate,
                { it, pos, bindItem ->
                    bindItem.tvGrnTempId.text =
                        getString(com.olam.warehouse.presentation.R.string.transaction_mtnr)
                    bindItem.tvGrnTempIdValue.text = it.grnNumber
                    bindItem.tvVendorValue.text = it.supplierName.plus(" - ").plus(it.supplierCode)
                    bindItem.tvMaterialValue.text = it.materialName
                    bindItem.tvBatchNoValue.text = it.batchNumber
                    bindItem.tvNetWeightValue.text = it.netWeight.plus(" KG(S)")
                    if (it.erdat?.isNotEmpty() == true)
                        if (it.erdat?.length == 8) {
                            bindItem.tvDateValue.text =
                                it.erdat?.substring(6).plus("/").plus(it.erdat?.substring(4, 6))
                                    .plus("/")
                                    .plus(it.erdat?.substring(0, 4))
                        } else {
                            bindItem.tvDateValue.text = it.erdat.let { it1 ->
                                it1?.let { it2 ->
                                    DateUtils.getDate(it2.toLong(), "dd/MM/yyyy")
                                    /*DateUtils.getUTCDateTime(
                                    it2,
                                    App.getAppContext()
                                )*/
                                }
                            }
                        }
                    bindItem.cbGrnItem.isChecked = it.isProgress
                    bindItem.cbGrnItem.tag = pos
                    bindItem.cvGrnItem.setOnClickListener { view ->
                        it.isProgress = !it.isProgress
                        bindItem.cbGrnItem.isChecked = it.isProgress
                        if (it.isProgress) setPrintData(it, pos) else removePrintItem(it)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun removePrintItem(it: VegaReceiving) {
        var pos = 0
        printableList.forEachIndexed { index, vegaNicaraguaGrnReprintModel ->
            if (vegaNicaraguaGrnReprintModel.weighBridgeId.equals(it.weighBridgeId))
                pos = index
        }
        printableList.removeAt(pos)
    }
    private fun setPrintData(receivingData: VegaReceiving, pos: Int) {
        try {
            this.receivingData = receivingData
            netWeight = receivingData.netWeight
            vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
                materialQualityGradeList = it.toMutableList()
            })
            vm.getMaterialQualityGrades(receivingData.materialCode!!)
            vm.bagItems.observeOnce(viewLifecycleOwner, Observer {
                weighDetails = it
                val printModel = VegaNicMtnrReprintModel()
                printModel.weighBridgeId = receivingData.weighBridgeId
                printModel.receivingData = receivingData
                printModel.weighDetails = weighDetails
                printableList.add(printModel)
            })
            if (receivingData.tmpWbId.isNotEmpty())
                receivingData.batchNumber?.let { vm.getBagItems(it, receivingData.tmpWbId) }
            else {
                val printModel = VegaNicMtnrReprintModel()
                printModel.weighBridgeId = receivingData.weighBridgeId
                printModel.receivingData = receivingData
                printModel.weighDetails = weighDetails
                printableList.add(printModel)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun removePrintItemList(it: VegaMtnrReprintList) {
        var pos = 0
        printableList.forEachIndexed { index, vegaNicaraguaGrnReprintModel ->
            if (vegaNicaraguaGrnReprintModel.weighBridgeId.equals(it.moduleNo))
                pos = index
        }
        printableList.removeAt(pos)
    }
    private fun formatTwoDigString(str: String): String {
        if (str.isEmpty() || !str.contains(".") || !str.contains(",")) return str
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return strFormat.format(this).replace(",", "")
    }
    private fun getPlantName(plantId: String?): String {
        val plant = getMultiPlantList().filter { plantId?.contains(it.plantId) == true }
        return if (plant.size > 0) plant[0].plantName else ""
    }
}
