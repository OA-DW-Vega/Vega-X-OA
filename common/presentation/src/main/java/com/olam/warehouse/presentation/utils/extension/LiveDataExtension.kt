package com.olam.warehouse.presentation.utils.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Created by SangiliPandian C on 16-11-2019.
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun <T> mutableLiveDataOf(initialData: T? = null) = MutableLiveData<T>().apply { value = initialData }

inline fun <T> LiveData<T>.observeExt(owner: LifecycleOwner, crossinline onChange: (T) -> Unit) {
    this.observe(owner, Observer<T> { data -> onChange(data) })
}
