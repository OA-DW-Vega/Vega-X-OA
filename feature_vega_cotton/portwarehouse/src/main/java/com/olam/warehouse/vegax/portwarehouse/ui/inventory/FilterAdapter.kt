package com.olam.warehouse.portwarehouse.ui.inventory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.Grade
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.grade_row_item.view.*

class FilterAdapter(private val onClick: (Int) -> Unit, private val applicationContext: Context) :
    RecyclerView.Adapter<FilterAdapter.ItemViewHolder>() {

    private val mGrades = arrayListOf<Grade?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.grade_row_item,
            parent, false
        )
        return ItemViewHolder(v)
    }

    override fun getItemCount() = mGrades.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindItems(mGrades[position]?.grade)
        holder.itemView.tvRowItem.setOnClickListener {
            onClick(position)
        }

        when {
            mGrades[position]?.isSelect!! -> holder.itemView.tvRowItem.setBackgroundResource(R.drawable.border_corner_green_port)
            else -> holder.itemView.tvRowItem.setBackgroundResource(R.drawable.border_corner_port)
        }
    }

    class ItemViewHolder(private val iv: View) : RecyclerView.ViewHolder(iv) {
        fun bindItems(grade: String?) {
            iv.tvRowItem.text = grade
        }

    }

    fun addItems(grade: ArrayList<Grade>) {
        mGrades.clear()
        grade.let { mGrades.addAll(grade) }
        notifyDataSetChanged()
    }
}
