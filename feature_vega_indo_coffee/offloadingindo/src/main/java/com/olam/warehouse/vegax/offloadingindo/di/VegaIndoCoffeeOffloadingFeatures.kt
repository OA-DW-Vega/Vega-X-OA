package com.olam.warehouse.vegax.offloadingindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingindo.data.api.VegaIndoCoffeeOffloadingApi
import com.olam.warehouse.vegax.offloadingindo.data.domain.usecase.VegaIndoCoffeeOffloadingUseCase
import com.olam.warehouse.vegax.offloadingindo.data.repo.VegaIndoCoffeeOffloadingRepository
import com.olam.warehouse.vegax.offloadingindo.data.repo.VegaIndoCoffeeOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingindo.ui.VegaIndoCoffeeOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */

fun injectVegaIndoCoffeeOffloadingFeature() = loadFeature

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
    factory { VegaIndoCoffeeOffloadingUseCase(get()) }
    viewModel { VegaIndoCoffeeOffloadingViewModel(get(), get()) }
}

val repositoryModule = module {
    factory<VegaIndoCoffeeOffloadingRepository> { VegaIndoCoffeeOffloadingRepositoryImpl(get(), get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndoCoffeeOffloadingApi::class.java) }
}
