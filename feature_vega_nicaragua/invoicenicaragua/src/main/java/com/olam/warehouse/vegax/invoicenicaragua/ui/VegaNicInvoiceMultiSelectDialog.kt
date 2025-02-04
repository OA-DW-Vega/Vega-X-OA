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
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.invoicenicaragua.R
import kotlinx.android.synthetic.main.invoice_grn_multi_select_dialog_layout.*
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_grn.view.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.invoice_grn_multi_select_dialog_layout)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mGrnList = items
        recyclerView = recycler_view
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        icDialogClose.setOnClickListener { dismiss() }
        setAdapter(mGrnList, listener)
        tvProceed.setOnClickListener {
            val selectedList = mGrnList.filter { it.isSelected }
            listener.updateSelectedGrn(selectedList)
            dismiss()
        }
    }

    private fun setAdapter(
        item: MutableList<GrnDetails>,
        listener: MultiSelectCallBackListener
    ) {
        recycler_view.setUp(
            item,
            R.layout.item_vega_nicaragua_invoice_grn,
            { it, pos ->
                tvGrnTempIdValue.text = it.grnNumber
                tvBatchNoValue.text = it.batchNumber
                tvNetWeightValue.text = it.grnQty?.trim().plus(" ").plus(it.unitsOfMeasure)
                cbGrnItem.isChecked = it.isSelected
                if (it.isGrnInOffline) {
                    cbGrnItem.gone()
                    clLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            clLayout.context,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    )
                }
                cvGrnItem.setOnClickListener { view ->
                    if (!it.isGrnInOffline) {
                        it.isSelected = !it.isSelected
                        cbGrnItem.isChecked = it.isSelected
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
