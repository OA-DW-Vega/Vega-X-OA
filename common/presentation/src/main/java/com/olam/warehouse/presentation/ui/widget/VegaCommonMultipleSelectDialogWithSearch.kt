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
import com.olam.warehouse.presentation.databinding.CustomMultiSelectDialogLayoutBinding
import com.olam.warehouse.presentation.databinding.ItemMultiSelectBinding
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible

class VegaCommonMultipleSelectDialogWithSearch (
    private var title: String,
    private var isSelected: String,
    private var items: List<String>,
    private var activity: Context,
    private var listener: VegaMultiSelectCommonListener
) :
    Dialog(activity),
    View.OnClickListener {
    private var dialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private val mSearchList = ArrayList<MatrialItem>()
    private val mMaterialList = ArrayList<MatrialItem>()
    private lateinit var binding: CustomMultiSelectDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomMultiSelectDialogLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = binding.recyclerView
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager
        binding.tvTitle.text = title
        binding.icDialogClose.setOnClickListener { dismiss() }
        items.forEach {
            mMaterialList.add(MatrialItem(name = it))

        }
        binding.btOk.setOnClickListener {
            val selectedItem = mMaterialList.filter { it.isSelected }.map { it.name }

            listener.clickOnItem(selectedItem, true)
        }
        setAdapter(mMaterialList, listener)
        //sv_sto_search.visibility = if (isWh) View.GONE else View.VISIBLE

        binding.svStoSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                newText.let {
                    if (newText?.isEmpty() == true) {
                        setAdapter(mMaterialList, listener)
                    } else {
                        mSearchList.clear()
                        mMaterialList.forEach { qtyWb ->
                            newText?.let { text ->
                                if (qtyWb.name.contains(text, true)) {
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
        item: List<MatrialItem>,
        listener: VegaMultiSelectCommonListener
    ) {
        binding.recyclerView.setUpAdapter(
            item as MutableList,
            R.layout.item_multi_select,
            ItemMultiSelectBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.textView.text = it.name
                if (it.isSelected) bindingItem.ivSelect.visible()
                else bindingItem.ivSelect.gone()
                bindingItem.matrialCardView.setOnClickListener { v ->
                    it.isSelected = !it.isSelected
                    recyclerView?.adapter?.notifyItemChanged(pos)
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}

data class MatrialItem(
    var name:String="",
    var isSelected:Boolean=false
)
