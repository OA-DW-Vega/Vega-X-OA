package com.olam.warehouse.vegax.processingecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingecuador.data.api.VegaEcuadorProcessingApi
import com.olam.warehouse.vegax.processingecuador.data.domain.usecase.VegaEcuadorProcessingUseCase
import com.olam.warehouse.vegax.processingecuador.data.repo.VegaEcuadorProcessingRepository
import com.olam.warehouse.vegax.processingecuador.data.repo.VegaIndiaCoffeeProcessingRepositoryImpl
import com.olam.warehouse.vegax.processingecuador.ui.fgrn.VegaEcuadorProcessingFgrnViewModel
import com.olam.warehouse.vegax.processingecuador.ui.rmin.VegaEcuadorCocoaRminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectCocoaProcessingFeature() = loadFeature

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
    factory { VegaEcuadorProcessingUseCase(get()) }
    viewModel { VegaEcuadorCocoaRminViewModel(get(), get()) }
    viewModel { VegaEcuadorProcessingFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorProcessingRepository> {
        VegaIndiaCoffeeProcessingRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
