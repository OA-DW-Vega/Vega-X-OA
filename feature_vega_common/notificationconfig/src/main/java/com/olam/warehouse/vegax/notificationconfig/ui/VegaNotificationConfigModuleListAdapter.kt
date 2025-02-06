package com.olam.warehouse.vegax.notificationconfig.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnGroupClickListener
import android.widget.ExpandableListView.OnGroupExpandListener
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.olam.warehouse.master.user.model.NotifyModuleList
import com.olam.warehouse.vegax.notificationconfig.R
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyModuleList
import com.olam.warehouse.vegax.notificationconfig.databinding.ItemModuleChildListBinding
import com.olam.warehouse.vegax.notificationconfig.databinding.ItemModuleParentListBinding


class VegaNotificationConfigModuleListAdapter internal constructor(
    private val context: Context,
    private val titleList: List<String>,
    public val dataList: HashMap<String, List<VegaNotifyModuleList>>
   /* var aleradySelectedString: HashMap<String, Boolean> = HashMap<String, Boolean>()*/
) : BaseExpandableListAdapter(), OnGroupExpandListener, OnGroupClickListener {
    companion object {
//        fun getSelectedList():HashMap<String, List<NotifyModuleList>>{
//            return selectorList
//        }

    }
    val selectorList: HashMap<String, List<VegaNotifyModuleList>> = dataList
    private var aleradySelectedString = HashMap<String, Boolean>()

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var groupBinding: ItemModuleParentListBinding
    private lateinit var itemBinding: ItemModuleChildListBinding

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.dataList[this.titleList[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        view: View?,
        parent: ViewGroup
    ): View {
        var convertView = view
        val holder: ItemViewHolder
        if (convertView == null) {
            itemBinding = ItemModuleChildListBinding.inflate(inflater)
            convertView = itemBinding.root
            holder = ItemViewHolder()
            holder.checkBox = itemBinding.expandedListItem
            convertView.tag = holder
        } else {
            holder = convertView.tag as ItemViewHolder
        }
        val expandedListText = getChild(listPosition, expandedListPosition) as VegaNotifyModuleList
        var isAlreadyChecked = selectorList.get(titleList.get(listPosition))?.get(expandedListPosition)?.isConfigured
        var pos = listPosition.toString().plus(expandedListPosition.toString())
        if (/*aleradySelectedString.get(pos) == true || */isAlreadyChecked == true) {
            holder.checkBox!!.isChecked = true
        } else {
            holder.checkBox!!.isChecked = false
        }
        holder.checkBox!!.text = expandedListText.moduleName
        holder.checkBox!!.setOnCheckedChangeListener { buttonView, isChecked ->

//            if (isChecked) {
//                aleradySelectedString.put(pos, isChecked)
//            }
//            if (isAlreadyChecked == false) {
//                selectorList.get(titleList.get(listPosition))?.get(expandedListPosition)?.isConfigured = true
////                dataList[this.titleList[listPosition]]!![expandedListPosition].isConfigured = true
//            }
        }
        holder.checkBox!!.setOnClickListener {
            if (/*aleradySelectedString.get(pos) == true*/isAlreadyChecked == true) {
                aleradySelectedString.put(pos, false)
//                selectorList.get(titleList.get(listPosition))?.get(expandedListPosition)?.isConfigured = false
//                selectorList[this.titleList[listPosition]]!![expandedListPosition].isConfigured = false
                var list = selectorList[this.titleList[listPosition]]
                list?.get(expandedListPosition)?.isConfigured = false
                selectorList[this.titleList[listPosition]] = list?: emptyList()
            } else {
                aleradySelectedString.put(pos, true)
//                selectorList.get(titleList.get(listPosition))?.get(expandedListPosition)?.isConfigured = true
//                selectorList[this.titleList[listPosition]]!![expandedListPosition].isConfigured = true
                var list = selectorList[this.titleList[listPosition]]
                list?.get(expandedListPosition)?.isConfigured = true
                selectorList[this.titleList[listPosition]] = list?: emptyList()
            }
        }
        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataList[this.titleList[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        view: View?,
        parent: ViewGroup
    ): View {
        var convertView = view
        val holder: GroupViewHolder
        if (convertView == null) {
            groupBinding = ItemModuleParentListBinding.inflate(inflater)
            convertView = groupBinding.root
            holder = GroupViewHolder()
            holder.title = groupBinding.listTitle
            holder.llLayout = groupBinding.llParent
            convertView.tag = holder
        } else {
            holder = convertView.tag as GroupViewHolder
        }
        val listTitle = getGroup(listPosition) as String
        holder.title!!.text = listTitle

        if (isExpanded) {
            holder.llLayout?.setBackgroundDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.layout_grey_border_with_light_grey))
        } else {
            holder.llLayout?.setBackgroundDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.layout_grey_border_with_white))
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

    inner class ItemViewHolder {
        internal var checkBox: CheckBox? = null
    }

    inner class GroupViewHolder {
        internal var title: TextView? = null
        internal var llLayout: LinearLayout? = null
    }

    override fun onGroupExpand(groupPosition: Int) {
    }

    override fun onGroupClick(parent: ExpandableListView?, view: View?, groupPosition: Int, id: Long): Boolean {
        view?.setBackgroundColor(context.resources.getColor(com.olam.warehouse.presentation.R.color.black))
        return false
    }

    fun getSelectedList():HashMap<String, List<VegaNotifyModuleList>>{
        return selectorList
    }
}
