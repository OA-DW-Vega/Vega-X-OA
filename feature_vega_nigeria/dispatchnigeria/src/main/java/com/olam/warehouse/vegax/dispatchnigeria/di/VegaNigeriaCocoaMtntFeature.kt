package com.olam.warehouse.vegax.dispatchnigeria.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.dispatchnigeria.data.api.VegaNigeriaCocoaMtntApi
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.usecase.VegaNigeriaCocoaDispatchUseCase
import com.olam.warehouse.vegax.dispatchnigeria.data.repo.VegaNigeriaCocoaMtntRepository
import com.olam.warehouse.vegax.dispatchnigeria.data.repo.VegaNigeriaCocoaMtntRepositoryImpl
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit


fun injectNigeriaCocoaMtntDispatchFeature() = loadFeature

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
    factory { VegaNigeriaCocoaDispatchUseCase(get()) }
    viewModel { VegaNigeriaCocoaMtntViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaNigeriaCocoaMtntApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaNigeriaCocoaMtntRepository> { VegaNigeriaCocoaMtntRepositoryImpl(get(), get(), get()) }
}
