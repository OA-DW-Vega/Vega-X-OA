package com.olam.warehouse.vegax.offloadingecuador.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingecuador.data.api.VegaEcuadorOffloadingApi
import com.olam.warehouse.vegax.offloadingecuador.data.domain.usecase.VegaEcuadorOffloadingUseCase
import com.olam.warehouse.vegax.offloadingecuador.data.repo.VegaEcuadorOffloadingRepository
import com.olam.warehouse.vegax.offloadingecuador.data.repo.VegaEcuadorOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingecuador.ui.VegaEcuadorOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectEcuadorOffloadingFeature() = loadFeature

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
    factory { VegaEcuadorOffloadingUseCase(get()) }
    viewModel { VegaEcuadorOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaEcuadorOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaEcuadorOffloadingRepository> { VegaEcuadorOffloadingRepositoryImpl(get(), get(), get()) }
}



