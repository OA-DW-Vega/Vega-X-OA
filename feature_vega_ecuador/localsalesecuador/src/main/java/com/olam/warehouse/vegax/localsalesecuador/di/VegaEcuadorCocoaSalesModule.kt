package com.olam.warehouse.vegax.localsalesecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.localsalesecuador.data.api.VegaEcuadorCocoaSalesApi
import com.olam.warehouse.vegax.localsalesecuador.data.domain.usecase.VegaEcuadorCocoaSalesDispatchUseCase
import com.olam.warehouse.vegax.localsalesecuador.data.repo.VegaCoffeeSalesRepository
import com.olam.warehouse.vegax.localsalesecuador.data.repo.VegaCoffeeSalesRepositoryImpl
import com.olam.warehouse.vegax.localsalesecuador.ui.VegaEcuadorCocoaSalesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */

fun injectCoffeeSalesFeature() = loadFeature

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
    factory { VegaEcuadorCocoaSalesDispatchUseCase(get()) }
    viewModel { VegaEcuadorCocoaSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorCocoaSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeSalesRepository> { VegaCoffeeSalesRepositoryImpl(get(), get(), get()) }
}
