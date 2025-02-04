package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.olam.warehouse.login.R
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */

class IndicatorDots @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr) {
    //    @IntDef([IndicatorType.FIXED, IndicatorType.FILL, IndicatorType.FILL_WITH_ANIMATION])
    @Retention(RetentionPolicy.SOURCE)
    annotation class IndicatorType {
        companion object {
            var FIXED = 0
            var FILL = 1
            var FILL_WITH_ANIMATION = 2
        }
    }

    private var mDotDiameter = 0
    private var mDotSpacing = 0
    private var mFillDrawable = 0
    private var mEmptyDrawable = 0
    private var mPinLength = 0
    private var mIndicatorType = 0
    private var mPreviousLength = 0
    private fun initView(context: Context) {
        if (mIndicatorType == 0) {
            for (i in 0 until mPinLength) {
                val dot = View(context)
                emptyDot(dot)
                val params = LayoutParams(
                    mDotDiameter,
                    mDotDiameter
                )
                params.setMargins(mDotSpacing, 0, mDotSpacing, 0)
                dot.layoutParams = params
                addView(dot)
            }
        } else if (mIndicatorType == 2) {
            val layoutTransition = LayoutTransition()
            layoutTransition.setDuration(DEFAULT_ANIMATION_DURATION.toLong())
            layoutTransition.setStartDelay(LayoutTransition.APPEARING, 0)
            setLayoutTransition(layoutTransition)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // If the indicator type is not fixed
        if (mIndicatorType != 0) {
            val params = this.layoutParams
            params.height = mDotDiameter
            requestLayout()
        }
    }

    fun updateDot(length: Int) {
        if (mIndicatorType == 0) {
            mPreviousLength = if (length > 0) {
                if (length > mPreviousLength) {
                    fillDot(getChildAt(length - 1))
                } else {
                    emptyDot(getChildAt(length))
                }
                length
            } else {
                // When {@code mPinLength} is 0, we need to reset all the views back to empty
                for (i in 0 until childCount) {
                    val v = getChildAt(i)
                    emptyDot(v)
                }
                0
            }
        } else {
            if (length > 0) {
                if (length > mPreviousLength) {
                    val dot = View(context)
                    fillDot(dot)
                    val params = LayoutParams(
                        mDotDiameter,
                        mDotDiameter
                    )
                    params.setMargins(mDotSpacing, 0, mDotSpacing, 0)
                    dot.layoutParams = params
                    addView(dot, length - 1)
                } else {
                    removeViewAt(length)
                }
                mPreviousLength = length
            } else {
                removeAllViews()
                mPreviousLength = 0
            }
        }
    }

    private fun emptyDot(dot: View) {
        dot.setBackgroundResource(mEmptyDrawable)
    }

    private fun fillDot(dot: View) {
        dot.setBackgroundResource(mFillDrawable)
    }

    fun getPinLength(): Int {
        return mPinLength
    }

    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
        removeAllViews()
        initView(context)
    }

    @IndicatorType
    fun getIndicatorType(): Int {
        return mIndicatorType
    }

    fun setIndicatorType(@IndicatorType type: Int) {
        mIndicatorType = type
        removeAllViews()
        initView(context)
    }

    companion object {
        private const val DEFAULT_PIN_LENGTH = 4
        private const val DEFAULT_ANIMATION_DURATION = 200
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PinLockView)
        try {
            mDotDiameter = typedArray.getDimension(
                R.styleable.PinLockView_dotDiameter,
                ResourceUtils.getDimensionInPx(getContext(), R.dimen.dot_diameter)
            ).toInt()
            mDotSpacing = typedArray.getDimension(
                R.styleable.PinLockView_dotSpacing,
                ResourceUtils.getDimensionInPx(getContext(), R.dimen.dot_spacing)
            ).toInt()
            mFillDrawable = typedArray.getResourceId(
                R.styleable.PinLockView_dotFilledBackground,
                R.drawable.dot_filled
            )
            mEmptyDrawable = typedArray.getResourceId(
                R.styleable.PinLockView_dotEmptyBackground,
                R.drawable.dot_empty
            )
            mPinLength =
                typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mIndicatorType = typedArray.getInt(
                R.styleable.PinLockView_indicatorType,
                IndicatorType.FIXED
            )
        } finally {
            typedArray.recycle()
        }
        initView(context)
    }
}
