package com.example.mywifiprinter.observers

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */
interface Observable {
    fun notifyObserver(bool: Boolean)
    fun attach(observer: Observer?)
    fun detach(observer: Observer?)
    fun notify(param: Any?)
    fun updateProgress(percentage: Int)
}