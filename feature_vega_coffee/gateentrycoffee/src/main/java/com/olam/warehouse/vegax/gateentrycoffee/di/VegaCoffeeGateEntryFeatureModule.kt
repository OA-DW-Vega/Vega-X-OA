package com.olam.warehouse.vegax.gateentrycoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.gateentrycoffee.data.api.VegaCoffeeGateEntryApi
import com.olam.warehouse.vegax.gateentrycoffee.data.domain.usecase.VegaCoffeeGateEntryUseCase
import com.olam.warehouse.vegax.gateentrycoffee.data.repo.VegaCoffeeGateEntryRepository
import com.olam.warehouse.vegax.gateentrycoffee.data.repo.VegaCoffeeGateEntryRepositoryImpl
import com.olam.warehouse.vegax.gateentrycoffee.ui.VegaCoffeeGateEntryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCoffeeGateEntryFeature() = loadFeature

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
    factory { VegaCoffeeGateEntryUseCase(get()) }
    viewModel { VegaCoffeeGateEntryViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeGateEntryApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeGateEntryRepository> { VegaCoffeeGateEntryRepositoryImpl(get(), get(), get()) }
}
