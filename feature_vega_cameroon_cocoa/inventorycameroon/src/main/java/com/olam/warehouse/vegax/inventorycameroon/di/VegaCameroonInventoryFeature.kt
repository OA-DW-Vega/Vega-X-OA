package com.olam.warehouse.vegax.inventorycameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.inventorycameroon.data.api.VegaCameroonInventoryApi
import com.olam.warehouse.vegax.inventorycameroon.data.domain.usecase.VegaCameroonInventoryUseCase
import com.olam.warehouse.vegax.inventorycameroon.data.repo.VegaCameroonInventoryRepository
import com.olam.warehouse.vegax.inventorycameroon.data.repo.VegaCameroonInventoryRepositoryImpl
import com.olam.warehouse.vegax.inventorycameroon.ui.VegaCameroonInventoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */

fun injectVegaCameroonInventoryFeature() = loadFeature

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
    factory { VegaCameroonInventoryUseCase(get()) }
    viewModel { VegaCameroonInventoryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonInventoryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonInventoryRepository> { VegaCameroonInventoryRepositoryImpl(get(), get()) }
}
