package com.olam.warehouse.vegax.exportsalescoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.exportsalescoffee.data.api.VegaCoffeeExportSalesApi
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.usecase.VegaCoffeeExportSalesUsecase
import com.olam.warehouse.vegax.exportsalescoffee.data.repo.VegaCoffeeExportSalesRepository
import com.olam.warehouse.vegax.exportsalescoffee.data.repo.VegaCoffeeExportSalesRepositoryImpl
import com.olam.warehouse.vegax.exportsalescoffee.ui.VegaCoffeeExportSalesViewModel
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
    factory { VegaCoffeeExportSalesUsecase(get()) }
    viewModel { VegaCoffeeExportSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeExportSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeExportSalesRepository> { VegaCoffeeExportSalesRepositoryImpl(get(), get()) }
}
