package com.olam.warehouse.vegax.salescoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.salescoffee.data.api.VegaCoffeeSalesApi
import com.olam.warehouse.vegax.salescoffee.data.domain.usecase.VegaCoffeeSalesDispatchUseCase
import com.olam.warehouse.vegax.salescoffee.data.repo.VegaCoffeeSalesRepository
import com.olam.warehouse.vegax.salescoffee.data.repo.VegaCoffeeSalesRepositoryImpl
import com.olam.warehouse.vegax.salescoffee.ui.VegaCoffeeSalesViewModel
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
    factory { VegaCoffeeSalesDispatchUseCase(get()) }
    viewModel { VegaCoffeeSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeSalesRepository> { VegaCoffeeSalesRepositoryImpl(get(), get(), get()) }
}
