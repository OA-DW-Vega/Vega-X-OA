package com.olam.warehouse.vegax.mtntindo.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class DividerSpaceItemDecorationIndoCoffee(
    private val verticalSpaceHeight: Int,
    private val dispatchLotsList: MutableList<VegaEcuadorDispatchLots>
) :
    RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val currentItemPos = parent.getChildAdapterPosition(view)
        val nextItemPos = currentItemPos + 1

        if (nextItemPos < dispatchLotsList.size &&
            dispatchLotsList[currentItemPos].pairId != dispatchLotsList[nextItemPos].pairId
        )
            outRect.bottom = verticalSpaceHeight
        else {
            if (dispatchLotsList[currentItemPos].pairId!! > 0)
                outRect.bottom = 0
            else
                outRect.bottom = verticalSpaceHeight
        }
    }
}
