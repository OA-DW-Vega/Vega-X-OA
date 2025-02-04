package com.olam.warehouse.portwarehouse.ui.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleGrade
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.row_inventory_grades.view.*

class InventoryGradeAdapter(private val gradeList:MutableList<BaleGrade>,
                            private val onGradeCheck: () -> Unit,
                            private val onGradeSync: (grade:String) -> Unit): RecyclerView.Adapter<InventoryGradeAdapter.InventoryGradeViewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryGradeViewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_inventory_grades,
            parent,false)

        return InventoryGradeViewholder(view)
    }

    override fun getItemCount(): Int {
        return gradeList.size
    }

    override fun onBindViewHolder(holder: InventoryGradeViewholder, position: Int) {
        holder.bindRow(gradeList[position])
    }

    inner class InventoryGradeViewholder(itemView: View):RecyclerView.ViewHolder(itemView) {

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

    private fun changeSelection(position:Int) {

        val grade = gradeList[position]
        grade.isChecked = !grade.isChecked
        onGradeCheck()
        notifyDataSetChanged()
    }

    fun getItem(): MutableList<BaleGrade> {
        return gradeList
    }

    fun getSelectedGrades(): ArrayList<String> {
        val grades:ArrayList<String> = arrayListOf()

        gradeList.forEach {
            if(it.isChecked) {
                grades.add(it.grade)
            }
        }

        return grades
    }


}
