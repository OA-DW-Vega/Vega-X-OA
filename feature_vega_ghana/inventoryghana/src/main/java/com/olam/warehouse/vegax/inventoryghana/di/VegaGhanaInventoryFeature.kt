package com.olam.warehouse.vegax.inventoryghana.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventoryghana.data.api.VegaGhanaInventoryApi
import com.olam.warehouse.vegax.inventoryghana.data.domain.usecase.VegaGhanaInventoryUseCase
import com.olam.warehouse.vegax.inventoryghana.data.repo.VegaGhanaInventoryRepository
import com.olam.warehouse.vegax.inventoryghana.data.repo.VegaGhanaInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventoryghana.ui.VegaGhanaInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */

fun injectVegaGhanaInventoryFeature() = loadFeature

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
    factory { VegaGhanaInventoryUseCase(get()) }
    viewModel { VegaGhanaInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaInventoryRepository> { VegaGhanaInventoryRepositoryImpl(get(), get()) }
}
