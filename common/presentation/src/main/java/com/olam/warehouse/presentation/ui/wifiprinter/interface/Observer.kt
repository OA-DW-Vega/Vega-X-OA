package com.example.mywifiprinter.observers

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */
interface Observer {
    fun update()
    fun updateObserver(bool: Boolean)
    fun updateObserverProgress(percentage: Int)
}
