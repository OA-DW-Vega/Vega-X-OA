package com.olam.warehouse.vegax.offloadingcoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingcoffee.data.api.VegaCoffeeOffloadingApi
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.usecase.VegaCoffeeOffloadingUseCase
import com.olam.warehouse.vegax.offloadingcoffee.data.repo.VegaCoffeeOffloadingRepository
import com.olam.warehouse.vegax.offloadingcoffee.data.repo.VegaCoffeeOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectVegaCoffeeOffloadingFeature() = loadFeature

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
    factory { VegaCoffeeOffloadingUseCase(get()) }
    viewModel { VegaCoffeeOffloadingViewModel(get(), get()) }
}

val repositoryModule = module {
    factory<VegaCoffeeOffloadingRepository> {
        VegaCoffeeOffloadingRepositoryImpl(
            get(),
            get(),
            get(),
            get()
        )
    }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeOffloadingApi::class.java) }
}
