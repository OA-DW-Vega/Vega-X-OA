package com.olam.warehouse.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.IdRes
import kotlin.reflect.KClass

/**
 * Created by SangiliPandian C on 25-11-2019.
 */
fun <T : Feature<*>> KClass<T>.info(context: Context) = when (this) {
    ReceivingFeature::class -> Feature.Info(
        id = "home",
        name = context.getString(R.string.title_feature_receiving),
        actionId = R.id.actionReceiving
    )
    QualityFeature::class -> Feature.Info(
        id = "video",
        name = context.getString(R.string.title_feature_quality),
        actionId = R.id.actionQuality
    )
    else -> throw IllegalArgumentException("Unexpected feature $this")
}

interface Feature<T> {
    fun getMainScreen(): Activity
    fun getLaunchIntent(context: Context): Intent
    fun inject(dependencies: T)

    data class Info(
        val id: String,
        val name: String,
        @IdRes val actionId: Int
    )
}

interface ReceivingFeature : Feature<ReceivingFeature.Dependencies> {
    interface Dependencies {
        val context: Context
    }
}

interface QualityFeature : Feature<QualityFeature.Dependencies> {
    interface Dependencies {
        val context: Context
    }
}
