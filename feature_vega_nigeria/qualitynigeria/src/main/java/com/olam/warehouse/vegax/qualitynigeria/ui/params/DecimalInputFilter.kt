package com.olam.warehouse.vegax.qualitynigeria.ui.params

import android.text.InputFilter
import android.text.Spanned

    class DecimalInputFilter : InputFilter {

        override fun filter(p0: CharSequence?, p1: Int, p2: Int, p3: Spanned?, p4: Int, p5: Int): CharSequence? {

            var value = p3?.split(".")

            return if (value?.size!! > 1) {
                //condition made to 2 as p3 contain data already added and p0 data to be added
                if (value.get(1).trim().length ?: 0 < 3)
                    return null
                else return ""
            } else null
        }
    }

