package com.olam.warehouse.vegax.processingsesame.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingsesame.data.api.VegaSesameProcessingApi
import com.olam.warehouse.vegax.processingsesame.data.domain.usecase.VegaSesameProcessingUseCase
import com.olam.warehouse.vegax.processingsesame.data.repo.VegaSesameProcessingRepoImpl
import com.olam.warehouse.vegax.processingsesame.data.repo.VegaSesameProcessingRepository
import com.olam.warehouse.vegax.processingsesame.ui.fgrn.VegaSesameFgrnViewModel
import com.olam.warehouse.vegax.processingsesame.ui.rmin.VegaSesameRminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectSesameProcessingFeature() = loadFeature

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
    factory { VegaSesameProcessingUseCase(get()) }
    viewModel { VegaSesameRminViewModel(get(), get()) }
    viewModel { VegaSesameFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaSesameProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaSesameProcessingRepository> { VegaSesameProcessingRepoImpl(get(), get(), get(), get()) }
}
