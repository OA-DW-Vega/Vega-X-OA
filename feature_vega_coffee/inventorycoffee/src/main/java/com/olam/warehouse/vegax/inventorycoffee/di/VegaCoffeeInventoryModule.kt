package com.olam.warehouse.vegax.inventorycoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventorycoffee.data.api.VegaCoffeeInventoryApi
import com.olam.warehouse.vegax.inventorycoffee.data.domain.usecase.VegaCoffeeInventoryUseCase
import com.olam.warehouse.vegax.inventorycoffee.data.repo.VegaCoffeeInventoryRepository
import com.olam.warehouse.vegax.inventorycoffee.data.repo.VegaCoffeeInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventorycoffee.ui.VegaCoffeeInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCoffeeInventoryFeature() = loadFeature

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
    factory { VegaCoffeeInventoryUseCase(get()) }
    viewModel { VegaCoffeeInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeInventoryRepository> { VegaCoffeeInventoryRepositoryImpl(get(), get()) }
}
