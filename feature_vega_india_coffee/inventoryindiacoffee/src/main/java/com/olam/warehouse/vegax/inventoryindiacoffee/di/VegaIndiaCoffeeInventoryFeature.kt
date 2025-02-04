package com.olam.warehouse.vegax.inventoryindiacoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventoryindiacoffee.data.api.VegaIndiaCoffeeInventoryApi
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.usecase.VegaIndiaCoffeeInventoryUseCase
import com.olam.warehouse.vegax.inventoryindiacoffee.data.repo.VegaIndiaCoffeeInventoryRepository
import com.olam.warehouse.vegax.inventoryindiacoffee.data.repo.VegaIndiaCoffeeInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventoryindiacoffee.ui.VegaIndiaCoffeeInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectVegaIndiaCoffeeInventoryFeature() = loadFeature

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
    factory { VegaIndiaCoffeeInventoryUseCase(get()) }
    viewModel { VegaIndiaCoffeeInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndiaCoffeeInventoryRepository> { VegaIndiaCoffeeInventoryRepositoryImpl(get(), get()) }
}
