package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.R

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
class PinLockView : RecyclerView {
    private var mPin = ""
    private var mPinLength = 0
    private var mHorizontalSpacing = 0
    private var mVerticalSpacing = 0
    private var mTextColor = 0
    private var mDeleteButtonPressedColor = 0
    private var mTextSize = 0
    private var mButtonSize = 0
    private var mDeleteButtonWidthSize = 0
    private var mDeleteButtonHeightSize = 0
    private var mButtonBackgroundDrawable: Drawable? = null
    private var mDeleteButtonDrawable: Drawable? = null
    private var mShowDeleteButton = false
    private var mIndicatorDots: IndicatorDots? = null
    private var mAdapter: PinLockAdapter? = null
    private var mPinLockListener: PinLockListener? = null
    private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
    private lateinit var mCustomKeySet: IntArray
    private val mOnNumberClickListener: PinLockAdapter.OnNumberClickListener = object :
        PinLockAdapter.OnNumberClickListener {
        override fun onNumberClicked(keyValue: Int) {
            if (mPin.length < getPinLength()) {
                mPin = mPin + keyValue.toString()
                if (isIndicatorDotsAttached()) {
                    mIndicatorDots!!.updateDot(mPin.length)
                }
                if (mPin.length == 1) {
                    mAdapter!!.setPinLength(mPin.length)
                    mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
                }
                if (mPinLockListener != null) {
                    if (mPin.length == mPinLength) {
                        mPinLockListener!!.onComplete(mPin)
                    } else {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
                    }
                }
            } else {
                if (!isShowDeleteButton()) {
                    resetPinLockView()
                    mPin = mPin + keyValue.toString()
                    if (isIndicatorDotsAttached()) {
                        mIndicatorDots!!.updateDot(mPin.length)
                    }
                    if (mPinLockListener != null) {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
                    }
                } else {
                    if (mPinLockListener != null) {
                        mPinLockListener!!.onComplete(mPin)
                    }
                }
            }
        }
    }
    private val mOnDeleteClickListener: PinLockAdapter.OnDeleteClickListener = object :
        PinLockAdapter.OnDeleteClickListener {
        override fun onDeleteClicked() {
            if (mPin.length > 0) {
                mPin = mPin.substring(0, mPin.length - 1)
                if (isIndicatorDotsAttached()) {
                    mIndicatorDots!!.updateDot(mPin.length)
                }
                if (mPin.length == 0) {
                    mAdapter!!.setPinLength(mPin.length)
                    mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
                }
                if (mPinLockListener != null) {
                    if (mPin.length == 0) {
                        mPinLockListener!!.onEmpty()
                        clearInternalPin()
                    } else {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
                    }
                }
            } else {
                if (mPinLockListener != null) {
                    mPinLockListener!!.onEmpty()
                }
            }
        }

        override fun onDeleteLongClicked() {
            resetPinLockView()
            if (mPinLockListener != null) {
                mPinLockListener!!.onEmpty()
            }
        }
    }

    constructor(context: Context?) : super(context!!) {
        init(null, 0)
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init(attrs, 0)
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attributeSet: AttributeSet?, defStyle: Int) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PinLockView)
        try {
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mHorizontalSpacing = typedArray.getDimension(
                R.styleable.PinLockView_keypadHorizontalSpacing,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_horizontal_spacing)
            ).toInt()
            mVerticalSpacing = typedArray.getDimension(
                R.styleable.PinLockView_keypadVerticalSpacing,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_vertical_spacing)
            ).toInt()
            mTextColor = typedArray.getColor(
                R.styleable.PinLockView_keypadTextColor,
                ResourceUtils.getColor(context, R.color.text_fingerprint)!!
            )
            mTextSize = typedArray.getDimension(
                R.styleable.PinLockView_keypadTextSize,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_text_size)
            ).toInt()
            mButtonSize = typedArray.getDimension(
                R.styleable.PinLockView_keypadButtonSize,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_button_size)
            ).toInt()
            mDeleteButtonWidthSize = typedArray.getDimension(
                R.styleable.PinLockView_keypadDeleteButtonSize,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_delete_button_size_width)
            ).toInt()
            mDeleteButtonHeightSize = typedArray.getDimension(
                R.styleable.PinLockView_keypadDeleteButtonSize,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_delete_button_size_height)
            ).toInt()
            mButtonBackgroundDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadButtonBackgroundDrawable)
            mDeleteButtonDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadDeleteButtonDrawable)
            mShowDeleteButton = typedArray.getBoolean(R.styleable.PinLockView_keypadShowDeleteButton, true)
            mDeleteButtonPressedColor = typedArray.getColor(
                R.styleable.PinLockView_keypadDeleteButtonPressedColor,
                ResourceUtils.getColor(context, R.color.text_fingerprint)!!
            )
        } finally {
            typedArray.recycle()
        }
        mCustomizationOptionsBundle = CustomizationOptionsBundle()
        mCustomizationOptionsBundle!!.setTextColor(mTextColor)
        mCustomizationOptionsBundle!!.setTextSize(mTextSize)
        mCustomizationOptionsBundle!!.setButtonSize(mButtonSize)
        mCustomizationOptionsBundle!!.setButtonBackgroundDrawable(mButtonBackgroundDrawable)
        mCustomizationOptionsBundle!!.setDeleteButtonDrawable(mDeleteButtonDrawable)
        mCustomizationOptionsBundle!!.setDeleteButtonWidthSize(mDeleteButtonWidthSize)
        mCustomizationOptionsBundle!!.setDeleteButtonHeightSize(mDeleteButtonHeightSize)
        mCustomizationOptionsBundle!!.setShowDeleteButton(mShowDeleteButton)
        mCustomizationOptionsBundle!!.setDeleteButtonPressesColor(mDeleteButtonPressedColor)
        initView()
    }

    private fun initView() {
        layoutManager = GridLayoutManager(context, 3)
        mAdapter = PinLockAdapter()
        mAdapter!!.setOnItemClickListener(mOnNumberClickListener)
        mAdapter!!.setOnDeleteClickListener(mOnDeleteClickListener)
        mAdapter!!.setCustomizationOptions(mCustomizationOptionsBundle)
        adapter = mAdapter
        addItemDecoration(ItemSpaceDecoration(mHorizontalSpacing, mVerticalSpacing, 3, false))
        overScrollMode = OVER_SCROLL_NEVER
    }

    fun setTypeFace(typeFace: Typeface?) {
        mAdapter!!.setTypeFace(typeFace)
    }

    /**
     * Sets a [PinLockListener] to the to listen to pin update events
     *
     * @param pinLockListener the listener
     */
    fun setPinLockListener(pinLockListener: PinLockListener?) {
        mPinLockListener = pinLockListener
    }

    /**
     * Get the length of the current pin length
     *
     * @return the length of the pin
     */
    fun getPinLength(): Int {
        return mPinLength
    }

    /**
     * Sets the pin length dynamically
     *
     * @param pinLength the pin length
     */
    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
        if (isIndicatorDotsAttached()) {
            mIndicatorDots!!.setPinLength(pinLength)
        }
    }

    /**
     * Get the text color in the buttons
     *
     * @return the text color
     */
    fun getTextColor(): Int {
        return mTextColor
    }

    /**
     * Set the text color of the buttons dynamically
     *
     * @param textColor the text color
     */
    fun setTextColor(textColor: Int) {
        mTextColor = textColor
        mCustomizationOptionsBundle!!.setTextColor(textColor)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Get the size of the text in the buttons
     *
     * @return the size of the text in pixels
     */
    fun getTextSize(): Int {
        return mTextSize
    }

    /**
     * Set the size of text in pixels
     *
     * @param textSize the text size in pixels
     */
    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        mCustomizationOptionsBundle!!.setTextSize(textSize)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Get the size of the pin buttons
     *
     * @return the size of the button in pixels
     */
    fun getButtonSize(): Int {
        return mButtonSize
    }

    /**
     * Set the size of the pin buttons dynamically
     *
     * @param buttonSize the button size
     */
    fun setButtonSize(buttonSize: Int) {
        mButtonSize = buttonSize
        mCustomizationOptionsBundle!!.setButtonSize(buttonSize)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Get the current background drawable of the buttons, can be null
     *
     * @return the background drawable
     */
    fun getButtonBackgroundDrawable(): Drawable? {
        return mButtonBackgroundDrawable
    }

    /**
     * Set the background drawable of the buttons dynamically
     *
     * @param buttonBackgroundDrawable the background drawable
     */
    fun setButtonBackgroundDrawable(buttonBackgroundDrawable: Drawable?) {
        mButtonBackgroundDrawable = buttonBackgroundDrawable
        mCustomizationOptionsBundle!!.setButtonBackgroundDrawable(buttonBackgroundDrawable)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Get the drawable of the delete button
     *
     * @return the delete button drawable
     */
    fun getDeleteButtonDrawable(): Drawable? {
        return mDeleteButtonDrawable
    }

    /**
     * Set the drawable of the delete button dynamically
     *
     * @param deleteBackgroundDrawable the delete button drawable
     */
    fun setDeleteButtonDrawable(deleteBackgroundDrawable: Drawable?) {
        mDeleteButtonDrawable = deleteBackgroundDrawable
        mCustomizationOptionsBundle!!.setDeleteButtonDrawable(deleteBackgroundDrawable)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Get the delete button width size in pixels
     *
     * @return size in pixels
     */
    fun getDeleteButtonWidthSize(): Int {
        return mDeleteButtonWidthSize
    }

    /**
     * Get the delete button size height in pixels
     *
     * @return size in pixels
     */
    fun getDeleteButtonHeightSize(): Int {
        return mDeleteButtonHeightSize
    }

    /**
     * Set the size of the delete button width in pixels
     *
     * @param deleteButtonWidthSize size in pixels
     */
    fun setDeleteButtonWidthSize(deleteButtonWidthSize: Int) {
        mDeleteButtonWidthSize = deleteButtonWidthSize
        mCustomizationOptionsBundle!!.setDeleteButtonWidthSize(deleteButtonWidthSize)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Set the size of the delete button height in pixels
     *
     * @param deleteButtonHeightSize size in pixels
     */
    fun setDeleteButtonHeightSize(deleteButtonHeightSize: Int) {
        mDeleteButtonHeightSize = deleteButtonHeightSize
        mCustomizationOptionsBundle!!.setDeleteButtonWidthSize(deleteButtonHeightSize)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Is the delete button shown
     *
     * @return returns true if shown, false otherwise
     */
    fun isShowDeleteButton(): Boolean {
        return mShowDeleteButton
    }

    /**
     * Dynamically set if the delete button should be shown
     *
     * @param showDeleteButton true if the delete button should be shown, false otherwise
     */
    fun setShowDeleteButton(showDeleteButton: Boolean) {
        mShowDeleteButton = showDeleteButton
        mCustomizationOptionsBundle!!.setShowDeleteButton(showDeleteButton)
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Get the delete button pressed/focused state color
     *
     * @return color of the button
     */
    fun getDeleteButtonPressedColor(): Int {
        return mDeleteButtonPressedColor
    }

    /**
     * Set the pressed/focused state color of the delete button
     *
     * @param deleteButtonPressedColor the color of the delete button
     */
    fun setDeleteButtonPressedColor(deleteButtonPressedColor: Int) {
        mDeleteButtonPressedColor = deleteButtonPressedColor
        mCustomizationOptionsBundle!!.setDeleteButtonPressesColor(deleteButtonPressedColor)
        mAdapter!!.notifyDataSetChanged()
    }

    fun getCustomKeySet(): IntArray {
        return mCustomKeySet
    }

    fun setCustomKeySet(customKeySet: IntArray) {
        mCustomKeySet = customKeySet
        if (mAdapter != null) {
            mAdapter!!.setKeyValues(customKeySet)
        }
    }

    fun enableLayoutShuffling() {
        mCustomKeySet = ShuffleArrayUtils.shuffle(DEFAULT_KEY_SET)
        if (mAdapter != null) {
            mAdapter!!.setKeyValues(mCustomKeySet)
        }
    }

    private fun clearInternalPin() {
        mPin = ""
    }

    /**
     * Resets the [PinLockView], clearing the entered pin
     * and resetting the [IndicatorDots] if attached
     */
    fun resetPinLockView() {
        clearInternalPin()
        mAdapter!!.setPinLength(mPin.length)
        mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
        if (mIndicatorDots != null) {
            mIndicatorDots!!.updateDot(mPin.length)
        }
    }

    /**
     * Returns true if [IndicatorDots] are attached to [PinLockView]
     *
     * @return true if attached, false otherwise
     */
    fun isIndicatorDotsAttached(): Boolean {
        return mIndicatorDots != null
    }

    /**
     * Attaches [IndicatorDots] to [PinLockView]
     *
     * @param mIndicatorDots the view to attach
     */
    fun attachIndicatorDots(mIndicatorDots: IndicatorDots?) {
        this.mIndicatorDots = mIndicatorDots
    }

    companion object {
        private const val DEFAULT_PIN_LENGTH = 4
        private val DEFAULT_KEY_SET = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
    }
}
