package com.olam.warehouse.vegax.processingcoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingcoffee.data.api.VegaCoffeeProcessingApi
import com.olam.warehouse.vegax.processingcoffee.data.domain.usecase.VegaCoffeeProcessingUseCase
import com.olam.warehouse.vegax.processingcoffee.data.repo.VegaCoffeeProcessingRepoImpl
import com.olam.warehouse.vegax.processingcoffee.data.repo.VegaCoffeeProcessingRepository
import com.olam.warehouse.vegax.processingcoffee.ui.fgrn.VegaCoffeeFgrnViewModel
import com.olam.warehouse.vegax.processingcoffee.ui.rmin.VegaCoffeeRminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCoffeeProcessingFeature() = loadFeature

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
    factory { VegaCoffeeProcessingUseCase(get()) }
    viewModel { VegaCoffeeRminViewModel(get(), get()) }
    viewModel { VegaCoffeeFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeProcessingRepository> { VegaCoffeeProcessingRepoImpl(get(), get(), get()) }
}
