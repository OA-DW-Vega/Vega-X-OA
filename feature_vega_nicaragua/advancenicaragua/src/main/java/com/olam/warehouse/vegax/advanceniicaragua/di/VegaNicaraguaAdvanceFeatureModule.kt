package com.olam.warehouse.vegax.advanceniicaragua.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.advanceniicaragua.data.api.VegaNicaraguaAdvanceApi
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.usecase.VegaNicaraguaAdvanceUseCase
import com.olam.warehouse.vegax.advanceniicaragua.data.repo.VegaNicaraguaAdvanceRepository
import com.olam.warehouse.vegax.advanceniicaragua.data.repo.VegaNicaraguaAdvanceRepositoryImpl
import com.olam.warehouse.vegax.advanceniicaragua.ui.VegaNicaraguaAdvanceViewModel
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
    factory { VegaNicaraguaAdvanceUseCase(get()) }
    viewModel { VegaNicaraguaAdvanceViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNicaraguaAdvanceApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNicaraguaAdvanceRepository> { VegaNicaraguaAdvanceRepositoryImpl(get(), get()) }
}



