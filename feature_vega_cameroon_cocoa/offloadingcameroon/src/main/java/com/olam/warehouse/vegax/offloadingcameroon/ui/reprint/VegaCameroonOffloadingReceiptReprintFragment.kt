package com.olam.warehouse.vegax.offloadingcameroon.ui.reprint

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaReprintList
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentVegaOffloadingReprintBinding
import com.olam.warehouse.vegax.offloadingcameroon.databinding.ItemVegaOffloadingReprintBinding
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaCameroonOffloadingReceiptReprintFragment : BaseFragment() {

    private val vm: VegaCameroonOffloadingViewModel by viewModel()
    private  var rePrintList: MutableList<VegaReprintList> = ArrayList()
    private var tallyPrintKeys = ArrayList<String>()

    override val layoutResourceId = R.layout.fragment_vega_offloading_reprint
    private lateinit var binding: FragmentVegaOffloadingReprintBinding

    var selectedItemId=""
    var type=""

    companion object {
        fun newInstance(type:String) = VegaCameroonOffloadingReceiptReprintFragment().putArgs {
            putString("Type",type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaOffloadingReprintBinding.inflate(layoutInflater)

        initUI()
        clickEvent()
        return binding.root
    }


    private fun clickEvent() {
        binding.tvPrint.setOnClickListener {
            vm.downloadSelectedItemPrint(selectedItemId)
        }
    }

    private fun initUI() {
        type= arguments?.getString("Type","").toString()
        vm.getReprintList()
        vm.reprintList.observe(viewLifecycleOwner, Observer { 
           bindPrintList(it)  
        })

        vm.printDownload.observe(viewLifecycleOwner, Observer {
            tallyPrintKeys.clear()
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if (it.data != null && it.data?.data != null && it.data?.data?.isNotEmpty() == true) {
                        tallyPrintKeys.add(it.data?.data!!)
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
        })
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
                            HandlerUtils.runOnUiThread {
                                showPreviewDialog()
                            }
                    }.execute()
                },
                { dismiss() })
        }
    }

    private fun showPreviewDialog() {
        DoAsync {
            HandlerUtils.runOnUiThread {
                hideLoading()
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(requireContext(), WifiMainActivity::class.java))
            }
        }.execute()

    }



    private fun bindPrintList(it: Resource<GenericReqAndResp<List<VegaReprintList>>>) {
        when (it.status) {
            Resource.Status.SUCCESS -> {
                rePrintList.clear()
                hideLoading()
                if (it.data?.data?.isNotEmpty()!!) {
                    val response = it.data?.data
                    rePrintList = response?.filter { it.fileType.equals(type) }
                            ?.sortedByDescending { desc -> desc.id } as MutableList<VegaReprintList>
                    setAdapter(rePrintList)
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

    private fun setAdapter(rePrintList1: MutableList<VegaReprintList>) {
             if(rePrintList1.isNotEmpty()){
                 binding.rvTransaction.visible()
                 binding.tvPrint.visible()
                 binding.tvNoData.gone()
             }else{
                 binding.rvTransaction.gone()
                 binding.tvPrint.gone()
                 binding.tvNoData.visible()
             }

        binding.rvTransaction.setUpAdapter(
            rePrintList1,
            R.layout.item_vega_offloading_reprint,
            ItemVegaOffloadingReprintBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvGrnTempIdValue.text = it.moduleNo
                bindItem.tvGrn.text = context.getString(R.string.grn_no)
                bindItem.tvBatchNoValue.text = it.transactionNo
                bindItem.tvDateValue.text = DateUtils.getUTCDateTime(it.date, context = requireContext())
                bindItem.tvMaterialValue.text = it.materialName
                bindItem.cbItem.isChecked = it.isProgress
                bindItem.cvItem.setOnClickListener { view ->
                    val oldPos = rePrintList1.indexOf(rePrintList1.find { it.isProgress == true })
                    it.isProgress = !it.isProgress
                    binding.tvPrint.setBackgroundColor(context.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
                    binding.tvPrint.isEnabled = true
                    bindItem.cbItem.isChecked = it.isProgress
                    if (it.isProgress) {
                        selectedItemId = it.id.toString()
                        removeCheckedList(pos, rePrintList1, oldPos)
                    }
                }
            },
            itemClick = {
            })
    }


      private fun removeCheckedList(pos: Int, list: MutableList<VegaReprintList>, oldpos: Int) {
          list.forEach { it.isProgress = false }
          list[pos].isProgress = true
          binding.rvTransaction.adapter?.notifyItemChanged(oldpos)
          binding.rvTransaction.adapter?.notifyItemChanged(pos)
      }




}
