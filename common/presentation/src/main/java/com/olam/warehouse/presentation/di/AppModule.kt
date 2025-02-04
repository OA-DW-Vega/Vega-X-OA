package com.olam.warehouse.presentation.di

import android.content.Context
import com.olam.warehouse.navigation.QualityFeature
import com.olam.warehouse.navigation.ReceivingFeature
import com.olam.warehouse.navigation.createFeatureManager
import com.olam.warehouse.presentation.ui.installdialog.InstallDialogViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by SangiliPandian C on 25-11-2019.
 */

fun createFeatureModule(context: Context) = module {

    single { context }

    single { createFeatureManager(get()) }

    single<ReceivingFeature.Dependencies> {
        object : ReceivingFeature.Dependencies {
            override val context: Context = get()
        }
    }

    single<QualityFeature.Dependencies> {
        object : QualityFeature.Dependencies {
            override val context: Context = get()
        }
    }

    viewModel { InstallDialogViewModelImpl(get(), get()) }
}
