package com.olam.warehouse.vegax.offloadingcocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingcocoa.data.api.VegaCoCoaOffloadingApi
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.usecase.VegaCoCoaOffloadingUseCase
import com.olam.warehouse.vegax.offloadingcocoa.data.repo.VegaCoCoaOffloadingRepository
import com.olam.warehouse.vegax.offloadingcocoa.data.repo.VegaCoCoaOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectVegaCoCoaOffloadingFeature() = loadFeature

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
    factory { VegaCoCoaOffloadingUseCase(get()) }
    viewModel { VegaCoCoaOffloadingViewModel(get(), get()) }
}

val repositoryModule = module {
    factory { VegaCoCoaOffloadingRepositoryImpl(get(), get(), get()) as VegaCoCoaOffloadingRepository }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoCoaOffloadingApi::class.java) }
}
