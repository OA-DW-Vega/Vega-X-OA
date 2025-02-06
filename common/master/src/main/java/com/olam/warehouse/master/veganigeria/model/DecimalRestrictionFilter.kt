package com.olam.warehouse.master.veganigeria.model

import android.text.InputFilter
import android.text.Spanned

class DecimalRestrictionFilter : InputFilter {

        override fun filter(p0: CharSequence?, p1: Int, p2: Int, p3: Spanned?, p4: Int, p5: Int): CharSequence? {

            //to retrict user to enter 13 digit including 2 decimal value
            val value = p3?.split(".")
            if (p3 != null && p0 != null) {
                val input = p3.toString().replace("-","")
                //to retrict user to enter 15 digit including 2 decimal value
  //              if(p3.contains(".") && p3.length < 16 || p3.length < 15 ) {
                    //to retrict user to enter 13 len ->  10 digit including 2 decimal value
                    if(input.contains(".") && input.length < 13 || (input.length < 10 || p0.contains(".")) ) {
                        return if (value?.size!! > 1) {
                            //allowed user to enter till 2 digits after decimal  as p3 contain data already added and p0 data to be added
                            // returning empty string for invalid input
                            if (value.get(1).trim().length < 2)
                                return null
                            else return ""
                        } else null

                    } else return ""

            }
            else return null
        }
    }
