package com.olam.warehouse.vegax.gateentryghanacocoa.ui

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
import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.vegax.gateentryghanacocoa.R
import com.olam.warehouse.vegax.gateentryghanacocoa.databinding.CustomGhSingleSelectDialogLayoutBinding
import com.olam.warehouse.vegax.gateentryghanacocoa.databinding.ItemGhSingleSelectBinding


class VegaGhanaCommonSingleDiolog(
    private var title: String,
    private var currentFalg: String,
    private var items: List<VegaReceivingMtn>,
    private var activity: Context,
    private var listener: VegaSingleSelectCommonListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private val mSearchList = ArrayList<VegaReceivingMtn>()
    private lateinit var binding: CustomGhSingleSelectDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomGhSingleSelectDialogLayoutBinding.inflate(layoutInflater)
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
                                if (qtyWb.mtnNumber.contains(text, true)) {
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
        item: List<VegaReceivingMtn>,
        listener: VegaSingleSelectCommonListener
    ) {
        binding.recyclerView.setUpAdapter(
            item as MutableList,
            R.layout.item_gh_single_select,
            ItemGhSingleSelectBinding::inflate,
            { it, pos, bindItem ->
                if (it.gateEntry?.isNotEmpty() == true) {
                    bindItem.textView.setTextColor(Color.GREEN)
                }
                bindItem.textView.text = it.mtnNumber
                /*fruitCardView.setOnClickListener { v ->
                    listener.clickOnItem(it.materialNumber, currentFalg)
                    ivSelect.visibility = View.VISIBLE
                }*/
            }, {
                listener.clickOnItem(this.mtnNumber, currentFalg)
                //ivSelect.visibility = View.VISIBLE
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
