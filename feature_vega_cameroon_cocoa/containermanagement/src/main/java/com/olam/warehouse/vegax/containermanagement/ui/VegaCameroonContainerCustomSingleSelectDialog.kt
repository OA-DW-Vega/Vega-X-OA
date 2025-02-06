package com.olam.warehouse.vegax.containermanagement.ui


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
import com.olam.warehouse.vegax.containermanagement.R
import com.olam.warehouse.vegax.containermanagement.databinding.CameroonContainerCustomDialogLayoutBinding
import com.olam.warehouse.vegax.containermanagement.databinding.CameroonContainerFilterItemBinding
import com.olam.warehouse.vegax.containermanagement.ui.addcontainer.VegaCameroonContainerCustomSingleSelectListener

class VegaCameroonContainerCustomSingleSelectDialog(
    private var title: String,
    private var isContainerSize: Boolean,
    private var isShippingLine: Boolean,
    private var items: ArrayList<String>,
    private var activity: Context,
    private var listener: VegaCameroonContainerCustomSingleSelectListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private val mSearchList = ArrayList<String>()
    private lateinit var binding: CameroonContainerCustomDialogLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CameroonContainerCustomDialogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = binding.recyclerView
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        binding.tvTitle.text = title
        binding.icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener)
        binding.svStoSearch.visibility = if (isContainerSize) View.GONE else View.VISIBLE

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
        listener: VegaCameroonContainerCustomSingleSelectListener
    ) {
        binding.recyclerView.setUpAdapter(
            item,
            R.layout.cameroon_container_filter_item,
            CameroonContainerFilterItemBinding::inflate,
            { it, pos, bindItem ->
                bindItem.textView.text = it
                bindItem.fruitCardView.setOnClickListener { v ->
                    listener.clickOnItem(it, isContainerSize, isShippingLine)
                    bindItem.ivSelect.visibility = View.VISIBLE
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
