package com.olam.warehouse.vegax.inventoryindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventoryindo.data.api.VegaIndoCoffeeInventoryApi
import com.olam.warehouse.vegax.inventoryindo.data.domain.usecase.VegaIndoCoffeeInventoryUsecase
import com.olam.warehouse.vegax.inventoryindo.data.repo.VegaIndoCoffeeInventoryRepository
import com.olam.warehouse.vegax.inventoryindo.data.repo.VegaIndoCoffeeInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventoryindo.ui.VegaIndoCoffeeInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */

fun injectIndoCoffeeInventoryFeature() = loadFeature

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
    factory { VegaIndoCoffeeInventoryUsecase(get()) }
    viewModel { VegaIndoCoffeeInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndoCoffeeInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndoCoffeeInventoryRepository> { VegaIndoCoffeeInventoryRepositoryImpl(get(), get()) }
}
