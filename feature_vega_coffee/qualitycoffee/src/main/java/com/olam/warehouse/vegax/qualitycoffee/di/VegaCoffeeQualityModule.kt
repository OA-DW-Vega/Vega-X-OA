package com.olam.warehouse.vegax.qualitycoffee.di

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.vegax.qualitycoffee.data.api.VegaCoffeeQualityApi
import com.olam.warehouse.vegax.qualitycoffee.data.domain.usecase.VegaCoffeeQualityUseCase
import com.olam.warehouse.vegax.qualitycoffee.data.repo.VegaCoffeeQualityRepository
import com.olam.warehouse.vegax.qualitycoffee.data.repo.VegaCoffeeQualityRepositoryImpl
import com.olam.warehouse.vegax.qualitycoffee.ui.VegaCoffeeQualityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

fun injectCoffeeQualityFeature() = loadFeature

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
    factory { VegaCoffeeQualityUseCase(get()) }
    viewModel { VegaCoffeeQualityViewModel(get(), get()) }
}

val networkModule: Module = module {
    factory { (get(named(Constants.BASE)) as Retrofit).create(VegaCoffeeQualityApi::class.java) }
}

val repositoryModule: Module = module {
    factory<VegaCoffeeQualityRepository> { VegaCoffeeQualityRepositoryImpl(get(), get(),get()) }
}
