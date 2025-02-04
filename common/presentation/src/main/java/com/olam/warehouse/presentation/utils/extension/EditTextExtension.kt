package com.olam.warehouse.presentation.utils.extension

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Created by SangiliPandian C on 16-11-2019.
 */
fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
