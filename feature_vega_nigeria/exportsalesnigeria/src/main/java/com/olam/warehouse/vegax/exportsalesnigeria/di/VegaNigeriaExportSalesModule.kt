package com.olam.warehouse.vegax.exportsalesnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.exportsalesnigeria.data.api.VegaNigeriaExportSalesApi
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.usecase.VegaNigeriaExportSalesUsecase
import com.olam.warehouse.vegax.exportsalesnigeria.data.repo.VegaNigeriaExportSalesRepository
import com.olam.warehouse.vegax.exportsalesnigeria.data.repo.VegaNigeriaExportSalesRepositoryImpl
import com.olam.warehouse.vegax.exportsalesnigeria.ui.VegaNigeriaExportSalesViewModel
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
    factory { VegaNigeriaExportSalesUsecase(get()) }
    viewModel { VegaNigeriaExportSalesViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaExportSalesApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaExportSalesRepository> { VegaNigeriaExportSalesRepositoryImpl(get(), get()) }
}
