package com.olam.warehouse.vegax.inventoryecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventoryecuador.data.api.VegaEcuadorInventoryApi
import com.olam.warehouse.vegax.inventoryecuador.data.domain.usecase.VegaEcuadorInventoryUseCase
import com.olam.warehouse.vegax.inventoryecuador.data.repo.VegaEcuadorInventoryRepository
import com.olam.warehouse.vegax.inventoryecuador.data.repo.VegaEcuadorInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventoryecuador.ui.VegaEcuadorInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */

fun injectVegaEcuadorInventoryFeature() = loadFeature

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
    factory { VegaEcuadorInventoryUseCase(get()) }
    viewModel { VegaEcuadorInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorInventoryRepository> { VegaEcuadorInventoryRepositoryImpl(get(), get()) }
}
