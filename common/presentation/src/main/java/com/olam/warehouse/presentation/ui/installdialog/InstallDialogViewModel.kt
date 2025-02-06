package com.olam.warehouse.presentation.ui.installdialog

import android.content.Context
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.olam.warehouse.navigation.*
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf

/**
 * Created by SangiliPandian C on 26-11-2019.
 */
interface InstallDialogViewModel {
    val installState: LiveData<FeatureManager.InstallState>
    fun installFeature(feature: String)
    fun installFeature(@IdRes actionId: Int)
}

class InstallDialogViewModelImpl(
    private val featureManager: FeatureManager,
    private val context: Context
) : ViewModel(), InstallDialogViewModel {
    override val installState = mutableLiveDataOf<FeatureManager.InstallState>()

    override fun installFeature(actionId: Int) {
        val listener: (FeatureManager.InstallState) -> Unit = { state ->
            installState.value = state
        }
        when (actionId) {
            ReceivingFeature::class.info(context).actionId -> featureManager.installFeature<ReceivingFeature>(listener)
            QualityFeature::class.info(context).actionId -> featureManager.installFeature<QualityFeature>(listener)
        }
    }

    override fun installFeature(feature: String) {
        val listener: (FeatureManager.InstallState) -> Unit = { state ->
            installState.value = state
        }
        when (feature) {
            ReceivingFeature::class.info(context).id -> featureManager.installFeature<ReceivingFeature>(listener)
            QualityFeature::class.info(context).id -> featureManager.installFeature<QualityFeature>(listener)
        }
    }
}
