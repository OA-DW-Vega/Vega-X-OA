package com.olam.warehouse.vegax.weighmentcoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.weighmentcoffee.data.api.VegaCoffeeMtntApi
import com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase.VegaCoffeeMtntUseCase
import com.olam.warehouse.vegax.weighmentcoffee.data.repo.VegaCoffeeMtntRepository
import com.olam.warehouse.vegax.weighmentcoffee.data.repo.VegaCoffeeMtntRepositoryImpl
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtnrSupplierViewModel
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectVegaCoffeeWeighmentFeature() = loadFeature

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
    factory { VegaCoffeeMtntUseCase(get()) }
    viewModel { VegaCoffeeMtntViewModel(get(), get()) }
    viewModel { VegaCoffeeMtnrSupplierViewModel(get(), get()) }
}

val repositoryModule = module {
    factory { VegaCoffeeMtntRepositoryImpl(get(), get(),get(),get()) as VegaCoffeeMtntRepository }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeMtntApi::class.java) }
}
