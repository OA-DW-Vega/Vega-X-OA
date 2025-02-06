package com.olam.warehouse.vegax.portwarehouse.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleGrade
import com.olam.warehouse.vegax.portwarehouse.databinding.RowInventoryGradesBinding

class InventoryGradeAdapter(private val gradeList:MutableList<BaleGrade>,
                            private val onGradeCheck: () -> Unit,
                            private val onGradeSync: (grade:String) -> Unit): RecyclerView.Adapter<InventoryGradeAdapter.InventoryGradeViewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryGradeViewholder {
        /* val view = LayoutInflater.from(parent.context).inflate(R.layout.row_inventory_grades,
             parent,false)*/
        val v =
            RowInventoryGradesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryGradeViewholder(v)
    }

    override fun getItemCount(): Int {
        return gradeList.size
    }

    override fun onBindViewHolder(holder: InventoryGradeViewholder, position: Int) {
        holder.bindRow(gradeList[position])
    }

    inner class InventoryGradeViewholder(itemView: RowInventoryGradesBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindRow(baleGrade: BaleGrade) {
            binding.cbGrade.isChecked = baleGrade.isChecked
            baleGrade.apply {
                binding.tvGrade.text = grade
                binding.tvBaleWeight.text = "$netWeight KG"
                binding.tvNoOfBales.text = count.toString()
            }
            binding.cbGrade.setOnClickListener {
                changeSelection(adapterPosition)
            }
            binding.ivSync.setOnClickListener {
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
