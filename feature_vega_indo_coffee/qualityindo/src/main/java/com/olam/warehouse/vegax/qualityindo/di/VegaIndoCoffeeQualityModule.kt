package com.olam.warehouse.vegax.qualityindo.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualityindo.data.api.VegaIndoCoffeeQualityApi
import com.olam.warehouse.vegax.qualityindo.data.domain.usecase.VegaIndoCoffeeQualityUseCase
import com.olam.warehouse.vegax.qualityindo.data.repo.VegaIndoCoffeeQualityRepository
import com.olam.warehouse.vegax.qualityindo.data.repo.VegaIndoCoffeeQualityRepositoryImpl
import com.olam.warehouse.vegax.qualityindo.ui.VegaIndoCoffeeQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */

fun injectIndoCoffeeQualityFeature() = loadFeature

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
    factory { VegaIndoCoffeeQualityUseCase(get()) }
    viewModel { VegaIndoCoffeeQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaIndoCoffeeQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaIndoCoffeeQualityRepository> { VegaIndoCoffeeQualityRepositoryImpl(get(), get()) }
}
