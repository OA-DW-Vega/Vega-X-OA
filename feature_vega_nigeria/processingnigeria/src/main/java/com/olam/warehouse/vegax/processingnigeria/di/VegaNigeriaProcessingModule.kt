package com.olam.warehouse.vegax.processingnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingnigeria.data.api.VegaNigeriaProcessingApi
import com.olam.warehouse.vegax.processingnigeria.data.domain.usecase.VegaNigeriaProcessingUseCase
import com.olam.warehouse.vegax.processingnigeria.data.repo.VegaNigeriaProcessingRepoImpl
import com.olam.warehouse.vegax.processingnigeria.data.repo.VegaNigeriaProcessingRepository
import com.olam.warehouse.vegax.processingnigeria.ui.fgrn.VegaNigeriaFgrnViewModel
import com.olam.warehouse.vegax.processingnigeria.ui.rmin.VegaNigeriaRminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectNigeriaProcessingFeature() = loadFeature

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
    factory { VegaNigeriaProcessingUseCase(get()) }
    viewModel { VegaNigeriaRminViewModel(get(), get()) }
    viewModel { VegaNigeriaFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaProcessingRepository> { VegaNigeriaProcessingRepoImpl(get(), get(), get()) }
}
