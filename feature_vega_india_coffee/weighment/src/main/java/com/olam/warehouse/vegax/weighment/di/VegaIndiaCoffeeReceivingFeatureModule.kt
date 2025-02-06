package com.olam.warehouse.vegax.weighment.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.weighment.data.api.VegaIndiaCoffeeMtntApi
import com.olam.warehouse.vegax.weighment.data.api.VegaIndiaCoffeeReceivingApi
import com.olam.warehouse.vegax.weighment.data.domain.usecase.VegaIndiaCoffeeMtntUseCase
import com.olam.warehouse.vegax.weighment.data.domain.usecase.VegaIndiaCoffeeReceivingUseCase
import com.olam.warehouse.vegax.weighment.data.repo.VegaMtntRepository
import com.olam.warehouse.vegax.weighment.data.repo.VegaMtntRepositoryImpl
import com.olam.warehouse.vegax.weighment.data.repo.VegaReceivingRepository
import com.olam.warehouse.vegax.weighment.data.repo.VegaReceivingRepositoryImpl
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeMtntViewModel
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeReceivingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectVegaReceivingFeature() = loadFeature

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
    factory { VegaIndiaCoffeeReceivingUseCase(get()) }
    factory { VegaIndiaCoffeeMtntUseCase(get()) }
    viewModel { VegaIndiaCoffeeReceivingViewModel(get(), get()) }
    viewModel { VegaIndiaCoffeeMtntViewModel(get(), get()) }
}

val repositoryModule = module {
    factory <VegaReceivingRepository> { VegaReceivingRepositoryImpl(get(), get(), get()) }
    factory <VegaMtntRepository> { VegaMtntRepositoryImpl(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeReceivingApi::class.java) }
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndiaCoffeeMtntApi::class.java) }

}
