package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

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
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.DialogOtFilterBinding
import com.olam.warehouse.vegax.portwarehouse.databinding.ItemSingleViewBinding


class DialogOTFilter (var items: ArrayList<String>,
var activity: Context, var
listener: OnClickItemListener
) :
Dialog(activity),
View.OnClickListener {
    private val mSearchList = ArrayList<String>()
    private var dialog: Dialog? = null

    internal var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var binding: DialogOtFilterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogOtFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = binding.recyclerView
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        setAdapter(items, listener)

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
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
        listener: OnClickItemListener
    ) {
        binding.recyclerView.setUpAdapter(
            item,
            R.layout.item_single_view,
            ItemSingleViewBinding::inflate,
            { it, pos, bindItem ->
                bindItem.itemText.text = it
                bindItem.fruitCardView.setOnClickListener { v ->
                    if (!it.toString().equals(activity.getString(R.string.select_ot_number))) {
                        listener.clickOnItem(pos, it.toString())
                        dismiss()
                    }

                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
