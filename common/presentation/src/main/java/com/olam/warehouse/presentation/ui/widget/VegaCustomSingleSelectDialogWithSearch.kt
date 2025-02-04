package com.olam.warehouse.presentation.ui.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.adapter.setUp
import kotlinx.android.synthetic.main.custom_single_select_dialog_layout.*
import kotlinx.android.synthetic.main.item_single_select.view.*


class VegaCustomSingleSelectDialogWithSearch(
    private var title: String,
    private var isWh: Boolean,
    private var isVendor: Boolean = false,
    private var isGrade: Boolean = false,
    private var items: List<String>,
    private var activity: Context,
    private var listener: VegaSingleSelectListener,
    private var isOBD: Boolean = false,
    private var isSupplier: Boolean = false,
    private var supplierListener: VegaCoffeeSingleSelectListener? = null,
    private var nicaraguaListener: VegaNicaraguaSingleSelectListener? = null,
    private var isLocation: Boolean = false,
    private var isIvSelection: Boolean = true,
    private var isOrigin: Boolean=false,
    private var isDepartment: Boolean=false
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
        setContentView(R.layout.custom_single_select_dialog_layout)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /*recyclerView = recycler_view
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = mLayoutManager*/
        tvTitle.text = title
        icDialogClose.setOnClickListener { dismiss() }
        setAdapter(items, listener, supplierListener, nicaraguaListener)
        //sv_sto_search.visibility = if (isWh) View.GONE else View.VISIBLE

        sv_sto_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                newText.let {
                    if (newText?.isEmpty() == true) {
                        setAdapter(items, listener, supplierListener, nicaraguaListener)
                    } else {
                        mSearchList.clear()
                        items.forEach { qtyWb ->
                            newText?.let { text ->
                                if (qtyWb.contains(text, true)) {
                                    mSearchList.add(qtyWb)
                                }
                            }
                        }
                        setAdapter(mSearchList, listener, supplierListener, nicaraguaListener)
                    }
                }
                return true
            }
        })

    }

    private fun setAdapter(
        item: List<String>,
        listener: VegaSingleSelectListener,
        supplierListener: VegaCoffeeSingleSelectListener?, nicaraguaListener: VegaNicaraguaSingleSelectListener?
    ) {
        val list1 = arrayListOf<String>()
//        list1.add("")
//        list1.add("")
        val item1 = item as MutableList
//        item1.addAll(list1)
        recycler_view.setUp(
            item1,
            R.layout.item_single_select,
            { it, pos ->
                textView.text = it
                fruitCardView.setOnClickListener { v ->
                    nicaraguaListener?.clickOnItem(it, isWh, isVendor, isGrade, isLocation)
                    listener.clickOnItem(it, isWh, isVendor, isGrade)
                    supplierListener?.clickOnItem(it, isWh, isVendor, isGrade, isSupplier,isOrigin,isDepartment)

                    if(isIvSelection) {
                        ivSelect.visibility = View.VISIBLE
                    }
                }
            })
    }


    override fun onClick(v: View) {
        dismiss()
    }
}
