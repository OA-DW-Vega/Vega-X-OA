package com.olam.warehouse.presentation.utils

/**
 * Created by Baskaran Kannan on 2/23/2022.
 */
object EventLocker {
    private var lock: Any? = null

    @Synchronized
    fun lock(`object`: Any?): Boolean {
        requireNotNull(`object`) { "object must not be null" }
        if (null == lock) {
            lock = `object`
            return true
        }
        return false
    }

    @Synchronized
    fun release(`object`: Any): Boolean {
        if (null != lock && lock == `object`) {
            lock = null
            return true
        }
        return false
    }
}