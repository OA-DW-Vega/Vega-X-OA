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
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.databinding.CustomSingleSelectDialogLayoutBinding
import com.olam.warehouse.presentation.databinding.ItemSingleSelectBinding
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.visible

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
    private lateinit var binding: CustomSingleSelectDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomSingleSelectDialogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = binding.recyclerView
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        binding.tvTitle.text = title
        binding.icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener)
        //sv_sto_search.visibility = if (isWh) View.GONE else View.VISIBLE

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
        item: List<String>,
        listener: VegaSingleSelectCommonListener
    ) {
        if(item.isNotEmpty()) {
            binding.recyclerView.visible()
            binding.recyclerView.setUpAdapter(
                item as MutableList,
                R.layout.item_single_select,
                ItemSingleSelectBinding::inflate,
                { it, pos, bindingItem ->
                    bindingItem.textView.text = it
                    bindingItem.fruitCardView.setOnClickListener { v ->
                        listener.clickOnItem(it, currentFalg)
                        bindingItem.ivSelect.visibility = View.VISIBLE
                    }
                })
        } else {
            binding.recyclerView.invisible()
        }
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
