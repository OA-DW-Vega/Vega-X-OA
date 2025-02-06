package com.olam.warehouse.presentation.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by SangiliPandian C on 20-11-2019.
 */
class GsonUtils : KoinComponent {
    private val gson: Gson by inject()
    fun toJson(param: Any): String {
        return gson.toJson(param)
    }
}

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
