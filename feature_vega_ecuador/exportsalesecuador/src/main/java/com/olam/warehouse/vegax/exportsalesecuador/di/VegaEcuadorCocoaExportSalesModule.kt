package com.olam.warehouse.vegax.exportsalesecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.exportsalesecuador.data.api.VegaEcuadorCocoaExportSalesApi
import com.olam.warehouse.vegax.exportsalesecuador.data.domain.usecase.VegaEcuadorCocoaExportSalesUsecase
import com.olam.warehouse.vegax.exportsalesecuador.data.repo.VegaCoffeeExportSalesRepository
import com.olam.warehouse.vegax.exportsalesecuador.data.repo.VegaCoffeeExportSalesRepositoryImpl
import com.olam.warehouse.vegax.exportsalesecuador.ui.VegaEcuadorCocoaExportSalesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 8/19/2020.
 */

fun injectCoffeeExportSalesFeature() = loadFeature

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
    factory { VegaEcuadorCocoaExportSalesUsecase(get()) }
    viewModel { VegaEcuadorCocoaExportSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorCocoaExportSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeExportSalesRepository> { VegaCoffeeExportSalesRepositoryImpl(get(), get()) }
}
