package com.olam.warehouse.presentation.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by SangiliPandian C on 16-11-2019.
 */
class Kadapter<in ITEM>(
    private val items: MutableList<ITEM>, layoutResId: Int,
    private val bindHolder: View.(ITEM, Int) -> Unit,
    private val itemClick: ITEM.() -> Unit = {}
) : AbstractAdapter<ITEM>(items, layoutResId) {

    override fun onItemClick(itemView: View, position: Int) {
        items[position].itemClick()
    }

    override fun View.bind(item: ITEM, position: Int) {
        bindHolder(item, position)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()
}

fun <ITEM> RecyclerView.setUp(
    items: MutableList<ITEM>,
    layoutResId: Int,
    bindHolder: View.(ITEM, Int) -> Unit,
    itemClick: ITEM.() -> Unit = {},
    manager: RecyclerView.LayoutManager = LinearLayoutManager(this.context)
): Kadapter<ITEM> {
    layoutManager = manager
    return Kadapter(items, layoutResId, bindHolder, itemClick).apply { adapter = this }
}
