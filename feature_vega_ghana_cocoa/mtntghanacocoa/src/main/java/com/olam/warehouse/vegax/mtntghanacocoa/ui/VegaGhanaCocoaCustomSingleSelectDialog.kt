package com.olam.warehouse.vegax.mtntghanacocoa.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.vegax.mtntghanacocoa.R
import kotlinx.android.synthetic.main.ghana_cocoa_custom_dialog_layout.*
import kotlinx.android.synthetic.main.ghana_cocoa_filter_item.view.*

class VegaGhanaCocoaCustomSingleSelectDialog(
    private var title: String,
    private var isWh: Boolean,
    private var isSendingWH: Boolean = false,
//    private var isMaterial: Boolean = false,
    private var items: ArrayList<String>,
    private var activity: Context,
    private var listenerCocoa: VegaGhanaCocoaSingleSelectListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private val mSearchList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.ghana_cocoa_custom_dialog_layout)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = recycler_view
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        tvTitle.text = title
        icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listenerCocoa)
        sv_sto_search.visibility = if (isWh) View.GONE else View.VISIBLE

        sv_sto_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                newText.let {
                    if (newText?.isEmpty() == true) {
                        setAdapter(items, listenerCocoa)
                    } else {
                        mSearchList.clear()
                        items.forEach { qtyWb ->
                            newText?.let { text ->
                                if (qtyWb.contains(text, true)) {
                                    mSearchList.add(qtyWb)
                                }
                            }
                        }
                        setAdapter(mSearchList, listenerCocoa)
                    }
                }
                return true
            }
        })

    }

    private fun setAdapter(
        item: ArrayList<String>,
        listenerCocoa: VegaGhanaCocoaSingleSelectListener
    ) {
        recycler_view.setUp(
            item,
            R.layout.ghana_cocoa_filter_item,
            { it, pos ->
                textView.text = it
                fruitCardView.setOnClickListener { v ->
                    listenerCocoa.clickOnItem(it, isWh, isSendingWH)
                    ivSelect.visibility = View.VISIBLE
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
