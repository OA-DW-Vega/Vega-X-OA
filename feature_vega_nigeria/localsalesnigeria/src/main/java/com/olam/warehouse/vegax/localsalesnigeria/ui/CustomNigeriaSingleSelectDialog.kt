package com.olam.warehouse.vegax.localsalesnigeria.ui

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
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.localsalesnigeria.R
import com.olam.warehouse.vegax.localsalesnigeria.databinding.CustomNigeriaDialogLayoutSalesBinding
import com.olam.warehouse.vegax.localsalesnigeria.databinding.FilterNigeriaItemsSalesBinding

class CustomNigeriaSingleSelectDialog(
    private var title: String, private var isWh: Boolean,
    private var items: ArrayList<String>,
    private var activity: Context,
    private var listener: NigeriaRecyclerViewItemClickListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private val mSearchList = ArrayList<String>()
    private lateinit var binding: CustomNigeriaDialogLayoutSalesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomNigeriaDialogLayoutSalesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = binding.recyclerView
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        binding.tvTitle.text = title
        binding.icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener)
        binding.svStoSearch.visible()
        binding.svStoSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
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
        listener: NigeriaRecyclerViewItemClickListener
    ) {
        binding.recyclerView.setUpAdapter(
            item,
            R.layout.filter_nigeria_items_sales,
            FilterNigeriaItemsSalesBinding::inflate,
            { it, pos, bindItem ->
                bindItem.textView.text = it
                bindItem.fruitCardView.setOnClickListener { v ->
                    listener.clickOnItem(it, isWh)
                    bindItem.ivSelect.visibility = View.VISIBLE
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
