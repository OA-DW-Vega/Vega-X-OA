package com.olam.warehouse.presentation.ui.wifiprinter.services

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */
interface PrintCompleteService {
    fun onMessage(status: Int)
    fun respondAfterWifiSwitch()
}
