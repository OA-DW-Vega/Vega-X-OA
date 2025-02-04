package com.olam.warehouse.vegax.processingcameroon.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processingcameroon.data.api.VegaCameroonProcessingApi
import com.olam.warehouse.vegax.processingcameroon.data.domain.usecase.VegaCameroonProcessingUseCase
import com.olam.warehouse.vegax.processingcameroon.data.repo.VegaCameroonProcessingRepoImpl
import com.olam.warehouse.vegax.processingcameroon.data.repo.VegaCameroonProcessingRepository
import com.olam.warehouse.vegax.processingcameroon.ui.fgrn.VegaCameroonFgrnViewModel
import com.olam.warehouse.vegax.processingcameroon.ui.rmin.VegaCameroonRminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCameroonProcessingFeature() = loadFeature

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
    factory { VegaCameroonProcessingUseCase(get()) }
    viewModel { VegaCameroonRminViewModel(get(), get()) }
    viewModel { VegaCameroonFgrnViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCameroonProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCameroonProcessingRepository> { VegaCameroonProcessingRepoImpl(get(), get(), get()) }
}
