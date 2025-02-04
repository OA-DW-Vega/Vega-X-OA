package com.olam.warehouse.vegax.grnindiacoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grnindiacoffee.data.api.VegaIndiaCoffeeGrnApi
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.VegaIndiaCoffeeGrnUseCase
import com.olam.warehouse.vegax.grnindiacoffee.data.repo.VegaIndiaCoffeeGrnRepository
import com.olam.warehouse.vegax.grnindiacoffee.data.repo.VegaIndiaCoffeeGrnRepositoryImpl
import com.olam.warehouse.vegax.grnindiacoffee.ui.VegaIndiaCoffeeGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectNigeriaSesameGrnFeature() = loadFeature

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
    factory { VegaIndiaCoffeeGrnUseCase(get()) }
    viewModel { VegaIndiaCoffeeGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndiaCoffeeGrnRepository> { VegaIndiaCoffeeGrnRepositoryImpl(get(), get()) }
}



