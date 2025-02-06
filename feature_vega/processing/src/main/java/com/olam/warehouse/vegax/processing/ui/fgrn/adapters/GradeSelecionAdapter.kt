package com.olam.warehouse.vegax.processing.ui.fgrn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaFgrnGrades
import com.olam.warehouse.vegax.processing.databinding.ItemVegaFgrnGradeSelectionBinding
import com.olam.warehouse.vegax.processing.ui.fgrn.VegaFgrnGradeSelectionFragment

class GradeSelecionAdapter(listener: VegaFgrnGradeSelectionFragment, processOrderDetails: List<VegaFgrnGrades>) :
    RecyclerView.Adapter<GradeSelecionAdapter.ItemViewHolder>() {

    private var mListener: ItemListener? = null
    private var processOrderSortDetail: List<VegaFgrnGrades>? = null

    init {
        mListener = listener
        processOrderSortDetail = processOrderDetails
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_fgrn_grade_selection, parent, false)
        val v = ItemVegaFgrnGradeSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(v)
    }

    override fun getItemCount() = processOrderSortDetail!!.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindItems(processOrderSortDetail, position)
    }

    inner class ItemViewHolder(private val iv: ItemVegaFgrnGradeSelectionBinding) :
        RecyclerView.ViewHolder(iv.root) {
        fun bindItems(item: List<VegaFgrnGrades>?, position: Int) {
            item?.let {
                iv.cbGrades.text = it[position].materialName
                iv.cbGrades.isChecked = it[position].isGradeChecked!!
                iv.cbGrades.isClickable = false
                itemView.setOnClickListener {
                    iv.cbGrades.isChecked = iv.cbGrades.isChecked
                    if (iv.cbGrades.isChecked) {
                        iv.cbGrades.isChecked = false
                        mListener!!.onItemUncheck(false, position)

                    } else {
                        iv.cbGrades.isChecked = true
                        mListener!!.onItemCheck(false, position)
                    }
                }
            }

        }

    }

    interface ItemListener {
        fun onItemCheck(item: Boolean, position: Int)
        fun onItemUncheck(item: Boolean, position: Int)

    }
}
