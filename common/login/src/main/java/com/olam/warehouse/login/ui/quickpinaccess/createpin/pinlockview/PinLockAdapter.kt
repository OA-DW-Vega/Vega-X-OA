package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.R

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
class PinLockAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
    private var mOnNumberClickListener: OnNumberClickListener? = null
    private var mOnDeleteClickListener: OnDeleteClickListener? = null
    private var mPinLength = 0
    private val BUTTON_ANIMATION_DURATION = 150
    private var mKeyValues: IntArray
    private var mTypeface: Typeface? = null
    fun setTypeFace(typeFace: Typeface?) {
        mTypeface = typeFace
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        viewHolder = if (viewType == VIEW_TYPE_NUMBER) {
            val view: View = inflater.inflate(R.layout.layout_number_item, parent, false)
            NumberViewHolder(view, mTypeface)
        } else {
            val view: View = inflater.inflate(R.layout.layout_delete_item, parent, false)
            DeleteViewHolder(view)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType === VIEW_TYPE_NUMBER) {
            val vh1 = holder as NumberViewHolder
            configureNumberButtonHolder(vh1, position)
        } else if (holder.itemViewType === VIEW_TYPE_DELETE) {
            val vh2 = holder as DeleteViewHolder
            configureDeleteButtonHolder(vh2)
        }
    }

    private fun configureNumberButtonHolder(holder: NumberViewHolder?, position: Int) {
        if (holder != null) {
            if (position == 9) {
                holder.mNumberButton.visibility = View.GONE
            } else {
                holder.mNumberButton.text = mKeyValues[position].toString()
                holder.mNumberButton.visibility = View.VISIBLE
                holder.mNumberButton.tag = mKeyValues[position]
            }
            if (mCustomizationOptionsBundle != null) {
                holder.mNumberButton.setTextColor(mCustomizationOptionsBundle!!.getTextColor())
                if (mCustomizationOptionsBundle!!.getButtonBackgroundDrawable() != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.mNumberButton.setBackgroundDrawable(
                            mCustomizationOptionsBundle!!.getButtonBackgroundDrawable()
                        )
                    } else {
                        holder.mNumberButton.background = mCustomizationOptionsBundle!!.getButtonBackgroundDrawable()
                    }
                }
                holder.mNumberButton.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mCustomizationOptionsBundle!!.getTextSize().toFloat()
                )
                val params = LinearLayout.LayoutParams(
                    mCustomizationOptionsBundle!!.getButtonSize(),
                    mCustomizationOptionsBundle!!.getButtonSize()
                )
                holder.mNumberButton.layoutParams = params
            }
        }
    }

    private fun configureDeleteButtonHolder(holder: DeleteViewHolder?) {
        if (holder != null) {
            if (mCustomizationOptionsBundle!!.isShowDeleteButton() && mPinLength > 0) {
                holder.mButtonImage.visibility = View.VISIBLE
                if (mCustomizationOptionsBundle!!.getDeleteButtonDrawable() != null) {
                    holder.mButtonImage.setImageDrawable(mCustomizationOptionsBundle!!.getDeleteButtonDrawable())
                }
                holder.mButtonImage.setColorFilter(
                    mCustomizationOptionsBundle!!.getTextColor(),
                    PorterDuff.Mode.SRC_ATOP
                )
                val params = LinearLayout.LayoutParams(
                    mCustomizationOptionsBundle!!.getDeleteButtonWidthSize(),
                    mCustomizationOptionsBundle!!.getDeleteButtonHeightSize()
                )
                holder.mButtonImage.layoutParams = params
            }
        }
    }

    override fun getItemCount(): Int {
        return 12
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            VIEW_TYPE_DELETE
        } else VIEW_TYPE_NUMBER
    }

    fun getPinLength(): Int {
        return mPinLength
    }

    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
    }

    fun getKeyValues(): IntArray {
        return mKeyValues
    }

    fun setKeyValues(keyValues: IntArray) {
        mKeyValues = getAdjustKeyValues(keyValues)
        notifyDataSetChanged()
    }

    private fun getAdjustKeyValues(keyValues: IntArray): IntArray {
        val adjustedKeyValues = IntArray(keyValues.size + 1)
        for (i in keyValues.indices) {
            if (i < 9) {
                adjustedKeyValues[i] = keyValues[i]
            } else {
                adjustedKeyValues[i] = -1
                adjustedKeyValues[i + 1] = keyValues[i]
            }
        }
        return adjustedKeyValues
    }

    fun getOnItemClickListener(): OnNumberClickListener? {
        return mOnNumberClickListener
    }

    fun setOnItemClickListener(onNumberClickListener: OnNumberClickListener?) {
        mOnNumberClickListener = onNumberClickListener
    }

    fun getOnDeleteClickListener(): OnDeleteClickListener? {
        return mOnDeleteClickListener
    }

    fun setOnDeleteClickListener(onDeleteClickListener: OnDeleteClickListener?) {
        mOnDeleteClickListener = onDeleteClickListener
    }

    fun getCustomizationOptions(): CustomizationOptionsBundle? {
        return mCustomizationOptionsBundle
    }

    fun setCustomizationOptions(customizationOptionsBundle: CustomizationOptionsBundle?) {
        mCustomizationOptionsBundle = customizationOptionsBundle
    }

    interface OnNumberClickListener {
        fun onNumberClicked(keyValue: Int)
    }

    interface OnDeleteClickListener {
        fun onDeleteClicked()
        fun onDeleteLongClicked()
    }

    inner class NumberViewHolder(itemView: View, font: Typeface?) :
        RecyclerView.ViewHolder(itemView) {
        var mNumberButton: Button

        init {
            mNumberButton = itemView.findViewById<View>(R.id.button) as Button
            if (font != null) {
                mNumberButton.typeface = font
            }
            mNumberButton.setOnClickListener { v ->
                if (mOnNumberClickListener != null) {
                    mOnNumberClickListener!!.onNumberClicked(v.tag as Int)
                }
            }
            mNumberButton.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    mNumberButton.startAnimation(scale())
                }
                false
            }
        }
    }

    inner class DeleteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mDeleteButton: LinearLayout
        var mButtonImage: ImageView

        init {
            mDeleteButton = itemView.findViewById<View>(R.id.button) as LinearLayout
            mButtonImage = itemView.findViewById<View>(R.id.buttonImage) as ImageView
            if (mCustomizationOptionsBundle!!.isShowDeleteButton() && mPinLength > 0) {
                mDeleteButton.setOnClickListener {
                    if (mOnDeleteClickListener != null) {
                        mOnDeleteClickListener!!.onDeleteClicked()
                    }
                }
                mDeleteButton.setOnLongClickListener {
                    if (mOnDeleteClickListener != null) {
                        mOnDeleteClickListener!!.onDeleteLongClicked()
                    }
                    true
                }
                mDeleteButton.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        mDeleteButton.startAnimation(scale())
                    }
                    false
                }
            }
        }
    }

    private fun scale(): Animation {
        val scaleAnimation = ScaleAnimation(
            .75f, 1f, .75f, 1f,
            Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f
        )
        scaleAnimation.duration = BUTTON_ANIMATION_DURATION.toLong()
        scaleAnimation.fillAfter = true
        return scaleAnimation
    }

    companion object {
        private const val VIEW_TYPE_NUMBER = 0
        private const val VIEW_TYPE_DELETE = 1
    }

    init {
        mKeyValues = getAdjustKeyValues(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0))
    }
}
