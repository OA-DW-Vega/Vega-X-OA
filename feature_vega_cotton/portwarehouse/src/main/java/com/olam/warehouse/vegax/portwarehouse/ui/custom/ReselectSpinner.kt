package com.olam.warehouse.vegax.portwarehouse.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

class ReselectSpinner : AppCompatSpinner {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun setSelection(position: Int) {
        val isSameSelected = position == selectedItemPosition
        super.setSelection(position)
        if (isSameSelected)
            onItemSelectedListener?.onItemSelected(this, selectedView, position, 0)
    }


}