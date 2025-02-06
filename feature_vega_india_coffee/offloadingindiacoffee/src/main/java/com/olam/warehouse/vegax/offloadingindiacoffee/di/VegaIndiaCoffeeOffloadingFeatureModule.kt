package com.olam.warehouse.vegax.offloadingindiacoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingindiacoffee.data.api.VegaIndiaCoffeeOffloadingApi
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.usecase.VegaIndiaCoffeeOffloadingUseCase
import com.olam.warehouse.vegax.offloadingindiacoffee.data.repo.VegaOffloadingRepository
import com.olam.warehouse.vegax.offloadingindiacoffee.data.repo.VegaOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingindiacoffee.ui.VegaOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectOffloadingFeature() = loadFeature

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
    factory { VegaIndiaCoffeeOffloadingUseCase(get()) }
    viewModel { VegaOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaOffloadingRepository> { VegaOffloadingRepositoryImpl(get(), get(), get()) }
}



