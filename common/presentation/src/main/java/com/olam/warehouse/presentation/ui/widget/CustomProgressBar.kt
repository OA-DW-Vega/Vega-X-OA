package com.olam.warehouse.presentation.ui.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.NonNull
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.databinding.FragmentBaseContentBinding
import com.olam.warehouse.presentation.databinding.ProgressBarBinding
import com.olam.warehouse.presentation.utils.extension.visible

/**
 * Created by SangiliPandian C on 26-08-2019.
 */

class CustomProgressBar {

    var dialog: Dialog? = null

    fun show(context: Context): Dialog? {
        return show(context, null)
    }

    @SuppressLint("InflateParams")
    fun show(context: Context, title: CharSequence?): Dialog? {
        val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = ProgressBarBinding.inflate(inflator)
        if (title != null) {
            view.cpTitle.text = title
        }
        view.cpBgView.setBackgroundColor(Color.parseColor("#60000000")) //Background Color
        view.cpCardview.setCardBackgroundColor(Color.parseColor("#70000000")) //Box Color
        /*setColorFilter(
            view.cp_pbar.indeterminateDrawable,
            ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null)
        ) //Progress Bar Color*/
        view.cpTitle.setTextColor(Color.WHITE) //Text Color

        dialog = Dialog(context, R.style.CustomProgressBarTheme)
        dialog?.setContentView(view.root)
        dialog?.setCancelable(false)
        dialog?.show()

        return dialog
    }

    @SuppressLint("InflateParams")
    fun showLoading(context: Context): Dialog? {
        val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = FragmentBaseContentBinding.inflate(inflator)
        view.progressbar.visible()
        dialog = Dialog(context, R.style.CustomProgressBarTheme)
        dialog?.setContentView(view.root)
//        dialog?.setCancelable(false)
        dialog?.show()

        return dialog
    }

    private fun setColorFilter(@NonNull drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

}
