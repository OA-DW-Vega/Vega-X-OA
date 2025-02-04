package com.olam.warehouse.vegax.offloading.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloading.data.api.VegaOffloadingApi
import com.olam.warehouse.vegax.offloading.data.domain.usecase.VegaOffloadingUseCase
import com.olam.warehouse.vegax.offloading.data.repo.VegaOffloadingRepository
import com.olam.warehouse.vegax.offloading.data.repo.VegaOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloading.ui.VegaOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectOffloadingFeature() = loadFeature

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
    factory { VegaOffloadingUseCase(get()) }
    viewModel { VegaOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaOffloadingRepository> { VegaOffloadingRepositoryImpl(get(), get(), get()) }
}



