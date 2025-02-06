package com.olam.warehouse.vegax.ginningwarehouse.ui.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.InventoryApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonGinningInventoryUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.InventoryRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.VegaGinningInventoryRepositoryImpl
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.inventory.InventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectGinningInventoryFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelInventoryModule,
            networkgInventoryodule,
            repositoryInventoryModule
        )
    )
}

val viewModelInventoryModule: Module = module {
    factory { VegaCottonGinningInventoryUseCase(get()) }
    viewModel { InventoryViewModel(get(), get()) }
}

val networkgInventoryodule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(InventoryApi::class.java) }
}

val repositoryInventoryModule: Module = module {
    factory<InventoryRepository> { VegaGinningInventoryRepositoryImpl(get()) }
}
