package com.olam.warehouse.vegax.grnindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grnindo.data.api.VegaIndoCoffeeGrnApi
import com.olam.warehouse.vegax.grnindo.data.domain.usecase.VegaIndoCoffeeGrnUseCase
import com.olam.warehouse.vegax.grnindo.data.repo.VegaIndoCoffeeGrnRepository
import com.olam.warehouse.vegax.grnindo.data.repo.VegaIndoCoffeeGrnRepositoryImpl
import com.olam.warehouse.vegax.grnindo.ui.VegaIndoCoffeeGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */

fun injectIndoCoffeeGrnFeature() = loadFeature

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
    factory { VegaIndoCoffeeGrnUseCase(get()) }
    viewModel { VegaIndoCoffeeGrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndoCoffeeGrnApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndoCoffeeGrnRepository> { VegaIndoCoffeeGrnRepositoryImpl(get(), get()) }
}
