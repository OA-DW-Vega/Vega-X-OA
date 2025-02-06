package com.olam.warehouse.vegax.offloadingcameroon.ui

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
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.databinding.CameroonOffloadingCustomDialogLayoutBinding
import com.olam.warehouse.vegax.offloadingcameroon.databinding.CameroonOffloadingFilterItemBinding

class VegaCameroonOffloadingCustomSingleSelectDialog(
    private var title: String,
    private var isWh: Boolean,
    private var isProduct: Boolean,
    private var isSupplier:Boolean,
    private var items: ArrayList<String>,
    private var activity: Context,
    private var listener: VegaCameroonOffloadingSingleSelectListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private val mSearchList = ArrayList<String>()
    private lateinit var binding: CameroonOffloadingCustomDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CameroonOffloadingCustomDialogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = binding.recyclerView
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        binding.tvTitle.text = title
        binding.icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener)
        binding.svStoSearch.visibility = if (isWh) View.GONE else View.VISIBLE

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
        listener: VegaCameroonOffloadingSingleSelectListener
    ) {
        binding.recyclerView.setUpAdapter(
            item,
            R.layout.cameroon_offloading_filter_item,
            CameroonOffloadingFilterItemBinding::inflate,
            { it, pos, bindItem ->
                bindItem.textView.text = it
                bindItem.fruitCardView.setOnClickListener { v ->
                    listener.clickOnItem(it, isWh, isProduct, isSupplier)
                    bindItem.ivSelect.visibility = View.VISIBLE
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
