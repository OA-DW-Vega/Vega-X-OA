package com.olam.warehouse.presentation.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Created by SangiliPandian C on 15-11-2019.
 */
object PreferenceHelper {

    private const val NAME = "VegaX"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    fun init(context: Context) {
        /*preferences = context.getSharedPreferences(
            NAME,
            MODE
        )*/
        preferences = EncryptedSharedPreferences.create(
            NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun save(label: String, elem: Any) {
        when (elem) {
            is Int -> preferences.edit()?.putInt(label, elem)?.apply()
            is String -> preferences.edit()?.putString(label, elem)?.apply()
            is Float -> preferences.edit()?.putFloat(label, elem)?.apply()
            is Boolean -> preferences.edit()?.putBoolean(label, elem)?.apply()
            is Long -> preferences.edit()?.putLong(label, elem)?.apply()
            else -> throw UnsupportedOperationException("Not Yet Implemented")
        }
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    fun <T> get(label: String, default: T): T = when (default) {
        is Int -> preferences.getInt(label, default)
        is String -> preferences.getString(label, default)
        is Float -> preferences.getFloat(label, default)
        is Boolean -> preferences.getBoolean(label, default)
        is Long -> preferences.getLong(label, default)
        else -> throw UnsupportedOperationException("Not Yet Implemented")
    } as T

    fun clear() {
        preferences.edit().clear().commit()
    }


}
