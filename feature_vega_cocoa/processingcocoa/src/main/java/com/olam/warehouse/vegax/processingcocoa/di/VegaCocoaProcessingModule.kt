package com.olam.warehouse.vegax.processingcocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingcocoa.data.api.VegaCocoaProcessingApi
import com.olam.warehouse.vegax.processingcocoa.data.domain.usecase.VegaCocoaProcessingUseCase
import com.olam.warehouse.vegax.processingcocoa.data.repo.VegaCocoaProcessingRepository
import com.olam.warehouse.vegax.processingcocoa.data.repo.VegaCocoaProcessingRepositoryImpl
import com.olam.warehouse.vegax.processingcocoa.ui.fgrn.VegaCocoaFgrnViewModel
import com.olam.warehouse.vegax.processingcocoa.ui.rmin.VegaCocoaRminViewModel
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
    factory { VegaCocoaProcessingUseCase(get()) }
    viewModel { VegaCocoaRminViewModel(get(), get()) }
    viewModel { VegaCocoaFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCocoaProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCocoaProcessingRepository> { VegaCocoaProcessingRepositoryImpl(get(), get(), get()) }
}
