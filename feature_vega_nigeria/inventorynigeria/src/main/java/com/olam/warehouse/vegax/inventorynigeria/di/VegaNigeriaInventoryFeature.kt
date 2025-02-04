package com.olam.warehouse.vegax.inventorynigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventorynigeria.data.api.VegaNigeriaInventoryApi
import com.olam.warehouse.vegax.inventorynigeria.data.domain.usecase.VegaNigeriaInventoryUsecase
import com.olam.warehouse.vegax.inventorynigeria.data.repo.VegaNigeriaInventoryRepository
import com.olam.warehouse.vegax.inventorynigeria.data.repo.VegaNigeriaInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventorynigeria.ui.VegaNigeriaInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Roshna Parambil on 9/3/2020.
 */

fun injectVegaNigeriaInventoryFeature() = loadFeature

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
    factory { VegaNigeriaInventoryUsecase(get()) }
    viewModel { VegaNigeriaInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaInventoryRepository> { VegaNigeriaInventoryRepositoryImpl(get(), get()) }
}
