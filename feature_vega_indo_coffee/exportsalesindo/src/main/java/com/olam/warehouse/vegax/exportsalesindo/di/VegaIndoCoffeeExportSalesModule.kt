package com.olam.warehouse.vegax.exportsalesindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.exportsalesindo.data.api.VegaIndoCoffeeExportSalesApi
import com.olam.warehouse.vegax.exportsalesindo.data.domain.usecase.VegaIndoCoffeeExportSalesUsecase
import com.olam.warehouse.vegax.exportsalesindo.data.repo.VegaIndoCoffeeExportSalesRepository
import com.olam.warehouse.vegax.exportsalesindo.data.repo.VegaIndoCoffeeExportSalesRepositoryImpl
import com.olam.warehouse.vegax.exportsalesindo.ui.VegaIndoCoffeeExportSalesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */

fun injectIndoCoffeeExportSalesFeature() = loadFeature

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
    factory { VegaIndoCoffeeExportSalesUsecase(get()) }
    viewModel { VegaIndoCoffeeExportSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndoCoffeeExportSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndoCoffeeExportSalesRepository> { VegaIndoCoffeeExportSalesRepositoryImpl(get(), get()) }
}
