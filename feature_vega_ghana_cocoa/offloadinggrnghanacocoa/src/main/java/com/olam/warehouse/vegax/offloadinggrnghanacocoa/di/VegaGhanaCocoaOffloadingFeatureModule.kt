package com.olam.warehouse.vegax.offloadinggrnghanacocoa.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.api.VegaGhanaCocoaOffloadingApi
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.VegaGhanaCocoaOffloadingUseCase
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.repo.VegaGhanaCocoaOffloadingRepository
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.repo.VegaGhanaOffloadingRepositoryImpl
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectGhanaOffloadingFeature() = loadFeature

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
    factory { VegaGhanaCocoaOffloadingUseCase(get()) }
    viewModel { VegaGhanaCocoaOffloadingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaGhanaCocoaOffloadingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaGhanaCocoaOffloadingRepository> { VegaGhanaOffloadingRepositoryImpl(get(), get(), get(), get()) }
}



