package com.olam.warehouse.vegax.ppqindiacoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.ppqindiacoffee.data.api.VegaIndiaCoffeePpqApi
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.usecase.VegaIndiaCoffeePpqUsecase
import com.olam.warehouse.vegax.ppqindiacoffee.data.repo.VegaIndiaCoffeePpqRepository
import com.olam.warehouse.vegax.ppqindiacoffee.data.repo.VegaIndiaCoffeePpqRepositoryImpl
import com.olam.warehouse.vegax.ppqindiacoffee.ui.VegaIndiaCoffeePpqViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectIndiaCoffeePpqFeature() = loadFeature

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
    factory { VegaIndiaCoffeePpqUsecase(get()) }
    viewModel { VegaIndiaCoffeePpqViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeePpqApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndiaCoffeePpqRepository> { VegaIndiaCoffeePpqRepositoryImpl(get(), get(), get()) }
}
