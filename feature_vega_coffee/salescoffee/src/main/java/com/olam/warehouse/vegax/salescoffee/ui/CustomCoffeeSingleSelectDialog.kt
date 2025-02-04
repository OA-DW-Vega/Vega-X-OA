package com.olam.warehouse.vegax.salescoffee.ui

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
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.salescoffee.R
import kotlinx.android.synthetic.main.custom_coffee_dialog_layout_sales.*
import kotlinx.android.synthetic.main.filter_coffee_items_sales.view.*

class CustomCoffeeSingleSelectDialog(
    private var title: String, private var isWh: Boolean,
    private var items: ArrayList<String>,
    private var activity: Context,
    private var listener: RecyclerViewItemClickListener
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
        setContentView(R.layout.custom_coffee_dialog_layout_sales)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = recycler_view
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        tvTitle.text = title
        icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener)
        sv_sto_search.visible()
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
        item: ArrayList<String>,
        listener: RecyclerViewItemClickListener
    ) {
        recycler_view.setUp(
            item,
            R.layout.filter_coffee_items_sales,
            { it, pos ->
                textView.text = it
                fruitCardView.setOnClickListener { v ->
                    listener.clickOnItem(it, isWh)
                    ivSelect.visibility = View.VISIBLE
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
