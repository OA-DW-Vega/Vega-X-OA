package com.olam.warehouse.vegax.invoicenicaragua.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.invoicenicaragua.data.api.VegaNicaraguaInvoiceApi
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.usecase.VegaNicaraguaInvoiceUseCase
import com.olam.warehouse.vegax.invoicenicaragua.data.repo.VegaNicaraguaInvoiceRepository
import com.olam.warehouse.vegax.invoicenicaragua.data.repo.VegaNicaraguaInvoiceRepositoryImpl
import com.olam.warehouse.vegax.invoicenicaragua.ui.VegaNicaraguaInvoiceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
fun injectNicaraguaInvoiceFeature() = loadFeature

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
    factory { VegaNicaraguaInvoiceUseCase(get()) }
    viewModel { VegaNicaraguaInvoiceViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNicaraguaInvoiceApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNicaraguaInvoiceRepository> { VegaNicaraguaInvoiceRepositoryImpl(get(), get()) }
}



