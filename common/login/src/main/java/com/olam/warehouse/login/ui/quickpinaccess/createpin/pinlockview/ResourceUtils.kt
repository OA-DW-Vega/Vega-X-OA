package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */

class ResourceUtils private constructor() {
    companion object {
        fun getColor(context: Context?, @ColorRes id: Int): Int? {
            return context?.let { ContextCompat.getColor(it, id) }
        }

        fun getDimensionInPx(context: Context, @DimenRes id: Int): Float {
            return context.resources.getDimension(id)
        }

        fun getDrawable(context: Context?, @DrawableRes id: Int): Drawable? {
            return context?.let { ContextCompat.getDrawable(it, id) }
        }
    }

    init {
        throw AssertionError()
    }
}
