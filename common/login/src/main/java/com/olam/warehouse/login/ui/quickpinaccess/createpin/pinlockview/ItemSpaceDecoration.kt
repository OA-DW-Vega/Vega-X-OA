package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
class ItemSpaceDecoration(
    horizontalSpaceWidth: Int,
    verticalSpaceHeight: Int,
    spanCount: Int,
    includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    private val mHorizontalSpaceWidth: Int
    private val mVerticalSpaceHeight: Int
    private val mSpanCount: Int
    private val mIncludeEdge: Boolean

    init {
        mHorizontalSpaceWidth = horizontalSpaceWidth
        mVerticalSpaceHeight = verticalSpaceHeight
        mSpanCount = spanCount
        mIncludeEdge = includeEdge
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position: Int = parent.getChildAdapterPosition(view)
        val column = position % mSpanCount
        if (mIncludeEdge) {
            outRect.right = mHorizontalSpaceWidth - column * mHorizontalSpaceWidth / mSpanCount
            outRect.left = (column + 1) * mHorizontalSpaceWidth / mSpanCount
            if (position < mSpanCount) {
                outRect.top = mVerticalSpaceHeight
            }
            outRect.bottom = mVerticalSpaceHeight
        } else {
            outRect.right = column * mHorizontalSpaceWidth / mSpanCount
            outRect.left = mHorizontalSpaceWidth - (column + 1) * mHorizontalSpaceWidth / mSpanCount
            if (position >= mSpanCount) {
                outRect.top = mVerticalSpaceHeight
            }
        }
    }


}
