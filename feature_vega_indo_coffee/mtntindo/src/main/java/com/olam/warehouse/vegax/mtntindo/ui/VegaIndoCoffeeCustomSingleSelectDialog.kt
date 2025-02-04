package com.olam.warehouse.vegax.mtntindo.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.vegax.mtntindo.R
import com.olam.warehouse.vegax.mtntindo.ui.callback.VegaIndoCoffeeRecyclerViewItemClickListener
import kotlinx.android.synthetic.main.vega_indo_coffee_custom_dialog_layout.*
import kotlinx.android.synthetic.main.vega_indo_coffee_filter_items.view.*

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeCustomSingleSelectDialog(
    private var title: String, private var isWh: Boolean,
    private var items: ArrayList<String>,
    private var activity: Context,
    private var listener: VegaIndoCoffeeRecyclerViewItemClickListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.vega_indo_coffee_custom_dialog_layout)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = recycler_view
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        tvTitle.text = title
        icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener)

    }

    private fun setAdapter(
        item: ArrayList<String>,
        listener: VegaIndoCoffeeRecyclerViewItemClickListener
    ) {
        recycler_view.setUp(
            item,
            R.layout.vega_indo_coffee_filter_items,
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

