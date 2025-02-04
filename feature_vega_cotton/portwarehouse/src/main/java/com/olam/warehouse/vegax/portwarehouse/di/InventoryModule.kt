package com.olam.warehouse.vegax.portwarehouse.di

import com.olam.warehouse.portwarehouse.viewmodel.InventoryGradeViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.portwarehouse.data.api.InventoryApi
import com.olam.warehouse.vegax.portwarehouse.data.domain.VegaCottonPortWareHouseInventoryUseCase
import com.olam.warehouse.vegax.portwarehouse.data.repository.InventoryRepository
import com.olam.warehouse.vegax.portwarehouse.data.repository.VegaPortWareHouseInventoryRepositoryImpl

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectPortInventoryFeaturee() = loadFeature

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
    factory { VegaCottonPortWareHouseInventoryUseCase(get()) }
    viewModel { InventoryGradeViewModel(get(), get()) }
}

val networkgInventoryodule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(InventoryApi::class.java) }
}

val repositoryInventoryModule: Module = module {
    factory<InventoryRepository> { VegaPortWareHouseInventoryRepositoryImpl(get()) }
}
