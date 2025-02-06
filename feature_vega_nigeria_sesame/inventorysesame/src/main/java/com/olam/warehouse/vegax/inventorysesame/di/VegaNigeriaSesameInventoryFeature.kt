package com.olam.warehouse.vegax.inventorysesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventorysesame.data.api.VegaNigeriaSesameInventoryApi
import com.olam.warehouse.vegax.inventorysesame.data.domain.usecase.VegaNigeriaSesamenventoryUseCase
import com.olam.warehouse.vegax.inventorysesame.data.repo.VegaNigeriaSesameInventoryRepository
import com.olam.warehouse.vegax.inventorysesame.data.repo.VegaNigeriaSesameInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventorysesame.ui.VegaNigeriaSesameInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */

fun injectVegaNigeriaSesameInventoryFeature() = loadFeature

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
    factory { VegaNigeriaSesamenventoryUseCase(get()) }
    viewModel { VegaNigeriaSesameInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaSesameInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaSesameInventoryRepository> { VegaNigeriaSesameInventoryRepositoryImpl(get(), get()) }
}
