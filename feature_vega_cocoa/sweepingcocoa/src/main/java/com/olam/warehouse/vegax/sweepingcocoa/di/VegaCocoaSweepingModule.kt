package com.olam.warehouse.vegax.sweepingcocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.sweepingcocoa.data.api.VegaCocoaSweepingApi
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.usecase.VegaCocoaSweepingUseCase
import com.olam.warehouse.vegax.sweepingcocoa.data.repo.VegaCocoaSweepingRepository
import com.olam.warehouse.vegax.sweepingcocoa.data.repo.VegaCocoaSweepingRepositoryImpl
import com.olam.warehouse.vegax.sweepingcocoa.ui.VegaCocoaSweepingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCocoaSweepingFeature() = loadFeature

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
    factory { VegaCocoaSweepingUseCase(get()) }
    viewModel { VegaCocoaSweepingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCocoaSweepingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCocoaSweepingRepository> { VegaCocoaSweepingRepositoryImpl(get(), get(), get()) }
}
