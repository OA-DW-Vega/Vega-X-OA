package com.olam.warehouse.vegax.forwardponicaragua.ui.reprint

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.databinding.VegaNicaraguaForwardpoReceiptPrintingBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.data.domain.model.VegaNicaraguaForwardPoReprintModel
import com.olam.warehouse.vegax.forwardponicaragua.databinding.FragmentVegaNicaraguaForwardPoReprintBinding
import com.olam.warehouse.vegax.forwardponicaragua.databinding.ItemVegaNicaraguaForwardpoReprintBinding
import com.olam.warehouse.vegax.forwardponicaragua.ui.VegaNicaraguaForwardPOViewModel
import com.olam.warehouse.vegax.forwardponicaragua.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
class VegaNicaraguaForwardPoReprintFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_forward_po_reprint
    private lateinit var binding: FragmentVegaNicaraguaForwardPoReprintBinding
    private val vm: VegaNicaraguaForwardPOViewModel by viewModel()
    private var grnList = mutableListOf<VegaNicaraguaForwardPODetails>()
    private var forWardPOList = mutableListOf<VegaNicaraguaForwardPODetails>()
    private var mSearchList = mutableListOf<VegaNicaraguaForwardPODetails>()
    private var receivingData = VegaNicaraguaForwardPODetails()
    private var callBack: CallBack? = null
    private var priceEdit = false
    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var gradeMappingDescription: String? = ""
    private var printableList = arrayListOf<VegaNicaraguaForwardPoReprintModel>()
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

    companion object {
        fun newInstance() = VegaNicaraguaForwardPoReprintFragment().putArgs {
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
        binding = FragmentVegaNicaraguaForwardPoReprintBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                            setupAdapter(forWardPOList)
                        } else {
                            mSearchList.clear()
                            forWardPOList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.poNumber?.contains(text,true) == true) {
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initUI() {

        vm.forwardPOTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        /*  vm.grnPrintDetails.observe(viewLifecycleOwner, Observer { updatePrintUI(it) })
        vm.grnPrintDetailsOffline.observe(viewLifecycleOwner, Observer { updatePrintOfflineUI(it) })*/

        binding.tvPrint.setOnClickListener { showConfirmDialog() }
        vm.getForwardPODetails()
    }

    private fun updatePrintOfflineUI(it: List<VegaNicaraguaForwardPODetails>?) {
        it?.let { it1 ->
            val listItems = it1 as MutableList<VegaNicaraguaForwardPODetails>
            grnList.addAll(listItems)
        }
        setupAdapter(grnList)
    }


    private fun updateUI(data: List<VegaNicaraguaForwardPODetails>?) {
        forWardPOList.clear()
        data?.let { forWardPOList.addAll(it)
                   }
forWardPOList.forEach {
    val printModel = VegaNicaraguaForwardPoReprintModel()

    printModel.forwardPoData = it
    printModel.weighBridgeId= it.poNumber.toString()
    printableList.add(printModel)
}
        setupAdapter(forWardPOList)
    }

    private fun setupAdapter(itemList: MutableList<VegaNicaraguaForwardPODetails>) {
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
            itemList.asReversed(),
            R.layout.item_vega_nicaragua_forwardpo_reprint,
            ItemVegaNicaraguaForwardpoReprintBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvGrnTempIdValue.text =
                    if (it.poNumber?.isNotEmpty() == true) it.poNumber else it.tempId
                bindItem.tvVendorValue.text = it.vendorName
                bindItem.tvMaterialValue.text = it.materialName
                bindItem.tvBatchNoValue.text = it.qualityGradeDesc
                bindItem.tvNetWeightValue.text = it.netWeight.plus(" KG(S)")
                bindItem.tvDateValue.text = it.docDate?.let { it1 ->
                    it1.let { it2 ->
                        DateUtils.getUTCDateTimeNicaragua(
                            DateUtils.getTimeStamp(it2).toString(),
                            App.getAppContext()
                        )
                    }
                }

                bindItem.cbGrnItem.isChecked = it.isProgress
                bindItem.cvGrnItem.setOnClickListener { view ->
                    it.isProgress = !it.isProgress
                    bindItem.cbGrnItem.isChecked = it.isProgress
                    if (it.isProgress) setPrintData(it) else removePrintItem(it)
                }

            })

    }



    private fun removePrintItem(it: VegaNicaraguaForwardPODetails) {
        var pos = 0
        printableList.forEachIndexed { index, vegaNicaraguaGrnReprintModel ->
            if (vegaNicaraguaGrnReprintModel.weighBridgeId.equals(it.poNumber))
                pos = index
        }
        printableList.removeAt(pos)
    }

    private fun setPrintData(receivingData: VegaNicaraguaForwardPODetails) {
        this.receivingData = receivingData
        netWeight = receivingData.netWeight
        val printDatas = printableList.map { it.weighBridgeId }
       if(!printDatas.contains(receivingData.poNumber)) {
           val printModel = VegaNicaraguaForwardPoReprintModel()
           printModel.forwardPoData = receivingData
           printModel.weighBridgeId = receivingData.poNumber.toString()
           printableList.add(printModel)
       }
        }

    private fun formatTwoDigString(str: String): String {
        if (str.isEmpty() || !str.contains(".") || !str.contains(",")) return str
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return strFormat.format(this).replace(",", "")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    generateBitMapKey()
                },
                { dismiss() })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateBitMapKey() {
        bitmapPrintKeys.clear()
        showCustomLoading()
        Log.d("printableList",printableList.size.toString())
        DoAsync {
            printableList.forEachIndexed { index, item ->
                var receivingData = item.forwardPoData
                var bagsData = item.weighBridgeId
                var view = LayoutInflater.from(context)
                    .inflate(com.olam.warehouse.login.R.layout.vega_nicaragua_forwardpo_receipt_printing, null)
                var input = receivingData.netWeight.toString()
                var qw = java.lang.Double.valueOf(input)
                var d = qw / 46
                  val pricegrosskg = d.formatTwoDigits()
                var viewBinder = VegaNicaraguaForwardpoReceiptPrintingBinding.bind(view)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                viewBinder.tvvendor.text =
                    receivingData.vendorName + "\n" + receivingData.vendorAddress + "\n" + receivingData.taxNumber

                 viewBinder.tvcontractdate.text = DateUtils.convertDateToSpanish(receivingData.docDate.toString())
                var deliverydate=DateUtils.convertDateToSpanish(receivingData.deliveryDate.toString())
                viewBinder.tvdeliveryterm.text =deliverydate.replace("-"," de ")
                viewBinder.tvQuantity.text =
                    pricegrosskg + " " + resources.getString(com.olam.warehouse.login.R.string.qq) + " " + resources.getString(
                        com.olam.warehouse.login.R.string.equivalente
                    ) + " " + receivingData.netWeight + " " + resources.getString(
                        com.olam.warehouse.login.R.string.kg
                    )
                viewBinder.tvcontractnumber.text = receivingData.poSequenceNumber
                viewBinder.tvProductName.text = receivingData.materialName
                viewBinder.tvQualityGrade.text = receivingData.qualityGradeDesc
                viewBinder.tvpricegrosskg.text =
                    resources.getString(com.olam.warehouse.login.R.string.c_doller) + " " + receivingData.pricePerUnit
                viewBinder.tvnetpricexQQPOA.text =resources.getString(com.olam.warehouse.login.R.string.c_doller).plus(" ").plus(receivingData.receiptNetPrice)
                viewBinder.tvplaceOfDelivery.text =resources.getString(com.olam.warehouse.login.R.string.s_buying_unit) + " " + getPlantDetails().plantName
                viewBinder.tvdata2.text = receivingData.vendorName
                viewBinder.buyer.text =
                    resources.getString(com.olam.warehouse.login.R.string.print_title_1) + "\n" + resources.getString(
                        com.olam.warehouse.login.R.string.print_title_3
                    ) + "\n" + resources.getString(com.olam.warehouse.login.R.string.print_title_2)
                viewBinder.title2.text =
                    resources.getString(com.olam.warehouse.login.R.string.direction) + " " + resources.getString(
                        com.olam.warehouse.login.R.string.print_title_3
                    )

                bitmapPrintKeys.clear()
                bitmapPrintKeys.add(
                    bitmapToString(
                        getBitmapFromView(
                            view, Color.WHITE
                        )
                    )
                )
            }
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(bitmapPrintKeys))
                startActivity(Intent(activity, WifiMainActivity::class.java))
                //showPreviewDialog()
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
        supplierList: ArrayList<VegaVendor>,
        vendorCode: String?
    ): String? {
        val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
        return if (vendor.size > 0) vendor[0].taxNumber else ""
    }

    fun getVendorNameFromVendorList(
        supplierList: ArrayList<VegaVendor>,
        vendorCode: String?
    ): String? {
        val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
        return if (vendor.size > 0) vendor[0].vendorName else ""
    }

}



