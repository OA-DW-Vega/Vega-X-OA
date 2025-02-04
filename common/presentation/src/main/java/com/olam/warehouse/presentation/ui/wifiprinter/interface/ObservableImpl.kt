package com.example.mywifiprinter.observers

import java.util.*

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */
class ObservableImpl : Observable {
    private val mObservers: MutableList<Observer>
    override fun attach(observer: Observer?) {
        if (observer != null) {
            mObservers.add(observer)
        }
    }

    override fun notifyObserver(bool: Boolean) {
        if (bool) {
            for (i in mObservers.indices.reversed()) {
                mObservers[i].update()
            }
        }
    }

    override fun updateProgress(percentage: Int) {
        for (i in mObservers.indices.reversed()) {
            mObservers[i].updateObserverProgress(percentage)
        }
    }

    override fun notify(param: Any?) {
        try {
            for (i in mObservers.indices.reversed()) {
                mObservers[i].update()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun detach(observer: Observer?) {
        try {
            mObservers.remove(observer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        mObservers = ArrayList()
    }
}