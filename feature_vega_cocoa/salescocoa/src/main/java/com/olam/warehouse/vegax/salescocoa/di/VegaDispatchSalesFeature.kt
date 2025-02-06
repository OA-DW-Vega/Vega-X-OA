package com.olam.warehouse.vegax.salescocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.salescocoa.data.api.VegaCocoaSalesApi
import com.olam.warehouse.vegax.salescocoa.data.domain.usecase.VegaCocoaSalesDispatchUseCase
import com.olam.warehouse.vegax.salescocoa.data.repo.VegaCocoaSalesRepository
import com.olam.warehouse.vegax.salescocoa.data.repo.VegaCocoaSalesRepositoryImpl
import com.olam.warehouse.vegax.salescocoa.ui.VegaCocoaSalesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 5/19/2020.
 */
fun injectCocoaSalesFeature() = loadFeature

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
    factory { VegaCocoaSalesDispatchUseCase(get()) }
    viewModel { VegaCocoaSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCocoaSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCocoaSalesRepository> { VegaCocoaSalesRepositoryImpl(get(), get(), get()) }
}
