package com.olam.warehouse.vegax.pilesesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.pilesesame.data.api.VegaSesamePileManagementApi
import com.olam.warehouse.vegax.pilesesame.data.domain.VegSesamePileManagementUseCase
import com.olam.warehouse.vegax.pilesesame.data.repo.VegaSesamePileManagementRepository
import com.olam.warehouse.vegax.pilesesame.data.repo.VegaSesamePileManagementRepositoryImpl
import com.olam.warehouse.vegax.pilesesame.ui.VegaSesamePileManagementViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectSesameProcessingFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelModule,
            networkModule,
            repositoryModule
        )
    )
}

val viewModelModule: Module = module {
    factory { VegSesamePileManagementUseCase(get()) }
    viewModel { VegaSesamePileManagementViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaSesamePileManagementApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaSesamePileManagementRepository> {
        VegaSesamePileManagementRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
