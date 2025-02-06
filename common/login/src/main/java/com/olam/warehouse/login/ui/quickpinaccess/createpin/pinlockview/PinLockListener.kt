package com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */


open interface PinLockListener {
    /**
     * Triggers when the complete pin is entered,
     * depends on the pin length set by the user
     *
     * @param pin the complete pin
     */
    fun onComplete(pin: String?)

    /**
     * Triggers when the pin is empty after manual deletion
     */
    fun onEmpty()

    /**
     * Triggers on a key press on the [PinLockView]
     *
     * @param pinLength       the current pin length
     * @param intermediatePin the intermediate pin
     */
    fun onPinChange(pinLength: Int, intermediatePin: String?)
}
