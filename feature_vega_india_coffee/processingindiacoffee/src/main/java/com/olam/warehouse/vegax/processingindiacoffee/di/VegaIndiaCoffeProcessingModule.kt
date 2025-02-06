package com.olam.warehouse.vegax.processingindiacoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingindiacoffee.data.api.VegaIndiaCoffeeProcessingApi
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.usecase.VegaIndiaCoffeeProcessingUseCase
import com.olam.warehouse.vegax.processingindiacoffee.data.repo.VegaIndiaCoffeeProcessingRepository
import com.olam.warehouse.vegax.processingindiacoffee.data.repo.VegaIndiaCoffeeProcessingRepositoryImpl
import com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn.VegaIndiaCoffeeFgrnViewModel
import com.olam.warehouse.vegax.processingindiacoffee.ui.rmin.VegaIndiaCoffeeRminViewModel
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
    factory { VegaIndiaCoffeeProcessingUseCase(get()) }
    viewModel { VegaIndiaCoffeeRminViewModel(get(), get()) }
    viewModel { VegaIndiaCoffeeFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndiaCoffeeProcessingRepository> {
        VegaIndiaCoffeeProcessingRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
