package com.olam.warehouse.vegax.exportsalescameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.exportsalescameroon.data.api.VegaCameroonExportSalesApi
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.usecase.VegaCameroonExportSalesUsecase
import com.olam.warehouse.vegax.exportsalescameroon.data.repo.VegaCameroonExportSalesRepository
import com.olam.warehouse.vegax.exportsalescameroon.data.repo.VegaCameroonExportSalesRepositoryImpl
import com.olam.warehouse.vegax.exportsalescameroon.ui.VegaCameroonExportSalesViewModel
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
    factory { VegaCameroonExportSalesUsecase(get()) }
    viewModel { VegaCameroonExportSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonExportSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonExportSalesRepository> {
        VegaCameroonExportSalesRepositoryImpl(
            get(),
            get(),
            get()
        )
    }
}
