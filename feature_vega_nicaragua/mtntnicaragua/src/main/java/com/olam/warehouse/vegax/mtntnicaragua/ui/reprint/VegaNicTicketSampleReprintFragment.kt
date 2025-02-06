package com.olam.warehouse.vegax.mtntnicaragua.ui.reprint

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import com.afollestad.materialdialogs.MaterialDialog
import com.microsoft.appcenter.utils.HandlerUtils

import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaMtnrReprintList
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicTicketReprintBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaMtntReprintBinding
import com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntViewModel
import com.olam.warehouse.vegax.mtntnicaragua.utils.MTNR_RECEIPT
import com.olam.warehouse.vegax.mtntnicaragua.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class VegaNicTicketSampleReprintFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_mtnt_reprint
    private lateinit var binding: FragmentVegaNicaraguaMtntReprintBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var printable = arrayListOf<VegaMtnrReprintList>()
    private var isMultipleAdd = false
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
        binding.tvGrnHeading.text = getString(R.string.re_print_ticket_sample)
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }

    private fun initUI() {
        try {
            vm.mtnrReprintList.observe(viewLifecycleOwner, { updatePrintListdetails(it) })
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
                            UIUtils.showErrorDialog(requireContext(), getString(R.string.no_data_found))
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
                        response?.filter { it.fileType.equals("Other") }
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
                bindItem.tvGrnTempIdValue.text = it.moduleNo!!.replace("\\", "")
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

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (tallyPrintKeys.size > 0)
                        showPreviewDialog()
                },
                { dismiss() })
        }
    }

    private fun showPreviewDialog() {
        showCustomLoading()
        DoAsync {
            HandlerUtils.runOnUiThread {
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

}

