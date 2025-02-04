package com.olam.warehouse.vegax.processing.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.processing.data.api.VegaProcessingApi
import com.olam.warehouse.vegax.processing.data.domain.usecase.VegaProcessingUseCase
import com.olam.warehouse.vegax.processing.data.repo.VegaProcessingRepository
import com.olam.warehouse.vegax.processing.data.repo.VegaProcessingRepositoryImpl
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 2/10/2020.
 */

fun injectProcessingFeature() = loadFeature

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
    factory { VegaProcessingUseCase(get()) }
    viewModel { VegaProcessingViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaProcessingApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaProcessingRepository> { VegaProcessingRepositoryImpl(get(), get(), get()) }
}
