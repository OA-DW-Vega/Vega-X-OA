package com.olam.warehouse.vegax.inventorynicaragua.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventorynicaragua.data.api.VegaNicInventoryApi
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.usecase.VegaNicInventoryUseCase
import com.olam.warehouse.vegax.inventorynicaragua.data.repo.VegaNicInventoryRepository
import com.olam.warehouse.vegax.inventorynicaragua.data.repo.VegaNicInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventorynicaragua.ui.VegaNicInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */

fun injectNicInventoryFeature() = loadFeature

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
    factory { VegaNicInventoryUseCase(get()) }
    viewModel { VegaNicInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNicInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNicInventoryRepository> { VegaNicInventoryRepositoryImpl(get(), get()) }
}
