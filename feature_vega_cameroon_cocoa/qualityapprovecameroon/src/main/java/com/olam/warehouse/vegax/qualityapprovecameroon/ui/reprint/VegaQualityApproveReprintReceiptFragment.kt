package com.olam.warehouse.vegax.qualityapprovecameroon.ui.reprint

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
import com.olam.warehouse.vegax.qualityapprovecameroon.R
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.VegaReprintList
import com.olam.warehouse.vegax.qualityapprovecameroon.databinding.FragmentVegaQaReprintBinding
import com.olam.warehouse.vegax.qualityapprovecameroon.databinding.ItemVegaQaReprintBinding
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.VegaQualityApproveCameroonViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaQualityApproveReprintReceiptFragment : BaseFragment() {

    private val vm: VegaQualityApproveCameroonViewModel by viewModel()

    private  var rePrintList: MutableList<VegaReprintList> = ArrayList()
    private var tallyPrintKeys = ArrayList<String>()
    override val layoutResourceId = R.layout.fragment_vega_qa_reprint
    private lateinit var binding: FragmentVegaQaReprintBinding
    var selectedItemId=""
    var type=""

    companion object {
        fun newInstance(type:String) = VegaQualityApproveReprintReceiptFragment().putArgs {
            putString("type",type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaQaReprintBinding.inflate(layoutInflater)
        return binding.root    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        clickEvent()
    }

    private fun clickEvent() {
        binding.tvPrint.setOnClickListener {
            vm.downloadSelectedItemPrint(selectedItemId)
        }
    }

    private fun initUI() {
           type= arguments?.getString("type","").toString()
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

    private fun bindPrintList(it: Resource<GenericReqAndResp<List<VegaReprintList>>>) {
        when (it.status) {
            Resource.Status.SUCCESS -> {
                if(rePrintList.isNotEmpty())
                rePrintList.clear()

                hideLoading()
                if (it.data?.data?.isNotEmpty()!!) {
                    val response = it.data?.data
                    rePrintList = response?.filter { it.fileType.equals(type) }
                        ?.sortedByDescending { desc -> desc.id }!!.toMutableList()
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
            R.layout.item_vega_qa_reprint,
            ItemVegaQaReprintBinding::inflate,
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
