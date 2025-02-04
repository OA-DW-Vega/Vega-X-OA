package com.olam.warehouse.vegax.mtntnicaragua.ui.reprint

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.databinding.ItemPrintTicketPreviewBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicTicketListModel
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicTicketModel
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaReprintTicketListBinding
import com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntViewModel
import com.olam.warehouse.vegax.mtntnicaragua.utils.getColor
import kotlinx.android.synthetic.main.fragment_vega_nic_ticket_reprint.view.*
import kotlinx.android.synthetic.main.item_vega_nicaragua_mtnt_reprint.view.cbMtntItem
import kotlinx.android.synthetic.main.item_vega_nicaragua_mtnt_reprint.view.cvMtntItem
import kotlinx.android.synthetic.main.item_vega_nicaragua_mtnt_reprint.view.tvNetWeightValue
import kotlinx.android.synthetic.main.item_vega_nicaragua_mtnt_reprint.view.tvVendorValue
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNicTicketReprintFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_reprint_ticket_list
    private lateinit var binding: FragmentVegaNicaraguaReprintTicketListBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var callBack: CallBack? = null
    private var bitmapPrintKeys = ArrayList<String>()
    private var itemList: MutableList<VegaNicTicketListModel> = ArrayList()
    private var printableList = arrayListOf<VegaNicTicketModel>()
    private var materials = mutableListOf<VegaMaterial>()
    private var materialCode: String = ""
    private var materialName: String = ""
    var list = ArrayList<String>()
    var ticket = ""
    var vendor: String = ""
    var vendorCode: String = ""
    private var supplierList = mutableListOf<VegaVendor>()
    private var supplierLists = mutableListOf<VegaVendor>()
    private var isMultipleAdd = false

    companion object {
        fun newInstance() = VegaNicTicketReprintFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment()

        fun replaceFragment(moveFrag: String, receivingData: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaNicaraguaReprintTicketListBinding.inflate(layoutInflater)
        binding.tvGrnHeading.text = getString(R.string.re_print_ticket)
        binding.spWareHouse.visible()
        binding.ivDown.visible()
        binding.tvPrint.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        binding.tvPrint.isEnabled = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        vm.allProduct.observe(viewLifecycleOwner, Observer {
            materials = it.toMutableList()

        })
        vm.getAllProduct()
        binding.spWareHouse.setOnClickListener { showMaterialDialog(materials) }
        vm.suppplier.observe(viewLifecycleOwner, Observer {
            if (it != null)
                supplierList = it as MutableList
        })
        vm.getSuppliers()
        binding.tvPrint.setOnClickListener { showConfirmDialog() }

    }

    private fun updateUIWithOnlineData(it: Resource<GenericReqAndResp<List<VegaNicTicketListModel>>>?) {
        when (it?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val response = it.data?.data
                itemList = response as MutableList<VegaNicTicketListModel>
                setupAdapter(itemList)
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), it.error.toString())
            }
        }

    }


    private fun setupAdapter(itemList: MutableList<VegaNicTicketListModel>) {
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
        binding.rvTransaction.setUp(
            itemList,
            R.layout.fragment_vega_nic_ticket_reprint,
            { it, pos ->
                vendorCode = it.vendorCode.trim()
                supplierLists =
                    supplierList.filter { it.vendorCode == vendorCode } as MutableList<VegaVendor>
                list = supplierLists.map {
                    it.vendorCode.plus("-").plus(it.vendorName)
                } as ArrayList<String>
                list.forEach {
                    tvVendorValue.text = it.trim()
                    vendor = it.trim()
                }
                it.vendor = vendor
                tvBatchNoValue.text = it.lotId.trim()
                tvNetWeightValue.text = it.stockQty.trim()
                if (it.grnDate.trim().isNotEmpty()) {
                    val day = it.grnDate.substring(6, 8)
                    val month = it.grnDate.substring(4, 6)
                    val year = it.grnDate.substring(0, 4)
                    tvDateValue.text = day.plus("/").plus(month).plus("/").plus(year)
                }
                if (it.inventoryQC.isNotEmpty()) {
                    it.inventoryQC.forEach {
                        if (it.qcName.trim().equals("NICERTI")) ticket = it.value.trim()
                    }
                }
                tvGrnTempIdValue.text = ticket
                cbMtntItem.isChecked = it.isProgress
                cvMtntItem.setOnClickListener { view ->
                    it.isProgress = !it.isProgress
                    binding.tvPrint.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.green))
                    binding.tvPrint.isEnabled = true
                    cbMtntItem.isChecked = it.isProgress
                    if (it.isProgress) setPrintData(it) else removePrintItem(it)
                    if (!isMultipleAdd) {
                        if (it.isProgress)
                            removeChecked(pos, itemList)
                    } else
                        binding.rvTransaction.adapter?.notifyItemChanged(pos)
                }

            },
            itemClick = {

            })
    }

    private fun removeChecked(item: Int, list: MutableList<VegaNicTicketListModel>) {
        list.forEach { it.isProgress = false }
        list[item].isProgress = true
        setupAdapter(list)
    }

    private fun removePrintItem(it: VegaNicTicketListModel) {
        var pos = 0
        printableList.forEachIndexed { index, vegaNicaraguaTicketReprintModel ->
            if (vegaNicaraguaTicketReprintModel.batchNumber.equals(it.lotId))
                pos = index
        }
        printableList.removeAt(pos)
    }

    private fun getMaterialList(): String {
        return materialCode
    }

    private fun setPrintData(receivingData: VegaNicTicketListModel) {
        val printModel = VegaNicTicketModel()
        printableList.clear()
        printModel.batchNumber = receivingData.lotId
        printModel.materialCode = receivingData.materialCode
        printModel.netWeight = receivingData.stockQty
        printModel.erdat = receivingData.grnDate
        if (receivingData.inventoryQC.isNotEmpty()) {
            receivingData.inventoryQC.forEach {
                if (it.qcName.trim().equals("NIFG0014")) printModel.certificate = it.value.trim()
                else if (it.qcName.trim().equals("STOCKBAGS")) printModel.bagCount = it.value.trim()
                else if (it.qcName.trim().equals("NICERTI")) printModel.ticket = it.value.trim()
                else if (it.qcName.trim().equals("NIPOSITI")) printModel.grade = it.value.trim()
            }
        }
        printModel.unitsOfMeasure = receivingData.uom
        printModel.isProgress = receivingData.isProgress
        printModel.transportVendorCode = receivingData.vendor
        printableList.add(printModel)

    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    generateBitMapKey()
                },
                { dismiss() })
        }
    }

    private fun showMaterialDialog(materialList: List<VegaMaterial>) {
        val suppliers = materialList.map { it.materialName ?: "" }
        MaterialDialog(requireContext()).show {
            title(R.string.select_material)
            listItemsSingleChoice(items = suppliers) { _, index, text ->
                binding.spWareHouse.text = text
                materialCode = materialList[index].materialCode
                materialName = materialList[index].materialName ?: ""
                vm.weighBridgeOnline.observe(
                    viewLifecycleOwner,
                    Observer { updateUIWithOnlineData(it) })
                vm.getWeighBridgeDataOnline(getMaterialList())
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            )
        }
    }

    private fun generateBitMapKey() {
        bitmapPrintKeys.clear()
        showCustomLoading()
        DoAsync {


            printableList.forEachIndexed { index, item ->
                val view = LayoutInflater.from(context)
                    .inflate(com.olam.warehouse.login.R.layout.item_print_ticket_preview, null)
                val viewBinder = ItemPrintTicketPreviewBinding.bind(view)

                val mtnt = item
                if (mtnt.ticket.isEmpty()) {
                    UIUtils.showErrorDialog(
                        requireContext(),
                        getString(R.string.no_data_found)
                    )
                } else {
                    viewBinder.ivPreview.setImageBitmap(getBitmap(mtnt.ticket))
                    viewBinder.tvLotValue.text = mtnt.ticket
                    if (mtnt.erdat.isNotEmpty()) {
                        val day = mtnt.erdat.substring(6, 8)
                        val month = mtnt.erdat.substring(4, 6)
                        val year = mtnt.erdat.substring(0, 4)
                        viewBinder.tvMaterialValue.text =
                            day.plus("/").plus(month).plus("/").plus(year)
                    }
                    //if(mtnt.grade.isEmpty()) viewBinder.tvGradeValue.text = "NIPERGAM N027"
                    viewBinder.tvGradeValue.text = mtnt.grade
                    //if(mtnt.certificate.isEmpty())  viewBinder.tvCertificateValue.text = "NICERTD FT"
                    viewBinder.tvCertificateValue.text = mtnt.certificate
                    viewBinder.tvWeightValue.text = mtnt.netWeight
                    // if(mtnt.transportVendorCode.isEmpty()) viewBinder.tvClientValue.text = "1123197 - ANTONIO"
                    viewBinder.tvClientValue.text = mtnt.transportVendorCode
                    viewBinder.tvSacksValue.text = mtnt.bagCount
                    bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                view, Color.WHITE
                            )
                        )
                    )
                }
            }
            HandlerUtils.runOnUiThread {
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


    fun getPlantDetails(): Plant {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    }

}
