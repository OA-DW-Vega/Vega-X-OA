package com.olam.warehouse.presentation.ui.widget

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
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.adapter.setUp
import kotlinx.android.synthetic.main.custom_filter_dialog.recycler_view
import kotlinx.android.synthetic.main.custom_filter_dialog.tvTitle
import kotlinx.android.synthetic.main.custom_single_select_dialog_layout.*
import kotlinx.android.synthetic.main.item_single_select.view.*

class VegaCommonSingleSelectDialogWithSearch(
    private var title: String,
    private var currentFalg: String,
    private var items: List<String>,
    private var activity: Context,
    private var listener: VegaSingleSelectCommonListener
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
        setContentView(R.layout.custom_single_select_dialog_layout)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = recycler_view
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        tvTitle.text = title
        icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener)
        //sv_sto_search.visibility = if (isWh) View.GONE else View.VISIBLE

        sv_sto_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                newText.let {
                    if (newText?.isEmpty() == true) {
                        setAdapter(items, listener)
                    } else {
                        mSearchList.clear()
                        items.forEach { qtyWb ->
                            newText?.let { text ->
                                if (qtyWb.contains(text, true)) {
                                    mSearchList.add(qtyWb)
                                }
                            }
                        }
                        setAdapter(mSearchList, listener)
                    }
                }
                return true
            }
        })

    }

    private fun setAdapter(
        item: List<String>,
        listener: VegaSingleSelectCommonListener
    ) {
        recycler_view.setUp(
            item as MutableList,
            R.layout.item_single_select,
            { it, pos ->
                textView.text = it
                fruitCardView.setOnClickListener { v ->
                    listener.clickOnItem(it, currentFalg)
                    ivSelect.visibility = View.VISIBLE
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
