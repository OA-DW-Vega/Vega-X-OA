package com.olam.warehouse.vegax.pileindiacoffee.ui.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.pileindiacoffee.ui.VegaIndiaCoffeePileManagementViewModel
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.api.VegaIndiaCoffeePileManagementApi
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.VegaIndiaCoffeePileManagementUseCase
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.repo.VegaIndiaCoffeePileManagementRepository
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.repo.VegaIndiaCoffeePileManagementRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectIndiaCoffeeProcessingFeature() = loadFeature

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
    factory { VegaIndiaCoffeePileManagementUseCase(get()) }
    viewModel { VegaIndiaCoffeePileManagementViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeePileManagementApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndiaCoffeePileManagementRepository> { VegaIndiaCoffeePileManagementRepositoryImpl(get(), get()) }
}
