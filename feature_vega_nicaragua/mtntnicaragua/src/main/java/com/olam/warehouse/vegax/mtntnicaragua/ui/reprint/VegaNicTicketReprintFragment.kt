package com.olam.warehouse.vegax.mtntnicaragua.ui.reprint

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.databinding.ItemPrintTicketPreviewNicaraguaBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
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
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaMtnrReprintList
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicTicketListModel
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicTicketModel
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicTicketReprintBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaReprintTicketListBinding
import com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntViewModel
import com.olam.warehouse.vegax.mtntnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNicTicketReprintFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_reprint_ticket_list
    private lateinit var binding: FragmentVegaNicaraguaReprintTicketListBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var callBack: CallBack? = null
    private var bitmapPrintKeys = ArrayList<String>()
    private var itemList: MutableList<VegaNicTicketListModel> = ArrayList()
    private var printableList = arrayListOf<VegaNicTicketModel>()
    private var printable = arrayListOf<VegaMtnrReprintList>()
    private var materials = mutableListOf<VegaMaterial>()
    private var materialCode: String = ""
    private var materialName: String = ""
    var list = ArrayList<String>()
    var ticket = ""
    var batchno = ""
    var vendor: String = ""
    var vendorCode: String = ""
    private var supplierList = mutableListOf<VegaVendor>()
    private var supplierLists = mutableListOf<VegaVendor>()
    private var isMultipleAdd = false
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var printticketArr: MutableList<VegaMtnrReprintList> = ArrayList()
    private var filteredprintticketArr = arrayListOf<VegaMtnrReprintList>()

    companion object {
        fun newInstance() = VegaNicTicketReprintFragment().putArgs {
        }
    }

    interface CallBack {
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
        setHasOptionsMenu(true)
        binding = FragmentVegaNicaraguaReprintTicketListBinding.inflate(layoutInflater)
        binding.tvGrnHeading.text = getString(R.string.re_print_ticket)
        binding.spWareHouse.gone()
        binding.ivDown.gone()
        binding.tvPrint.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        binding.tvPrint.isEnabled = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        vm.mtnrReprintList.observe(viewLifecycleOwner, Observer {
            updatePrintListdetails(it)

        })
        vm.getMtnrReprintList()
        vm.mtnrPrintDetails.observe(viewLifecycleOwner) {
            bitmapPrintKeys.clear()
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if (it.data != null && it.data!!.data != null && it.data!!.data.isNotEmpty()) {
                        bitmapPrintKeys.add(it.data!!.data)
                        showConfirmDialog()
                    } else
                        showErrorDialogWithFAQLink(
                            requireActivity(),
                            getString(R.string.no_data_found)
                        )

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
            vm.getMtnrPrintDetails(ticket, batchno, MTNR_Ticket)
        }
        /// ------- old ticket reprint
        /* vm.allProduct.observe(viewLifecycleOwner, Observer {
             materials = it.toMutableList()

         })
         vm.getAllProduct()
         binding.spWareHouse.setOnClickListener { showMaterialDialog(materials) }
         vm.suppplier.observe(viewLifecycleOwner, Observer {
             if (it != null)
                 supplierList = it as MutableList
         })
         vm.getSuppliers()
         binding.tvPrint.setOnClickListener { showConfirmDialog() }*/
    }

    private fun updatePrintListdetails(it: Resource<GenericReqAndResp<List<VegaMtnrReprintList>>>) {
        when (it.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (it.data?.data?.isNotEmpty()!!) {
                    printticketArr.clear()
                    val response = it.data?.data
                    printticketArr =
                        response?.filter { it.fileType.equals("Ticket") } ?.sortedByDescending { desc -> desc.id } as MutableList<VegaMtnrReprintList>
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
                bindItem.tvGrnTempIdValue.text = it.moduleNo?.replace("\\", "")
                bindItem.tvBatchNoValue.text = it.transactionNo
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

            })
    }

    private fun removeCheckedlist(pos: Int, list: MutableList<VegaMtnrReprintList>, oldpos: Int) {
        list.forEach { it.isProgress = false }
        list[pos].isProgress = true
        binding.rvTransaction.adapter?.notifyItemChanged(oldpos)
        binding.rvTransaction.adapter?.notifyItemChanged(pos)
    }

    private fun setPrintDatalist(receivingData: VegaMtnrReprintList) {
        val printModel = VegaMtnrReprintList()
        printable.clear()
        printModel.transactionNo = receivingData.transactionNo
        printModel.materialName = receivingData.materialName

        printModel.date = DateUtils.getDate(receivingData.date.toLong(), "dd/MM/YYYY").toString()

        printable.add(printModel)
        // ticket = receivingData.moduleNo ?: "".replace("\\", "")
        ticket = receivingData.id.toString()
        batchno = receivingData.transactionNo ?: ""

    }
    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    dismiss()
                    hideLoading()
                    if (bitmapPrintKeys.size > 0)
                        showPreviewDialog()
                },
                {
                    hideLoading()
                    dismiss()
                })
        }
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }
    private fun showPreviewDialog() {
        DoAsync {
            HandlerUtils.runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(bitmapPrintKeys))
                startActivity(Intent(requireContext(), WifiMainActivity::class.java))
            }
        }.execute()
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
                                    if (qtyWb.moduleNo!!.replace("\\", "").contains(text)) {
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

    ///------------- old reprint functions
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
                showErrorDialogWithFAQLink(requireContext(), it.error.toString())
            }
            else -> {}
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
        binding.rvTransaction.setUpAdapter(
            itemList,
            R.layout.fragment_vega_nic_ticket_reprint,
            FragmentVegaNicTicketReprintBinding::inflate,
            { it, pos, bindItem ->
                vendorCode = if (!it.vendorCode.isNullOrBlank()) it.vendorCode else ""
                supplierLists =
                    supplierList.filter { it.vendorCode == vendorCode } as MutableList<VegaVendor>
                list = supplierLists.map {
                    it.vendorCode.plus("-").plus(it.vendorName)
                } as ArrayList<String>
                list.forEach {
                    bindItem.tvVendorValue.text = it.trim()
                    vendor = it.trim()
                }
                it.vendor = if (!vendor.isNullOrBlank()) vendor.split("-")[1] else ""
                bindItem.tvBatchNoValue.text = it.lotId.trim()
                bindItem.tvNetWeightValue.text = it.stockQty.trim()
                if (it.grnDate.trim().isNotEmpty()) {
                    val day = it.grnDate.substring(6, 8)
                    val month = it.grnDate.substring(4, 6)
                    val year = it.grnDate.substring(0, 4)
                    bindItem.tvDateValue.text = day.plus("/").plus(month).plus("/").plus(year)
                }
                if (it.inventoryQC.isNotEmpty()) {
                    it.inventoryQC.forEach {
                        if (it.qcName.trim().equals("NICERTI")) ticket = it.value.trim()
                    }
                }
                bindItem.tvGrnTempIdValue.text = ticket
                bindItem.cbMtntItem.isChecked = it.isProgress
                bindItem.cvMtntItem.setOnClickListener { view ->
                    it.isProgress = !it.isProgress
                    binding.tvPrint.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
                    binding.tvPrint.isEnabled = true
                    bindItem.cbMtntItem.isChecked = it.isProgress
                    if (it.isProgress) setPrintData(it, pos) else removePrintItem(it)
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
        //setupAdapter(list)
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

    private fun getQualityDescriptionFromCode(gradeCode1: String?): String? {
        // val gradeCode = if (gradeCode1?.length ?: 0 >= 4) gradeCode1?.takeLast(4).toString() else ""
        val desc = materialQualityGradeList.filter { it.materialCode.equals(gradeCode1) }.map { it.grade }
        return if (desc.isNotEmpty()) gradeCode1.plus(" - ").plus(desc[0]) else gradeCode1
    }

    private fun setPrintData(receivingData: VegaNicTicketListModel, pos: Int) {
        val printModel = VegaNicTicketModel()
        printableList.clear()
        printModel.batchNumber = receivingData.lotId
        printModel.materialCode = receivingData.materialCode
        printModel.netWeight = receivingData.stockQty
        printModel.erdat = receivingData.grnDate

        if (printModel.grade.isNotEmpty())
            vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
                materialQualityGradeList = it.toMutableList()

            })
        vm.getMaterialQualityGrades(printModel.materialCode)
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
                    .inflate(com.olam.warehouse.login.R.layout.item_print_ticket_preview_nicaragua, null)
                val viewBinder = ItemPrintTicketPreviewNicaraguaBinding.bind(view)

                val mtnt = item.copy()
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
                if (mtnt.grade.isNotEmpty())
                    viewBinder.tvGradeValue.text = getQualityDescriptionFromCode(mtnt.grade)

                //if(mtnt.certificate.isEmpty())  viewBinder.tvCertificateValue.text = "NICERTD FT"
                viewBinder.tvCertificateValue.text = mtnt.certificate
                viewBinder.tvWeightValue.text = mtnt.netWeight
                // if(mtnt.transportVendorCode.isEmpty()) viewBinder.tvClientValue.text = "1123197 - ANTONIO"

                if (mtnt.materialName.contains("tolling"))
                    viewBinder.tvClientValue.text = mtnt.transportVendorCode
                else {
                    viewBinder.tvClient.gone()
                    viewBinder.tvClientValue.gone()
                }
                viewBinder.tvSacksValue.text = mtnt.bagCount
                bitmapPrintKeys.add(
                    bitmapToString(
                        getBitmapFromView(
                            view, Color.WHITE
                        )
                    )
                )

            }
            HandlerUtils.runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(bitmapPrintKeys))
                startActivity(
                    Intent(activity, WifiMainActivity::class.java).putExtra(
                        Constants.IS_SET_DEFAULT_SIZE,
                        true
                    )
                )
            }

        }.execute()
    }

    fun getPlantDetails(): Plant {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    }


}
