package com.olam.warehouse.vegax.grncoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.grncoffee.data.api.VegaCoffeeGrnApi
import com.olam.warehouse.vegax.grncoffee.data.domain.usecase.VegaCoffeeGrnUseCase
import com.olam.warehouse.vegax.grncoffee.data.repo.VegaCoffeeGrnRepository
import com.olam.warehouse.vegax.grncoffee.data.repo.VegaCoffeeGrnRepositoryImpl
import com.olam.warehouse.vegax.grncoffee.ui.VegaCoffeeGrnViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCoffeeGrnFeature() = loadFeature
val loadFeature by lazy {
    loadKoinModules(
        listOf(
            viewModelModule,
            networkModule,
            repositoryModule
        )
    )
}
val viewModelModule: Module = module {
    factory { VegaCoffeeGrnUseCase(get()) }
    viewModel { VegaCoffeeGrnViewModel(get(), get()) }
}
val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeGrnApi::class.java) }
}
val repositoryModule: Module = module {
    factory<VegaCoffeeGrnRepository> { VegaCoffeeGrnRepositoryImpl(get(), get()) }
}
