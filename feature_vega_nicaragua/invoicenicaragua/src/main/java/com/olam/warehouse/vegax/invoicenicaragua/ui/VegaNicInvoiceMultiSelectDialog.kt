package com.olam.warehouse.vegax.invoicenicaragua.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.databinding.InvoiceGrnMultiSelectDialogLayoutBinding
import com.olam.warehouse.vegax.invoicenicaragua.databinding.ItemVegaNicaraguaInvoiceGrnBinding

class VegaNicInvoiceMultiSelectDialog(
    private var items: ArrayList<GrnDetails>,
    private var activity: Context,
    private var listener: MultiSelectCallBackListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mGrnList = mutableListOf<GrnDetails>()
    private lateinit var binding: InvoiceGrnMultiSelectDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = InvoiceGrnMultiSelectDialogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mGrnList = items
        recyclerView = binding.recyclerView
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        binding.icDialogClose.setOnClickListener { dismiss() }
        setAdapter(mGrnList, listener)
        binding.tvProceed.setOnClickListener {
            val selectedList = mGrnList.filter { it.isSelected }
            listener.updateSelectedGrn(selectedList)
            dismiss()
        }
    }

    private fun setAdapter(
        item: MutableList<GrnDetails>,
        listener: MultiSelectCallBackListener
    ) {
        binding.recyclerView.setUpAdapter(
            item,
            R.layout.item_vega_nicaragua_invoice_grn,
            ItemVegaNicaraguaInvoiceGrnBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvGrnTempIdValue.text = it.grnNumber
                bindItem.tvBatchNoValue.text = it.batchNumber
                bindItem.tvNetWeightValue.text = it.grnQty?.trim().plus(" ").plus(it.unitsOfMeasure)
                bindItem.cbGrnItem.isChecked = it.isSelected
                if (it.isGrnInOffline) {
                    bindItem.cbGrnItem.gone()
                    bindItem.clLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            bindItem.clLayout.context,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    )
                }
                bindItem.cvGrnItem.setOnClickListener { view ->
                    if (!it.isGrnInOffline) {
                        it.isSelected = !it.isSelected
                        bindItem.cbGrnItem.isChecked = it.isSelected
                        if (it.isSelected)
                            mGrnList.find { item -> item.grnNumber.equals(it.grnNumber) }
                                .apply { it.isSelected = true }
                        else
                            mGrnList.find { item -> item.grnNumber.equals(it.grnNumber) }
                                .apply { it.isSelected = false }
                    } else {
                        context?.toast("This Grn is added in another offline transaction")
                    }
                }
            })
    }

    override fun onClick(v: View) {
        dismiss()
    }
}
