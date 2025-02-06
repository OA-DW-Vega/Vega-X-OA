package com.olam.warehouse.vegax.mtntcoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.mtntcoffee.data.api.VegaCoffeeMtntApi
import com.olam.warehouse.vegax.mtntcoffee.data.domain.usecase.VegaCoffeeDispatchUseCase
import com.olam.warehouse.vegax.mtntcoffee.data.repo.VegaCoffeeMtntRepository
import com.olam.warehouse.vegax.mtntcoffee.data.repo.VegaCoffeeMtntRepositoryImpl
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectCoffeeDispatchFeature() = loadFeature

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
    factory { VegaCoffeeDispatchUseCase(get()) }
    viewModel { VegaCoffeeMtntViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeMtntApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeMtntRepository> { VegaCoffeeMtntRepositoryImpl(get(), get(), get()) }
}
