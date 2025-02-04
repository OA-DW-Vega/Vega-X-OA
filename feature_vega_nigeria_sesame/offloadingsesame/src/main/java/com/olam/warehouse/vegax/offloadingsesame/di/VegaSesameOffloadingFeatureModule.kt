package com.olam.warehouse.vegax.offloadingsesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingsesame.data.api.VegaSesameOffloadingApi
import com.olam.warehouse.vegax.offloadingsesame.data.domain.usecase.VegaSesameOffloadingUseCase
import com.olam.warehouse.vegax.offloadingsesame.data.repo.VegaSesameOffloadingRepository
import com.olam.warehouse.vegax.offloadingsesame.data.repo.VegaSesameOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingsesame.ui.VegaSesameOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectSesameOffloadingFeature() = loadFeature

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
    factory { VegaSesameOffloadingUseCase(get()) }
    viewModel { VegaSesameOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaSesameOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaSesameOffloadingRepository> { VegaSesameOffloadingRepositoryImpl(get(), get(), get(), get()) }
}



