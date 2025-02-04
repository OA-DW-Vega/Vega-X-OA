package com.olam.warehouse.ginning.ui.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.ginning.data.model.BaleGrade
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.ginning_row_inventory_grades.view.*

class InventoryGradeAdapter(
    private val gradeList: MutableList<BaleGrade>,
    private val onGradeCheck: () -> Unit,
    private val onGradeSync: (grade: String) -> Unit
) : RecyclerView.Adapter<InventoryGradeAdapter.InventoryGradeViewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryGradeViewholder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.ginning_row_inventory_grades,
            parent, false
        )

        return InventoryGradeViewholder(view)
    }

    override fun getItemCount(): Int {
        return gradeList.size
    }

    override fun onBindViewHolder(holder: InventoryGradeViewholder, position: Int) {
        holder.bindRow(gradeList[position])
    }

    inner class InventoryGradeViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindRow(baleGrade: BaleGrade) {
            itemView.cbGrade.isChecked = baleGrade.isChecked
            baleGrade.apply {
                itemView.tvGrade.text = grade
                itemView.tvBaleWeight.text = "$netWeight KG"
                itemView.tvNoOfBales.text = count.toString()
            }
            itemView.cbGrade.setOnClickListener {
                changeSelection(adapterPosition)
            }
            itemView.ivSync.setOnClickListener {
                onGradeSync(baleGrade.grade)
            }
        }
    }

    private fun changeSelection(position: Int) {

        val grade = gradeList[position]
        grade.isChecked = !grade.isChecked
        onGradeCheck()
        notifyDataSetChanged()
    }

    fun getItem(): ArrayList<BaleGrade> {
        return gradeList as ArrayList<BaleGrade>
    }

    fun getSelectedGrades(): ArrayList<String> {
        val grades: ArrayList<String> = arrayListOf()

        gradeList.forEach {
            if (it.isChecked) {
                grades.add(it.grade)
            }
        }

        return grades
    }


}
