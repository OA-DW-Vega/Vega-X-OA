package com.olam.warehouse.vegax.reconcilnicaragua.ui

interface Callback {
    fun replaceFragment(type: String, data: Any)
    fun replaceFragment(type: String, data: Any, items: Any)
}
