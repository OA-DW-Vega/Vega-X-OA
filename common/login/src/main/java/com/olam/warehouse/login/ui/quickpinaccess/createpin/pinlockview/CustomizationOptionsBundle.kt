package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

import android.graphics.drawable.Drawable

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
class CustomizationOptionsBundle {
    private var textColor = 0
    private var textSize = 0
    private var buttonSize = 0
    private var buttonBackgroundDrawable: Drawable? = null
    private var deleteButtonDrawable: Drawable? = null
    private var deleteButtonWidthSize = 0
    private var deleteButtonHeightSize = 0
    private var showDeleteButton = false
    private var deleteButtonPressesColor = 0
    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
    }

    fun getTextSize(): Int {
        return textSize
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize
    }

    fun getButtonSize(): Int {
        return buttonSize
    }

    fun setButtonSize(buttonSize: Int) {
        this.buttonSize = buttonSize
    }

    fun getButtonBackgroundDrawable(): Drawable? {
        return buttonBackgroundDrawable
    }

    fun setButtonBackgroundDrawable(buttonBackgroundDrawable: Drawable?) {
        this.buttonBackgroundDrawable = buttonBackgroundDrawable
    }

    fun getDeleteButtonDrawable(): Drawable? {
        return deleteButtonDrawable
    }

    fun setDeleteButtonDrawable(deleteButtonDrawable: Drawable?) {
        this.deleteButtonDrawable = deleteButtonDrawable
    }

    fun getDeleteButtonWidthSize(): Int {
        return deleteButtonWidthSize
    }

    fun getDeleteButtonHeightSize(): Int {
        return deleteButtonHeightSize
    }

    fun setDeleteButtonWidthSize(deleteButtonWidthSize: Int) {
        this.deleteButtonWidthSize = deleteButtonWidthSize
    }

    fun setDeleteButtonHeightSize(deleteButtonHeightSize: Int) {
        this.deleteButtonHeightSize = deleteButtonHeightSize
    }

    fun isShowDeleteButton(): Boolean {
        return showDeleteButton
    }

    fun setShowDeleteButton(showDeleteButton: Boolean) {
        this.showDeleteButton = showDeleteButton
    }

    fun getDeleteButtonPressesColor(): Int {
        return deleteButtonPressesColor
    }

    fun setDeleteButtonPressesColor(deleteButtonPressesColor: Int) {
        this.deleteButtonPressesColor = deleteButtonPressesColor
    }
}
