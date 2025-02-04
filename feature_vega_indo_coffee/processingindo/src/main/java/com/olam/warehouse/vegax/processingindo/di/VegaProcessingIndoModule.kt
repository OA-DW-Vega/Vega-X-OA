package com.olam.warehouse.vegax.processingindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingindo.data.api.VegaProcessingIndoApi
import com.olam.warehouse.vegax.processingindo.data.domain.usecase.VegaProcessingIndoUseCase
import com.olam.warehouse.vegax.processingindo.data.repo.VegaProcessingIndoRepository
import com.olam.warehouse.vegax.processingindo.data.repo.VegaProcessingIndoRepositoryImpl
import com.olam.warehouse.vegax.processingindo.ui.fgrn.VegaProcessingIndoFgrnViewModel
import com.olam.warehouse.vegax.processingindo.ui.rmin.VegaProcessingIndoRminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectProcessingIndoFeature() = loadFeature

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
    factory { VegaProcessingIndoUseCase(get()) }
    viewModel { VegaProcessingIndoRminViewModel(get(), get()) }
    viewModel { VegaProcessingIndoFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaProcessingIndoApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaProcessingIndoRepository> { VegaProcessingIndoRepositoryImpl(get(), get(), get()) }
}
