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
import com.olam.warehouse.presentation.databinding.CustomFilterDialogBinding
import com.olam.warehouse.presentation.databinding.FilterItemsCommonBinding

class CustomFilterDialog(
    var items: ArrayList<String>,
    var activity: Context, var
    listener: RecyclerViewItemClickListener
) :
    Dialog(activity),
    View.OnClickListener {
    private val mSearchList = ArrayList<String>()
    private var dialog: Dialog? = null

    internal var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var binding: CustomFilterDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomFilterDialogBinding.inflate(layoutInflater)
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
        listener: RecyclerViewItemClickListener
    ) {
        binding.recyclerView.setUpAdapter(
            item,
            R.layout.filter_items_common,
            FilterItemsCommonBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.textView.text = it
                bindingItem.fruitCardView.setOnClickListener { v ->
                    listener.clickOnItem(
                        items.indexOf(
                            it
                        )
                    )
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
