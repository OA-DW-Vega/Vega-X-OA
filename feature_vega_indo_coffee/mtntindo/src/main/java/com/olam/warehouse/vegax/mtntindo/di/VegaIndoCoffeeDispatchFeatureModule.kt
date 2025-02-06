package com.olam.warehouse.vegax.mtntindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.mtntindo.data.api.VegaIndoCoffeeDispatchApi
import com.olam.warehouse.vegax.mtntindo.data.domain.usecase.VegaIndoCoffeeDispatchUseCase
import com.olam.warehouse.vegax.mtntindo.data.repo.VegaIndoCoffeeDispatchRepository
import com.olam.warehouse.vegax.mtntindo.data.repo.VegaIndoCoffeeDispatchRepositoryImpl
import com.olam.warehouse.vegax.mtntindo.ui.VegaIndoCoffeeDispatchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */

fun injectIndoCoffeeDispatchFeature() = loadFeature

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
    factory { VegaIndoCoffeeDispatchUseCase(get()) }
    viewModel { VegaIndoCoffeeDispatchViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndoCoffeeDispatchApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndoCoffeeDispatchRepository> { VegaIndoCoffeeDispatchRepositoryImpl(get(), get(), get()) }
}
