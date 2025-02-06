package com.olam.warehouse.vegax.offloadingnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadingnigeria.data.api.VegaNigeriaOffloadingApi
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.VegaNigeriaOffloadingUseCase
import com.olam.warehouse.vegax.offloadingnigeria.data.repo.VegaNigeriaOffloadingRepository
import com.olam.warehouse.vegax.offloadingnigeria.data.repo.VegaNigeriaOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectNigeriaOffloadingFeature() = loadFeature

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
    factory { VegaNigeriaOffloadingUseCase(get()) }
    viewModel { VegaNigeriaOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaOffloadingRepository> { VegaNigeriaOffloadingRepositoryImpl(get(), get(), get()) }
}



