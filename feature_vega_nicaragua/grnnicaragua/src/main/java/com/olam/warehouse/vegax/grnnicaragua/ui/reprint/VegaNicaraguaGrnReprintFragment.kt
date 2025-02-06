package com.olam.warehouse.vegax.grnnicaragua.ui.reprint

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.databinding.VegaNicaraguaGrnReceiptPrintingBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaGrnReprintModel
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnReprintBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaGrnReprintBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
class VegaNicaraguaGrnReprintFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_reprint
    private lateinit var binding: FragmentVegaNicaraguaGrnReprintBinding
    private val vm: VegaNicaraguaGrnViewModel by viewModel()
    private var grnList = mutableListOf<VegaReceiving>()
    private var mSearchList = mutableListOf<VegaReceiving>()
    private var receivingData = VegaReceiving()
    private var callBack: CallBack? = null
    private var priceEdit = false
    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var gradeMappingDescription: String? = ""
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var printableList = arrayListOf<VegaNicaraguaGrnReprintModel>()
    private var priceConfigDetails: VegaNicaraguaPriceConfigDetails? = null
    private var advanceValues = listOf<VegaNicaraguaAdvanceLineItemGrn>()
    private var yieldPercentage: String? = "1"//it is in percentage
    private var exchangeRate: String? = ""//it is in percentage
    private var currency: String? = ""
    private var USDAmount: String? = "1"
    private var netWeight: String? = ""
    private var certificatePremium: Double? = 0.0
    private var volumePremium: Double? = 0.0
    private var humidityPremium: Double? = 0.0
    private var qualityDiscount: Double? = 0.0
    private var totalPrice: Double? = 0.0
    private var grossValue: Double = 0.0
    var netPayment: Double? = 0.0
    private var bitmapPrintKeys = ArrayList<String>()
    private var vendorList = arrayListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()

    companion object {
        fun newInstance() = VegaNicaraguaGrnReprintFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String, receivingData: Any, qualityParameterList: ArrayList<VegaQualityParameter?>
        )
        fun replaceFragment(moveFrag: String, receivingData: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNicaraguaGrnReprintBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
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
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_by_lot)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setupAdapter(grnList)
                        } else {
                            mSearchList.clear()
                            grnList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.batchNumber?.contains(text,true) == true ||
                                        qtyWb.grnNumber?.contains(text,true) == true ||
                                        (qtyWb.grnNumber?.isEmpty() == true && qtyWb.palletType?.contains(text,true) == true)
                                    ) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setupAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }


    private fun initUI() {
        vm.grnTransList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.grnPrintDetails.observe(viewLifecycleOwner, Observer { updatePrintUI(it) })
        vm.grnPrintDetailsOffline.observe(viewLifecycleOwner, Observer { updatePrintOfflineUI(it) })
        vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
            materialQualityGradeList = it.toMutableList()
            vm.getProducts()
        })
        vm.getMaterialQualityGrades("")
        binding.tvPrint.setOnClickListener { showConfirmDialog() }

        vm.product.observe(viewLifecycleOwner, Observer {
            materialList.addAll(it)
            vm.getSuppliers("")
        })

        vm.supplier.observe(viewLifecycleOwner, Observer {
            it?.let {
                vendorList = it as ArrayList<VegaVendor>
            }
            vm.getReceivingWithLineItem()
        })


    }

    private fun updatePrintOfflineUI(it: List<VegaReceiving>?) {
        it?.let { it1 ->
            val listItems = it1 as MutableList<VegaReceiving>
            grnList.addAll(listItems)
        }
        val offlineList= grnList.filter {  it.grnNumber.isNullOrEmpty() || it.batchNumber.isNullOrEmpty() }.sortedByDescending { it.palletType }
        val onlineList= grnList.filter { !it.grnNumber.isNullOrEmpty() }.sortedByDescending { it.grnNumber }

        grnList.clear()
        grnList.addAll(offlineList)
        grnList.addAll(onlineList)
        setupAdapter(grnList)
        setupAdapter(grnList)
    }

    private fun updatePrintUI(response: Resource<GenericReqAndResp<List<VegaReceiving>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let { it1 ->
                    if (it1.size > 0) {
                        val listItems = it1 as MutableList<VegaReceiving>
                        listItems.forEach {
                            it.supplierName =
                                getVendorNameFromVendorList(vendorList, it.supplierCode.toString())
                            it.materialName = getMaterialNameFromMaterialList(
                                materialList,
                                it.materialCode.toString()
                            )
                            it.taxId =
                                getTaxIdFromVendorList(vendorList, it.supplierCode.toString())
                            it.gradeDesc =
                                (if (it.inventoryDTO?.isNotEmpty() == true) getQualityDescriptionFromCode(
                                    it.inventoryDTO?.get(0)?.gradeDesc
                                ) else "").toString()
                            it.certificate =
                                (if (it.inventoryDTO?.isNotEmpty() == true) it.inventoryDTO?.get(0)?.certification else "").toString()
                        }
                        grnList.addAll(listItems.filter { it.wtype.equals(Constants.PROCURE) })
                    }
                    val offlineList= grnList.filter {  it.grnNumber.isNullOrEmpty() || it.batchNumber.isNullOrEmpty() }.sortedByDescending { it.palletType }
                    val onlineList= grnList.filter { !it.grnNumber.isNullOrEmpty() }.sortedByDescending { it.grnNumber }
                    grnList.clear()
                    grnList.addAll(offlineList)
                    grnList.addAll(onlineList)
                    setupAdapter(grnList)

                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
                setupAdapter(grnList)
            }
        }
    }


    private fun updateUI(it: List<VegaReceiving>?) {
        it?.let { items ->
            val grnData = items.filter { !it.isSynced && it.status == Status.SYNC_PENDING }.toMutableList()
            grnData.forEach {
                if (!it.materialName?.contains("tolling", true)!!)
                    it.tareWeight = (if (it.bagCount?.isNotEmpty() == true) it.bagCount?.toInt() ?: 0 else 0).times(
                        if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() ?: 0.0 else 0.0
                    ).formatTwoDigits()
            }
            grnData.sortByDescending { it.batchNumber }
            grnList = grnData
//            setupAdapter(grnList)
        }
        if (isOnline()) vm.getGrnPrintDetails() else vm.getGrnPrintDetailsOffline()
    }

    private fun setupAdapter(itemList: MutableList<VegaReceiving>) {
//        itemList.sortByDescending { DateUtils.getDateString(it.erdat?:"") }
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvPrint.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvPrint.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUpAdapter(
            itemList,
            R.layout.item_vega_nicaragua_grn_reprint,
            ItemVegaNicaraguaGrnReprintBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvGrnTempIdValue.text =
                    if (it.grnNumber?.isNotEmpty() == true) it.grnNumber else it.palletType
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
                bindItem.cvGrnItem.setOnClickListener { view ->
                    it.isProgress = !it.isProgress
                    bindItem.cbGrnItem.isChecked = it.isProgress
                    if (it.isProgress) setPrintData(it) else removePrintItem(it)
                    if(it.isProgress) {
                        grnList.forEach { grItem -> grItem.isProgress = false }
                        grnList.find { gr -> gr.batchNumber?.equals(it.batchNumber) == true }
                            .apply { this?.isProgress = true }
                        binding.rvTransaction.adapter?.notifyDataSetChanged()
                    }
                }
            })
    }

    private fun removePrintItem(it: VegaReceiving) {
        var pos = 0
        printableList.forEachIndexed { index, vegaNicaraguaGrnReprintModel ->
            if (vegaNicaraguaGrnReprintModel.weighBridgeId.equals(it.weighBridgeId))
                pos = index
        }
        printableList.removeAt(pos)
    }

    private fun setPrintData(receivingData: VegaReceiving) {
        printableList.clear()
        this.receivingData = receivingData
        netWeight = receivingData.netWeight
        /*vm.advanceItems.observeOnce(viewLifecycleOwner, Observer { advanceValues = it })
        vm.getAdvanceItem(receivingData.tmpWbId)*/
        vm.bagItems.observeOnce(viewLifecycleOwner, Observer {
            weighDetails = it
            val printModel = VegaNicaraguaGrnReprintModel()
            printModel.weighBridgeId = receivingData.weighBridgeId
            printModel.receivingData = receivingData
            printModel.weighDetails = weighDetails
            printableList.add(printModel)
        })
        if (receivingData.tmpWbId.isNotEmpty()) vm.getBagItems(receivingData.tmpWbId)
        else {
            val printModel = VegaNicaraguaGrnReprintModel()
            printModel.weighBridgeId = receivingData.weighBridgeId
            printModel.receivingData = receivingData
            printModel.weighDetails = weighDetails
            printableList.add(printModel)
        }
    }

    private fun formatTwoDigString(str: String): String {
        if (str.isEmpty() || !str.contains(".") || !str.contains(",")) return str
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return strFormat.format(this).replace(",", "")
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    generateBitMapKey()
                },
                { dismiss() })
        }
    }

    private fun generateBitMapKey() {
        bitmapPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            printableList.forEachIndexed { index, item ->
                var receivingData = item.receivingData
                var bagsData = item.weighDetails

                var view = LayoutInflater.from(context)
                    .inflate(
                        com.olam.warehouse.login.R.layout.vega_nicaragua_grn_receipt_printing,
                        null
                    )

                var viewBinder = VegaNicaraguaGrnReceiptPrintingBinding.bind(view)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoCopy)
                UIUtils.setOlamLogoDynamically(viewBinder.certificationLogoCopy)
                viewBinder.sapVendorName.text = receivingData.supplierName
                if (receivingData.grnNumber?.isNotEmpty() == true) {
                    viewBinder.grnNumber.text = receivingData.grnNumber
                    viewBinder.grnNumberCopy.text = receivingData.grnNumber
                } else {
                    viewBinder.grnNumber.text = ""
                    viewBinder.grnNumberCopy.text = ""
                }
                if (receivingData.erdat?.isNotEmpty() == true)
                    if (receivingData.erdat?.length == 8) {
                        var date= receivingData.erdat?.substring(6).plus("/").plus(receivingData.erdat?.substring(4, 6)).plus("/")
                            .plus(receivingData.erdat?.substring(0, 4))
                        viewBinder.dateAndHour.text =date
                        viewBinder.dateAndHourCopy.text =date
                    } else {
                        var date= receivingData.erdat.let { it1 ->
                            it1?.let { it2 ->
                                DateUtils.getDate(it2.toLong(), "dd/MM/yyyy")
                                /*DateUtils.getUTCDateTime(
                                    it2,
                                    App.getAppContext()
                                )*/
                            }
                        }
                        viewBinder.dateAndHour.text = date
                        viewBinder.dateAndHourCopy.text = date
                    }
                viewBinder.receiveNumber.text = receivingData.palletType
                viewBinder.receiveNumberCopy.text = receivingData.palletType
                viewBinder.buyingUnit.text = getPlantDetails().plantName

                viewBinder.sapVendorNameCopy.text = receivingData.supplierName
                viewBinder.buyingUnitCopy.text = getPlantDetails().plantName

                when (receivingData.certificate) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                        viewBinder.certificationLogoCopy.visibility = View.GONE
                    }
                }

                if (receivingData.certificate!!.length > 0) {
                    viewBinder.certification.text = receivingData.certificate
                    viewBinder.certificationCopy.text = receivingData.certificate
                }


                viewBinder.tvMaterialCopy.append(receivingData.materialName)
                viewBinder.tvBagsQuantityCopy.append(receivingData.bagCount)
//                    viewBinder.tvTareWeightCopy.append((if(receivingData.bagCount?.isNotEmpty()==true)receivingData.bagCount?.toInt()?:0 else 0).times(if(receivingData.tareWeight?.isNotEmpty()==true)receivingData.tareWeight?.toDouble()?:0.0 else 0.0).formatTwoDigits())
                if (receivingData.tareWeight?.isEmpty() == true || receivingData.tareWeight.equals("0"))
                    viewBinder.tvTareWeightCopy.append(
                        (if (receivingData.grossWeight?.isNotEmpty() == true) receivingData.grossWeight?.toDouble()
                            ?: 0.0 else 0.0).minus(if (receivingData.netWeight.isNotEmpty()) receivingData.netWeight.toDouble() else 0.0)
                            .formatTwoDigits()
                    )
                else
                    viewBinder.tvTareWeightCopy.append(formatTwoDigString(receivingData.tareWeight.toString()))
                viewBinder.tvGrossWeightCopy.append(formatTwoDigString(receivingData.grossWeight.toString()))
                viewBinder.tvNetWeightCopy.append(formatTwoDigString(receivingData.netWeight))
                viewBinder.tvUOMCopy.append(receivingData.unitsOfMeasure)
                viewBinder.tvBatchNumberCopy.append(receivingData.batchNumber)
                viewBinder.tvQualityGradeCopy.append(receivingData.gradeDesc)
                viewBinder.taxId.text = receivingData.taxId


                viewBinder.tvMaterial.append(receivingData.materialName)
                viewBinder.tvBagsQuantity.append(receivingData.bagCount)
//                    viewBinder.tvTareWeight.append((if(receivingData.bagCount?.isNotEmpty()==true)receivingData.bagCount?.toInt()?:0 else 0).times(if(receivingData.tareWeight?.isNotEmpty()==true)receivingData.tareWeight?.toDouble()?:0.0 else 0.0).formatTwoDigits())
                if (receivingData.tareWeight?.isEmpty() == true || receivingData.tareWeight.equals("0"))
                    viewBinder.tvTareWeight.append(
                        (if (receivingData.grossWeight?.isNotEmpty() == true) receivingData.grossWeight?.toDouble()
                            ?: 0.0 else 0.0).minus(if (receivingData.netWeight.isNotEmpty()) receivingData.netWeight.toDouble() else 0.0)
                            .formatTwoDigits()
                    )
                else
                    viewBinder.tvTareWeight.append(formatTwoDigString(receivingData.tareWeight.toString()))
                viewBinder.tvGrossWeight.append(formatTwoDigString(receivingData.grossWeight.toString()))
                viewBinder.tvNetWeight.append(formatTwoDigString(receivingData.netWeight))
                viewBinder.tvUOM.append(receivingData.unitsOfMeasure)
                viewBinder.tvBatchNumber.append(receivingData.batchNumber)
                viewBinder.tvQualityGrade.append(receivingData.gradeDesc)
                viewBinder.taxIdCopy.text = receivingData.taxId

                when (receivingData.grnType) {
                    getString(com.olam.warehouse.login.R.string.ptbf) -> {
                        viewBinder.note.visibility = View.VISIBLE
                        viewBinder.notecopy.visibility = View.VISIBLE
                        viewBinder.note.text = Html.fromHtml(
                            "<b>" + getString(com.olam.warehouse.login.R.string.nota) + "</b>".plus(
                                getString(
                            com.olam.warehouse.login.R.string.grn_receipt_nota))).toString()
                        viewBinder.notecopy.text=    Html.fromHtml("<b>"+getString(com.olam.warehouse.login.R.string.nota)+"</b>".plus(getString(
                            com.olam.warehouse.login.R.string.grn_receipt_nota))).toString()

                        viewBinder.typeOfPurchaseCopy.text = getString(com.olam.warehouse.login.R.string.s_ptbf)
                        viewBinder.typeOfPurchase.text =getString(com.olam.warehouse.login.R.string.s_ptbf)
                    }
                    getString(com.olam.warehouse.login.R.string.spot) ->
                    {
                        viewBinder.note.visibility=View.GONE
                        viewBinder.notecopy.visibility=View.GONE
                        viewBinder.typeOfPurchaseCopy.text = getString(com.olam.warehouse.login.R.string.s_spot)
                        viewBinder.typeOfPurchase.text =getString(com.olam.warehouse.login.R.string.s_spot)
                    }

                    getString(com.olam.warehouse.login.R.string.spot_tolling) ->
                    {
                        viewBinder.note.visibility=View.GONE
                        viewBinder.notecopy.visibility=View.GONE
                        viewBinder.typeOfPurchaseCopy.text = getString(com.olam.warehouse.login.R.string.s_tolling)
                        viewBinder.typeOfPurchase.text =getString(com.olam.warehouse.login.R.string.s_tolling)
                    }

                    getString(com.olam.warehouse.login.R.string.fixed) ->
                    {
                        viewBinder.note.visibility=View.GONE
                        viewBinder.notecopy.visibility=View.GONE
                        viewBinder.typeOfPurchaseCopy.text = getString(com.olam.warehouse.login.R.string.s_fixed)
                        viewBinder.typeOfPurchase.text =getString(com.olam.warehouse.login.R.string.s_fixed)
                    }
                }


                bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                view, Color.WHITE
                            )
                        )
                    )
                }
                //showPreviewDialog()
                runOnUiThread {
                    val gson = GsonUtils()
                    PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(bitmapPrintKeys))
                    startActivity(Intent(activity, WifiMainActivity::class.java))
                }

            }.execute()
        }

        override fun onResume() {
            super.onResume()
            hideCustomLoading()
        }

    private fun showPreviewDialog() {

        val list = mutableListOf<String>()
        list.addAll(bitmapPrintKeys)
        val dialogFragment =
            PrintPreviewDialogFragment(list)
        activity?.supportFragmentManager?.let { dialogFragment.show(it, "signature") }
    }

    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }

    fun getPlantDetails(): Plant {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    }

    fun getTaxIdFromVendorList(
        supplierList: java.util.ArrayList<VegaVendor>,
        vendorCode: String?
    ): String? {
        val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
        return if (vendor.size > 0) vendor[0].taxNumber else ""
    }

    fun getVendorNameFromVendorList(
        supplierList: java.util.ArrayList<VegaVendor>,
        vendorCode: String?
    ): String? {
        val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
        return if (vendor.size > 0) vendor[0].vendorName else ""
    }

    private fun getMaterialNameFromMaterialList(
        materialList: MutableList<VegaMaterial>,
        materialCode: String
    ): String? {
        val material = materialList.filter { materialCode.contains(it.materialCode) }
        return if (material.size > 0) material[0].materialName else ""
    }

    private fun getQualityDescriptionFromCode(gradeCode1: String?): String? {
        val gradeCode = if (gradeCode1?.length ?: 0 >= 4) gradeCode1?.takeLast(4).toString() else ""
        val desc = materialQualityGradeList.filter { it.gradeCode.contains(gradeCode) }
        return if (desc.isNotEmpty()) desc[0].grade else ""
    }

}



