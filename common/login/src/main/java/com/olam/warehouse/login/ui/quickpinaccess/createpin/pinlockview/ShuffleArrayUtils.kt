package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

import java.util.*

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
object ShuffleArrayUtils {
    /**
     * Shuffle an array
     *
     * @param array
     */
    fun shuffle(array: IntArray): IntArray {
        val length = array.size
        val random = Random()
        random.nextInt()
        for (i in 0 until length) {
            val change = i + random.nextInt(length - i)
            swap(array, i, change)
        }
        return array
    }

    private fun swap(array: IntArray, index: Int, change: Int) {
        val temp = array[index]
        array[index] = array[change]
        array[change] = temp
    }
}
